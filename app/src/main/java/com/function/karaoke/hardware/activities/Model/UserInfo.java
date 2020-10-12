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

    public UserInfo(){}

    public UserInfo(String id) {
        this.id = id;
    }

    public UserInfo(String userEmail, String userName, String id) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.id = id;
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

    public void setSubscriptionType(int subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
