package com.startup.paynchat.models;

public class SubCategoryModel {

    String id;
    String category;
    String subcategory;
    String image;
    int imgDrawable = 0;

    public SubCategoryModel(String id, String category, String subcategory, String image) {
        this.imgDrawable = 0;
        this.id = id;
        this.category = category;
        this.subcategory = subcategory;
        this.image = image;
    }

    public SubCategoryModel(int imgDrawable, String id, String category, String subcategory, String image) {
        this.imgDrawable = imgDrawable;
        this.id = id;
        this.category = category;
        this.subcategory = subcategory;
        this.image = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getImgDrawable() {
        return imgDrawable;
    }

    public void setImgDrawable(int imgDrawable) {
        this.imgDrawable = imgDrawable;
    }

}
