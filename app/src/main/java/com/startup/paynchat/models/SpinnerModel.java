package com.startup.paynchat.models;
/**
 * Created by ssridharatiwari on 2021.
 */
public class SpinnerModel {
    String id;
    String title;
    String strImgUrl;

    public String getStrBillerAliasName() {
        return strBillerAliasName;
    }

    public void setStrBillerAliasName(String strBillerAliasName) {
        this.strBillerAliasName = strBillerAliasName;
    }

    public String getIsFetch() {
        return isFetch;
    }

    public void setIsFetch(String isFetch) {
        this.isFetch = isFetch;
    }

    String strBillerAliasName;
    String isFetch;

    public SpinnerModel(String id, String title, String strBillerAliasName, String isFetch) {
        this.id = id;
        this.title = title;
        this.strImgUrl = "";
        this.strBillerAliasName = strBillerAliasName;
        this.isFetch = isFetch;
    }

    public SpinnerModel(String title) {
        this.id = "";
        this.title = title;
        this.strImgUrl = "";
        this.strBillerAliasName = "";
        this.isFetch = "";
    }

    public SpinnerModel(String id, String title, String strImgUrl) {
        this.id = id;
        this.title = title;
        this.strImgUrl = strImgUrl;
        this.strBillerAliasName = "";
        this.isFetch = "";
    }

    public SpinnerModel(String id, String title, String desc, String image, String strImgUrl) {
        this.id = id;
        this.title = title;
        this.strImgUrl = strImgUrl;
        this.strBillerAliasName = "";
        this.isFetch = "";
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getStrImgUrl() {
        return strImgUrl;
    }
}