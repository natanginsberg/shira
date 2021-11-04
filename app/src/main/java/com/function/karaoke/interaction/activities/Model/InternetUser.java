package com.function.karaoke.interaction.activities.Model;

import java.util.Date;

public class InternetUser {

    private String name = "";
    private String email ="";
    private Date date = new Date();


    public InternetUser(){}

    public InternetUser(String email, Date date){
        this.email = email;
        this.date = date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDate() {
        return date;
    }

    public String getEmail() {
        return email;
    }

}
