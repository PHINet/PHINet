package com.example.androidudpclient;


public class ProcessID {

    // notify neighbor that fib is desired (sent in interest packet)
    static public final String REQUEST_FIB = "ReturnFIB";

    // notify recipient that content in data packet is FIBData
    static public final String FIB_DATA = "FIBData";
}
