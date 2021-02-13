package com.function.karaoke.hardware.activities.Model;

public class Coupon {

    private String code;
    private String emails;
    private int type;
    private int freeShares = 0;

    public Coupon() {
    }

    public Coupon(String code, String emails, int type, int numberOfFreeShares) {
        this.code = code;
        this.emails = emails;
        this.type = type;
        this.freeShares = numberOfFreeShares;
    }

    public Coupon(String code, String emails, int type) {
        this.code = code;
        this.emails = emails;
        this.type = type;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getFreeShares() {
        return freeShares;
    }

    public void setFreeShares(int freeShares) {
        this.freeShares = freeShares;
    }
}
