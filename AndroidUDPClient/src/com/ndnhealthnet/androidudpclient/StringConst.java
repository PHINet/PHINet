package com.ndnhealthnet.androidudpclient;

/**
 * File contains constant strings that are sent withing
 * packets to identify what processing should be invoked;
 * hence, ProcessID.
 *
 * Also contained are other useful string constants.
 */
public class StringConst {

    // notify neighbor that fib is desired (sent in interest packet)
    static public final String REQUEST_FIB = "ReturnFIB";

    // notify recipient that content in data packet is FIBData
    static public final String FIB_DATA = "FIBData";

    // placeholder in name when a field isn't needed for specific packet
    public static final String NULL_FIELD = "NULL_FIELD";

    // denotes a user who has no associated IP
    public static final String NULL_IP = "NULL_IP";

    // denotes that data from cache is desired
    static public final String REQUEST_CACHE_DATA = "CacheRequest";

}
