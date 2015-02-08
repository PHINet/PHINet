package com.example.androidudpclient;

import java.util.ArrayList;

/** Temporary class used to store patient data; valid until cache is functional **/
public class Patient {

    private String ip, name;
    private ArrayList<Integer> data;

    Patient(String ip, String name) {
        this.ip = ip;
        this.name = name;
        this.data = new ArrayList<Integer>(); // TODO - rework
    }

    void setName(String name) {
        this.name = name;
    }

    void setIP(String ip) {
        this.ip = ip;
    }

    void addData(int data) {
        this.data.add(data);
    }

    String getIP() {
        return this.ip;
    }

    String getName() {
        return this.name;
    }

    ArrayList<Integer> getData() {
        return this.data;
    }

    String getDataAsString() {
        String stringData = "";

        for (int i = 0; i < data.size(); i++) {
            stringData += Integer.toString(data.get(i)) + ",";
        }

        return stringData;
    }
}
