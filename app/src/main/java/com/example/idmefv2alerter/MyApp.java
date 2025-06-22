package com.example.idmefv2alerter;

import android.app.Application;

public class MyApp extends Application {
    private String user = "John Doe";
    private String url = "http://141.95.158.49/api_idmefv2/";
    private String login = "admin";
    private String password = "S4Sadmin";
    private Integer user_category = 0;
    private String priority = "Info";

    private Integer period = 0;

    private String descr = "No problem";

    private String message = "Hearbeat Smartphone";

    private String latitude = "Unknown";

    private String longitude = "Unknown";

    public void setUser(String s) {
        this.user = s;
    }

    public void setUrl(String s) {
        this.url = s;
    }

    public void setLogin(String s) {
        this.login = s;
    }

    public void setPassword(String s) {
        this.password = s;
    }

    public void setUserCategory(Integer s) {
        this.user_category = s;
    }

    public void setPriority(String s) {
        this.priority = s;
    }

    public void setPeriod(Integer s) {
        this.period = s;
    }

    public void setMessage(String s) {
        this.message = s;
    }

    public void setDescr(String s) {
        this.descr = s;
    }

    public void setLatitude(String s) {
        this.latitude = s;
    }

    public void setLongitude(String s) {
        this.longitude = s;
    }

    public String getUser() {
        return user;
    }

    public String getUrl() {
        return url;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getUserCategory() {
        String[] items = {"Phone", "Car", "Motorcycle", "Bike", "Truck" };
        return items[user_category];
    }

    public Integer getUserCategoryInt() {
        return user_category;
    }

    public String getPriority() {
        return priority;
    }

    public Integer getPeriod() {
        return period;
    }

    public String getMessage() {
        return message;
    }

    public String getDescr() {
        return descr;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getCategory() {
        return "Availability.Heartbeat";
    }

    public String getJSONLoc() {
        if (latitude != "Unknown")
            return ",         \"Location\": \"" + latitude + ", " + longitude + "\"\n";
        return "";
    }

    public String getJSONUser() {
        if ( user != "")
            return ",         \"User\": \"" + user + "\"\n";
        return "";
    }

    public String getJSONCategory() {
        return ",         \"Category\": \"" + getUserCategory() + "\"\n";
    }

    public String getJSONNote() {
        if (message != "")
            return "    \"Note\": \"" + message.replace("\"", "\\\"") + "\",\n";
        return "";
    }
}
