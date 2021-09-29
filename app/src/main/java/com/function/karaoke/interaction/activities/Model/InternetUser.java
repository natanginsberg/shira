package com.function.karaoke.interaction.activities.Model;

public class InternetUser {

    private String name = "";
    private String email ="";
    private String date = "";


    public InternetUser(){}

    public InternetUser(String name, String email){
        this.name = name;
        this.email = email;
    }

    public InternetUser(String name, String email, String date){
        this.name = name;
        this.email = email;
        this.date = date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
