package com.example.messwala.modal;

public class Review {
    private String Username;
    private float Rating;
    private String ReviewText;

    String date;

    public Review(String username, float rating, String reviewText, String date) {
        Username = username;
        Rating = rating;
        ReviewText = reviewText;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public float getRating() {
        return Rating;
    }

    public void setRating(float rating) {
        Rating = rating;
    }

    public String getReviewText() {
        return ReviewText;
    }

    public void setReviewText(String reviewText) {
        ReviewText = reviewText;
    }
}
