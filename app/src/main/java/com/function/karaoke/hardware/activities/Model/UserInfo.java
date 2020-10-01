package com.function.karaoke.hardware.activities.Model;

import java.io.Serializable;

public class UserInfo implements Serializable {

    private final int NONE = 100;
    private final int MONTHLY = 101;
    private final int YEARLY = 102;

    String userName;
    String userEmail;
    int userSubscription;

    public UserInfo(String userName, String userEmail) {
        this.userEmail = userEmail;
        this.userName = userName;
    }

    public int getUserSubscription() {
        return userSubscription;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserSubscription(int userSubscription) {
        this.userSubscription = userSubscription;
    }
}
