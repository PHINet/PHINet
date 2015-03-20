package com.ndnhealthnet.androidudpclient;

/**
 * File contains constant strings that are sent withing
 * packets to identify what processing should be invoked.
 *
 * Also contained are other useful string constants.
 */
public class StringConst {

    // notify neighbor that fib is desired (sent in interest packet)
    static public final String INTEREST_FIB = "INTEREST_FIB";

    // notify recipient that content in data packet is FIBData
    static public final String DATA_FIB = "DATA_FIB";

    // notify recipient that content in data packet is CacheData
    static public final String DATA_CACHE = "DATA_CACHE";

    // placeholder in name when a field isn't needed for specific packet
    public static final String NULL_FIELD = "NULL_FIELD";

    // denotes a user who has no associated IP
    public static final String NULL_IP = "NULL_IP";

    // denotes that data from cache is desired
    static public final String INTEREST_CACHE_DATA = "INTEREST_CACHE_DATA";

    // used to denote the two types of packets: interest and data
    static public final String INTEREST_TYPE = "INTEREST-TYPE";
    static public final String DATA_TYPE = "DATA-TLV";

    // notifies current time should be given
    static public final String CURRENT_TIME = "CURRENT_TIME";
}
