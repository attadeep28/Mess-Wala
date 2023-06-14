package com.example.messwala.modal;

public class Mess {
    private String MESS_NAME;
    private String FOOD_TYPE;
    private String PRICE;
    private String DISTANCE;
    private String IMAGE_URL;
    private Double RATING;

    private Double Longitude;
    private Double Latitude;

    private String ID;
    public Double getRATING() {
        return RATING;
    }

    public void setRATING(Double RATING) {
        this.RATING = RATING;
    }

    public Mess(String MESS_NAME, String FOOD_TYPE, String PRICE, String DISTANCE, String IMAGE_URL, Double RATING, Double longitude, Double latitude, String ID) {
        this.MESS_NAME = MESS_NAME;
        this.FOOD_TYPE = FOOD_TYPE;
        this.PRICE = PRICE;
        this.DISTANCE = DISTANCE;
        this.IMAGE_URL = IMAGE_URL;
        this.RATING = RATING;
        Longitude = longitude;
        Latitude = latitude;
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getMESS_NAME() {
        return MESS_NAME;
    }

    public String getFOOD_TYPE() {
        return FOOD_TYPE;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        Longitude = longitude;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }

    public String getPRICE() {
        return PRICE;
    }

    public String getDISTANCE() {
        return DISTANCE;
    }

    public String getIMAGE_URL() {
        return IMAGE_URL;
    }

    public void setMESS_NAME(String MESS_NAME) {
        this.MESS_NAME = MESS_NAME;
    }

    public void setFOOD_TYPE(String FOOD_TYPE) {
        this.FOOD_TYPE = FOOD_TYPE;
    }

    public void setPRICE(String PRICE) {
        this.PRICE = PRICE;
    }

    public void setDISTANCE(String DISTANCE) {
        this.DISTANCE = DISTANCE;
    }

    public void setIMAGE_URL(String IMAGE_URL) {
        this.IMAGE_URL = IMAGE_URL;
    }
}
