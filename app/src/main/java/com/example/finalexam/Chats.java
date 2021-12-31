package com.example.finalexam;

import com.google.firebase.Timestamp;

public class Chats {
    String Name;
    String LastMsg;
    Timestamp Time;
    String id;

    public Chats(String name, String lastMsg, Timestamp time, String id) {
        Name = name;
        LastMsg = lastMsg;
        Time = time;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLastMsg() {
        return LastMsg;
    }

    public void setLastMsg(String lastMsg) {
        LastMsg = lastMsg;
    }

    public Timestamp getTime() {
        return Time;
    }

    public void setTime(Timestamp time) {
        Time = time;
    }
}
