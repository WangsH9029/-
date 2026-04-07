package com.ywtong.springboothtml.entity;

import java.math.BigDecimal;

public class OverviewStatistics {
    private BigDecimal totalSales;
    private Double salesTrend;
    private Long totalOrders;
    private Double ordersTrend;
    private Long totalUsers;
    private Double usersTrend;
    private Long totalFarmers;
    private Double farmersTrend;

    public OverviewStatistics() {
    }

    public OverviewStatistics(BigDecimal totalSales, Double salesTrend, Long totalOrders, Double ordersTrend,
                            Long totalUsers, Double usersTrend, Long totalFarmers, Double farmersTrend) {
        this.totalSales = totalSales;
        this.salesTrend = salesTrend;
        this.totalOrders = totalOrders;
        this.ordersTrend = ordersTrend;
        this.totalUsers = totalUsers;
        this.usersTrend = usersTrend;
        this.totalFarmers = totalFarmers;
        this.farmersTrend = farmersTrend;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public Double getSalesTrend() {
        return salesTrend;
    }

    public void setSalesTrend(Double salesTrend) {
        this.salesTrend = salesTrend;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Double getOrdersTrend() {
        return ordersTrend;
    }

    public void setOrdersTrend(Double ordersTrend) {
        this.ordersTrend = ordersTrend;
    }

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Double getUsersTrend() {
        return usersTrend;
    }

    public void setUsersTrend(Double usersTrend) {
        this.usersTrend = usersTrend;
    }

    public Long getTotalFarmers() {
        return totalFarmers;
    }

    public void setTotalFarmers(Long totalFarmers) {
        this.totalFarmers = totalFarmers;
    }

    public Double getFarmersTrend() {
        return farmersTrend;
    }

    public void setFarmersTrend(Double farmersTrend) {
        this.farmersTrend = farmersTrend;
    }
}
