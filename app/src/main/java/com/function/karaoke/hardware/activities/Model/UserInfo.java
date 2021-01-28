package com.function.karaoke.hardware.activities.Model;

import com.function.karaoke.hardware.storage.UserService;

import java.io.Serializable;

public class UserInfo implements Serializable {

    private final int NONE = 100;
    private final int MONTHLY = 101;
    private final int YEARLY = 102;

    String userName;
    String userEmail;
    int subscriptionType = 1;
    String id;
    int shares;
    String picUrl;

    public UserInfo(){}

    public UserInfo(String id) {
        this.id = id;
    }

    public UserInfo(String userEmail, String userName, String picUrl, String id, int shares) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.id = id;
        this.shares = shares;
    }


    public int getSubscriptionType() {
        return subscriptionType;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setSubscriptionType(int subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int getShares(){
        return shares;
    }
}
