package com.nkh.appchat.model;

public class User {
    private String id;
    private String userName;
    private String imageURL;
    private String status;
    private String email;
    private String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User() {
    }

    public User(String id, String userName, String imageURL, String status, String email, String password) {
        this.id = id;
        this.userName = userName;
        this.imageURL = imageURL;
        this.status = status;
        this.email = email;
        this.password = password;
    }
}
