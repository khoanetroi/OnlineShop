package com.example.onlineshop.Domain;

import java.util.ArrayList;

public class OrderModel {
    private String orderId;
    private String userId;
    private double subtotal;
    private double tax;
    private double delivery;
    private double total;
    private long createdAt;
    private String status;
    private ArrayList<ItemsModel> items;

    public OrderModel() {
    }

    public OrderModel(String orderId, String userId, double subtotal, double tax, double delivery, double total,
                      long createdAt, String status, ArrayList<ItemsModel> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.subtotal = subtotal;
        this.tax = tax;
        this.delivery = delivery;
        this.total = total;
        this.createdAt = createdAt;
        this.status = status;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getDelivery() {
        return delivery;
    }

    public void setDelivery(double delivery) {
        this.delivery = delivery;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<ItemsModel> getItems() {
        return items;
    }

    public void setItems(ArrayList<ItemsModel> items) {
        this.items = items;
    }
}
