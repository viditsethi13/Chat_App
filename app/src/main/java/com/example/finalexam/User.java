package com.example.finalexam;

public class User {
    String Name;
    String UserId;

    public User(String name, String userId) {
        Name = name;
        UserId = userId;
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

    @Override
    public String toString() {
        return "User{" +
                "Name='" + Name + '\'' +
                '}';
    }
}
