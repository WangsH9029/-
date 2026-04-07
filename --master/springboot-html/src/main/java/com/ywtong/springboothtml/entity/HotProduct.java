package com.ywtong.springboothtml.entity;

import java.math.BigDecimal;

public class HotProduct {
    private Integer rank;
    private String name;
    private Integer sales;
    private BigDecimal amount;
    private Double trend;

    public HotProduct() {
    }

    public HotProduct(Integer rank, String name, Integer sales, BigDecimal amount, Double trend) {
        this.rank = rank;
        this.name = name;
        this.sales = sales;
        this.amount = amount;
        this.trend = trend;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Double getTrend() {
        return trend;
    }

    public void setTrend(Double trend) {
        this.trend = trend;
    }
}
