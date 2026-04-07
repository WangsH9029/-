package com.ywtong.springboothtml.entity;

import java.math.BigDecimal;

public class SalesDataPoint {
    private String date;
    private BigDecimal amount;
    private Long count;

    public SalesDataPoint() {
    }

    public SalesDataPoint(String date, BigDecimal amount, Long count) {
        this.date = date;
        this.amount = amount;
        this.count = count;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
