package com.nkh.appchat.model;

public class Image {
    private String ImageUrl;

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public Image() {
    }

    public Image(String imageUrl) {
        ImageUrl = imageUrl;
    }
}
