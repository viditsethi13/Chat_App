package com.example.finalexam;

public class User {
    String Name;
    String UserId;
    String uri;


    public User(String name, String userId, String uri) {
        Name = name;
        UserId = userId;
        this.uri = uri;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "User{" +
                "Name='" + Name + '\'' +
                '}';
    }
}
