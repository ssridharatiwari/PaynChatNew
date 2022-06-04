package com.startup.paynchat.models;

public class AppointmentUser {
    String contact, user_name, query, subcategory, appo_id, status;

    public AppointmentUser(String contact, String user_name, String query, String subcategory, String appo_id, String status) {
        this.contact = contact;
        this.user_name = user_name;
        this.query = query;
        this.subcategory = subcategory;
        this.appo_id = appo_id;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getAppo_id() {
        return appo_id;
    }

    public void setAppo_id(String appo_id) {
        this.appo_id = appo_id;
    }

    public AppointmentUser() {
    }
}
