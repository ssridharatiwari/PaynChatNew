package com.startup.paynchat.models;

public class CategoryModel {
    String id, category, image;
    int imgDrawable = 0;
    //List<SubCategoryModel> itemsSubCat = new ArrayList<>();

    public CategoryModel(String id, String category, String image) {
        this.imgDrawable = 0;
        this.id = id;
        this.category = category;
        this.image = image;
    }

    public CategoryModel(int imgDrawable, String id, String category, String image) {
        this.imgDrawable = imgDrawable;
        this.id = id;
        this.category = category;
        this.image = image;
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