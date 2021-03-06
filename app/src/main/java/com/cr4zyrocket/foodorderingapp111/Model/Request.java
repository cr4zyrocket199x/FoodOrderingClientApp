package com.cr4zyrocket.foodorderingapp111.Model;

import java.util.List;

public class Request {
    private String phone, name, address, total, status, comment;
    private List<Order> foodOrdered;

    public Request() {
    }

    public Request(String phone, String name, String address, String total, String status, String comment, List<Order> foodOrdered) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.status = status;
        this.comment = comment;
        this.foodOrdered = foodOrdered;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Order> getFoodOrdered() {
        return foodOrdered;
    }

    public void setFoodOrdered(List<Order> foodOrdered) {
        this.foodOrdered = foodOrdered;
    }
}
