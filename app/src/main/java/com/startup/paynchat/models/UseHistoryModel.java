package com.startup.paynchat.models;

public class UseHistoryModel {
    String sub_id, plan_id, plan_title, plan_cat, plan_type, sub_status, price;

    public UseHistoryModel(String sub_id, String plan_id, String plan_title, String plan_cat, String plan_type,
                           String sub_status, String price) {
        this.sub_id = sub_id;
        this.plan_id = plan_id;
        this.plan_title = plan_title;
        this.plan_cat = plan_cat;
        this.plan_type = plan_type;
        this.sub_status = sub_status;
        this.price = price;
    }

    public String getSub_id() {
        return sub_id;
    }

    public void setSub_id(String sub_id) {
        this.sub_id = sub_id;
    }

    public String getPlan_id() {
        return plan_id;
    }

    public void setPlan_id(String plan_id) {
        this.plan_id = plan_id;
    }

    public String getPlan_title() {
        return plan_title;
    }

    public void setPlan_title(String plan_title) {
        this.plan_title = plan_title;
    }

    public String getPlan_cat() {
        return plan_cat;
    }

    public void setPlan_cat(String plan_cat) {
        this.plan_cat = plan_cat;
    }

    public String getPlan_type() {
        return plan_type;
    }

    public void setPlan_type(String plan_type) {
        this.plan_type = plan_type;
    }

    public String getSub_status() {
        return sub_status;
    }

    public void setSub_status(String sub_status) {
        this.sub_status = sub_status;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
