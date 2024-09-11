package com.example.myapplication;

/**
 * Class to store data before uploading to firebase realtime database
 */
public class DataClass {
    //the data elements that will be stored
    String imageURL, title, uid, pattern, key, username;

    public DataClass(String title, String imageURL, String uid, String pattern, String key, String username) {
        this.title = title;
        this.imageURL = imageURL;
        this.uid = uid;
        this.pattern = pattern;
        this.key = key;
        this.username = username;
    }

    public DataClass()
    {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
