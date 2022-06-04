package com.startup.paynchat.models;

public class PlansItemsModel {
    String plan_id, discount, category, price;

    public PlansItemsModel(String plan_id, String discount, String category, String price) {
        this.plan_id = plan_id;
        this.discount = discount;
        this.category = category;
        this.price = price;
    }

    public String getPlan_id() {
        return plan_id;
    }

    public void setPlan_id(String plan_id) {
        this.plan_id = plan_id;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
