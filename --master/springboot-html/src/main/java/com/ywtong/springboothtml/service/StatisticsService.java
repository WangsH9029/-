package com.ywtong.springboothtml.service;

import com.ywtong.springboothtml.entity.*;
import com.ywtong.springboothtml.repository.OrderRepository;
import com.ywtong.springboothtml.repository.ProductRepository;
import com.ywtong.springboothtml.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public OverviewStatistics getOverviewStatistics() {
        Date now = new Date();
        Date lastMonthStart = getDateBefore(now, 30);
        Date twoMonthsAgoStart = getDateBefore(now, 60);

        BigDecimal totalSales = orderRepository.getTotalSalesAfter(new Date(0));
        BigDecimal lastMonthSales = orderRepository.getTotalSalesAfter(lastMonthStart);
        BigDecimal previousMonthSales = orderRepository.getTotalSalesAfter(twoMonthsAgoStart);
        previousMonthSales = previousMonthSales.subtract(lastMonthSales);

        Long totalOrders = orderRepository.count();
        Long lastMonthOrders = orderRepository.countOrdersAfter(lastMonthStart);
        Long previousMonthOrders = orderRepository.countOrdersAfter(twoMonthsAgoStart) - lastMonthOrders;

        Long totalUsers = userRepository.countByRole("ROLE_USER");
        Long lastMonthUsers = userRepository.countByRoleAndCreateTimeAfter("ROLE_USER", lastMonthStart);
        Long previousMonthUsers = userRepository.countByRoleAndCreateTimeAfter("ROLE_USER", twoMonthsAgoStart) - lastMonthUsers;

        Long totalFarmers = userRepository.countByRole("ROLE_FARMER");
        Long lastMonthFarmers = userRepository.countByRoleAndCreateTimeAfter("ROLE_FARMER", lastMonthStart);
        Long previousMonthFarmers = userRepository.countByRoleAndCreateTimeAfter("ROLE_FARMER", twoMonthsAgoStart) - lastMonthFarmers;

        return new OverviewStatistics(
            totalSales != null ? totalSales : BigDecimal.ZERO,
            calculateTrend(lastMonthSales, previousMonthSales),
            totalOrders,
            calculateTrend(lastMonthOrders, previousMonthOrders),
            totalUsers,
            calculateTrend(lastMonthUsers, previousMonthUsers),
            totalFarmers,
            calculateTrend(lastMonthFarmers, previousMonthFarmers)
        );
    }

    public List<SalesDataPoint> getSalesStatistics(String range) {
        Date startDate = getStartDateByRange(range);
        List<Object[]> results = orderRepository.getSalesByDateRange(startDate);

        Map<String, SalesDataPoint> dataMap = new LinkedHashMap<>();
        List<String> dateLabels = generateDateLabels(range);

        for (String date : dateLabels) {
            dataMap.put(date, new SalesDataPoint(date, BigDecimal.ZERO, 0L));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Object[] row : results) {
            String date = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            Long count = ((Number) row[2]).longValue();
            dataMap.put(date, new SalesDataPoint(date, amount, count));
        }

        return new ArrayList<>(dataMap.values());
    }

    public List<UserDataPoint> getUserStatistics(String range) {
        Date startDate = getStartDateByRange(range);
        List<Object[]> results = userRepository.getUserGrowthByDateRange("ROLE_USER", startDate);

        Map<String, UserDataPoint> dataMap = new LinkedHashMap<>();
        List<String> dateLabels = generateDateLabels(range);

        for (String date : dateLabels) {
            dataMap.put(date, new UserDataPoint(date, 0L, 0L));
        }

        Long cumulativeUsers = userRepository.countByRoleAndCreateTimeAfter("ROLE_USER", new Date(0)) -
                               userRepository.countByRoleAndCreateTimeAfter("ROLE_USER", startDate);

        for (Object[] row : results) {
            String date = (String) row[0];
            Long newUsers = ((Number) row[1]).longValue();
            cumulativeUsers += newUsers;
            dataMap.put(date, new UserDataPoint(date, newUsers, cumulativeUsers));
        }

        return new ArrayList<>(dataMap.values());
    }

    public List<FarmerDistribution> getFarmerStatistics(String range) {
        List<Object[]> results = productRepository.getProductCountByCategory();

        return results.stream()
            .map(row -> new FarmerDistribution((String) row[0], ((Number) row[1]).longValue()))
            .collect(Collectors.toList());
    }

    public List<HotProduct> getHotProducts(int limit) {
        Page<Product> topProducts = productRepository.findTopSellingProducts(PageRequest.of(0, limit));

        // 计算本月和上月的时间范围
        Calendar cal = Calendar.getInstance();
        Date currentMonthEnd = cal.getTime();
        cal.add(Calendar.MONTH, -1);
        Date lastMonthEnd = cal.getTime();
        cal.add(Calendar.MONTH, -1);
        Date lastMonthStart = cal.getTime();

        // 查询上月各商品销量
        List<Object[]> lastMonthSales = orderRepository.getProductSalesByDateRange(lastMonthStart, lastMonthEnd);
        Map<Long, Long> lastMonthSalesMap = new HashMap<>();
        for (Object[] row : lastMonthSales) {
            Long productId = ((Number) row[0]).longValue();
            Long quantity = ((Number) row[1]).longValue();
            lastMonthSalesMap.put(productId, quantity);
        }

        List<HotProduct> hotProducts = new ArrayList<>();
        int rank = 1;
        for (Product product : topProducts.getContent()) {
            BigDecimal amount = product.getPrice() != null && product.getSalesCount() != null
                ? product.getPrice().multiply(BigDecimal.valueOf(product.getSalesCount()))
                : BigDecimal.ZERO;

            // 计算趋势：对比当前总销量和上月销量
            Long currentSales = product.getSalesCount() != null ? product.getSalesCount().longValue() : 0L;
            Long lastMonthSalesCount = lastMonthSalesMap.getOrDefault(product.getId(), 0L);
            Double trend = calculateTrend(currentSales, lastMonthSalesCount);

            hotProducts.add(new HotProduct(
                rank++,
                product.getName(),
                product.getSalesCount() != null ? product.getSalesCount() : 0,
                amount,
                trend
            ));
        }

        return hotProducts;
    }

    private Double calculateTrend(Object current, Object previous) {
        if (current == null || previous == null) {
            return 0.0;
        }

        double currentVal = 0.0;
        double previousVal = 0.0;

        if (current instanceof BigDecimal) {
            currentVal = ((BigDecimal) current).doubleValue();
        } else if (current instanceof Number) {
            currentVal = ((Number) current).doubleValue();
        }

        if (previous instanceof BigDecimal) {
            previousVal = ((BigDecimal) previous).doubleValue();
        } else if (previous instanceof Number) {
            previousVal = ((Number) previous).doubleValue();
        }

        if (previousVal == 0) {
            return currentVal > 0 ? 100.0 : 0.0;
        }

        double trend = ((currentVal - previousVal) / previousVal) * 100;
        return Math.round(trend * 10.0) / 10.0;
    }

    private Date getStartDateByRange(String range) {
        Calendar cal = Calendar.getInstance();
        switch (range) {
            case "week":
                cal.add(Calendar.DAY_OF_MONTH, -7);
                break;
            case "year":
                cal.add(Calendar.YEAR, -1);
                break;
            case "month":
            default:
                cal.add(Calendar.MONTH, -1);
                break;
        }
        return cal.getTime();
    }

    private Date getDateBefore(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, -days);
        return cal.getTime();
    }

    private List<String> generateDateLabels(String range) {
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();

        int days;
        switch (range) {
            case "week":
                days = 7;
                break;
            case "year":
                days = 365;
                break;
            case "month":
            default:
                days = 30;
                break;
        }

        for (int i = days - 1; i >= 0; i--) {
            Calendar temp = (Calendar) cal.clone();
            temp.add(Calendar.DAY_OF_MONTH, -i);
            labels.add(sdf.format(temp.getTime()));
        }

        return labels;
    }
}
