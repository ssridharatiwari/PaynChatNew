package com.startup.paynchat.models;

import java.util.ArrayList;

public class PlansModel {
    String nametype;
    ArrayList<PlansItemsModel> plansMOdel = new ArrayList();

    public PlansModel(String nametype, ArrayList<PlansItemsModel> plansMOdel) {
        this.nametype = nametype;
        this.plansMOdel = plansMOdel;
    }

    public String getNametype() {
        return nametype;
    }

    public void setNametype(String nametype) {
        this.nametype = nametype;
    }

    public ArrayList<PlansItemsModel> getPlansMOdel() {
        return plansMOdel;
    }

    public void setPlansMOdel(ArrayList<PlansItemsModel> plansMOdel) {
        this.plansMOdel = plansMOdel;
    }
}
