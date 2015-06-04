package com.ndnhealthnet.androidudpclient.Utility;

/**
 * File contains constants that are sent withing
 * packets to identify what processing should be invoked.
 *
 * Also contained are other useful constants.
 */
public class ConstVar {

    // notify neighbor that fib is desired (sent in interest packet)
    static public final String INTEREST_FIB = "INTEREST_FIB";

    // notify recipient that content in data packet is FIBData
    static public final String DATA_FIB = "DATA_FIB";

    // notify recipient that content in data packet is CacheData
    static public final String DATA_CACHE = "DATA_CACHE";

    // notify the recipient (in this case the server) of client login request
    static public final String LOGIN_REQUEST = "LOGIN_REQUEST";

    // notify the recipient (in this case the server) of client register request
    static public final String REGISTER_REQUEST = "REGISTER_REQUEST";

    // notify the recipient (in this case a client) of the sender requesting credentials
    static public final String CREDENTIAL_REQUEST = "CREDENTIAL_REQUEST";

    // notify recipient (in this case the server) that content in data packet is user login credentials
    static public final String LOGIN_CREDENTIAL_DATA = "LOGIN_CREDENTIAL_DATA";

    // notify recipient (in this case the server) that content in data packet is user signup credentials
    static public final String SIGNUP_CREDENTIAL_DATA = "SIGNUP_CREDENTIAL_DATA";

    // request the result of login from  (in this case the server)
    static public final String INTEREST_LOGIN_RESULT = "INTEREST_LOGIN_RESULT";

    // request the result of login from  (in this case the server)
    static public final String INTEREST_REGISTER_RESULT = "INTEREST_REGISTER_RESULT";

    // used by server to respond to login result request
    static public final String DATA_LOGIN_RESULT = "DATA_LOGIN_RESULT";

    // used by server to respond to register result request
    static public final String DATA_REGISTER_RESULT = "DATA_REGISTER_RESULT";

    // placeholder in name when a field isn't needed for specific packet
    public static final String NULL_FIELD = "NULL_FIELD";

    // used as Data packet content when login/register have failed respective
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String REGISTER_FAILED = "REGISTER_FAILED";

    // denotes a user who has no associated IP
    public static final String NULL_IP = "NULL_IP";

    // denotes that data from cache is desired
    static public final String INTEREST_CACHE_DATA = "INTEREST_CACHE_DATA";

    // used to denote the two types of packets: interest and data
    static public final String INTEREST_TYPE = "INTEREST-TYPE";
    static public final String DATA_TYPE = "DATA-TLV";

    // notifies current time should be given
    static public final String CURRENT_TIME = "CURRENT_TIME";

    // the names of database tables
    public static final String CS_DB = "ContentStore";
    public static final String PIT_DB = "PendingInterestTable";
    public static final String FIB_DB = "ForwardingInformationBase";
    public static final String SENSOR_DB = "Sensors";
    public static final String PACKET_DB = "Packets";

    // use to store/retrieve/identify user login credentials
    public static final String PREFS_LOGIN_USER_ID_KEY = "__USER_ID__" ;
    public static final String PREFS_LOGIN_PASSWORD_ID_KEY = "__PASSWORD_ID__" ;

    // denotes the heartbeat sensor in SensorListActivity
    public static final String HEARTBEAT_SENSOR = "HeartbeatSensor";

    // passed within intent to ViewDataActivity to determine proper entity
    public static final String ENTITY_NAME = "ENTITY_NAME";

    // information used to contact the server
    public static final String SERVER_IP = "54.149.194.227";
    public static final String SERVER_ID = "CLOUD-SERVER";

    // port used by all PHINet applications
    public static final int PHINET_PORT = 50056;
}
