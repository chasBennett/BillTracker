package com.example.billstracker.custom_objects;

public class Biller {

    private String billerName;
    private String website;
    private String icon;
    private int type;

    public Biller(String billerName, String website, String icon, int type) {

        setBillerName(billerName);
        setWebsite(website);
        setIcon(icon);
        setType(type);
    }

    public Biller() {

    }

    public String getBillerName() {
        return billerName;
    }

    public void setBillerName(String billerName) {
        this.billerName = billerName;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}