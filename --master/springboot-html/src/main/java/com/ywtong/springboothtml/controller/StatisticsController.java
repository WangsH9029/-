package com.ywtong.springboothtml.controller;

import com.ywtong.springboothtml.entity.*;
import com.ywtong.springboothtml.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private static final String SESSION_ROLE = "currentUserRole";

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/overview")
    public OverviewStatistics getOverview(HttpSession session) {
        checkAdminRole(session);
        return statisticsService.getOverviewStatistics();
    }

    @GetMapping("/sales")
    public List<SalesDataPoint> getSalesStatistics(
            @RequestParam(defaultValue = "month") String range,
            HttpSession session) {
        checkAdminRole(session);
        return statisticsService.getSalesStatistics(range);
    }

    @GetMapping("/users")
    public List<UserDataPoint> getUserStatistics(
            @RequestParam(defaultValue = "month") String range,
            HttpSession session) {
        checkAdminRole(session);
        return statisticsService.getUserStatistics(range);
    }

    @GetMapping("/farmers")
    public List<FarmerDistribution> getFarmerStatistics(
            @RequestParam(defaultValue = "month") String range,
            HttpSession session) {
        checkAdminRole(session);
        return statisticsService.getFarmerStatistics(range);
    }

    @GetMapping("/hot-products")
    public List<HotProduct> getHotProducts(
            @RequestParam(defaultValue = "5") int limit,
            HttpSession session) {
        checkAdminRole(session);
        return statisticsService.getHotProducts(limit);
    }

    private void checkAdminRole(HttpSession session) {
        Object role = session.getAttribute(SESSION_ROLE);
        if (!"ROLE_ADMIN".equals(role)) {
            throw new RuntimeException("无权限访问统计数据");
        }
    }
}
