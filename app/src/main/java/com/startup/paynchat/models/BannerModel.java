package com.startup.paynchat.models;

public class BannerModel {
    String name = "", image, id, urlOpen = "";
    int imageLocal;

    public BannerModel(String name, String image, String id, String urlOpen) {
        this.name = name;
        this.image = image;
        this.id = id;
        this.urlOpen = urlOpen;
    }

    public int getImageLocal() {
        return imageLocal;
    }

    public void setImageLocal(int imageLocal) {
        this.imageLocal = imageLocal;
    }

    public BannerModel(String name, int imageLocal) {
        this.name = name;
        this.imageLocal = imageLocal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrlOpen() {
        return urlOpen;
    }

    public void setUrlOpen(String urlOpen) {
        this.urlOpen = urlOpen;
    }
}
