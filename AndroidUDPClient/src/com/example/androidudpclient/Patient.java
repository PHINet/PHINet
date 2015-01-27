package com.example.androidudpclient;

public class Patient {

    private String ip, name;
    Patient(String ip, String name) {
        this.ip = ip;
        this.name = name;
    }

    void setName(String name) {
        this.name = name;
    }

    void setIP(String ip) {
        this.ip = ip;
    }

    String getIP() {
        return this.ip;
    }

    String getName() {
        return this.name;
    }


}
