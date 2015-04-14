
/**
 * File contains constant strings that are sent withing
 * packets to identify what processing should be invoked.
 *
 * Also contained are other useful string constants.
 */

exports.StringConst = {
	// notify neighbor that fib is desired (sent in interest packet)
    INTEREST_FIB : "INTEREST_FIB",

    // notify recipient that content in data packet is FIBData
    DATA_FIB : "DATA_FIB",

    // notify recipient that content in data packet is CacheData
    DATA_CACHE : "DATA_CACHE",

    // placeholder in name when a field isn't needed for specific packet
    NULL_FIELD : "NULL_FIELD",

    // denotes a user who has no associated IP
    NULL_IP : "NULL_IP",

    // denotes that data from cache is desired
    INTEREST_CACHE_DATA : "INTEREST_CACHE_DATA",

    // used to denote the two types of packets: interest and data
    INTEREST_TYPE : "INTEREST-TYPE",
    DATA_TYPE : "DATA-TLV",

    // notifies current time should be given
    CURRENT_TIME : "CURRENT_TIME",

    // allows node.js postgres module to connect to db
    DB_CONNECTION_STRING: "pg://db_admin:8n0i2m0d6a7@ndnhealthdb.cunk36nc1e3u.us-west-2.rds.amazonaws.com:5432/ndnhealthnetdb",

    // db variables
    CS_DB : "ContentStore",
    PIT_DB : "PendingInterestTable",
    FIB_DB : "ForwardingInformationBase",

    KEY_USER_ID : "_userID",
    KEY_SENSOR_ID : "sensorID",
    KEY_TIME_STRING : "timeString",
    KEY_PROCESS_ID : "processID",
    KEY_IP_ADDRESS : "ipAddress",
    KEY_DATA_CONTENTS : "dataContents", 

    createPITQuery : "CREATE TABLE " + this.PIT_DB + "("
                +this.KEY_USER_ID + " TEXT ," + this.KEY_SENSOR_ID + " TEXT," +
    this.KEY_TIME_STRING + " TEXT," +this.KEY_PROCESS_ID + " TEXT," +this.KEY_IP_ADDRESS + " TEXT,"
                + "PRIMARY KEY(" + this.KEY_USER_ID + "," + this.KEY_TIME_STRING + ", "
                + this.KEY_IP_ADDRESS+ "))",

    createFIBQuery : "CREATE TABLE " + this.FIB_DB + "("
                +this.KEY_USER_ID + " TEXT PRIMARY KEY," + this.KEY_TIME_STRING +
                " TEXT, " +this.KEY_IP_ADDRESS + " TEXT)",

    createCSQuery : "CREATE TABLE " + this.CS_DB + "("
                + this.KEY_USER_ID + " TEXT ," + this.KEY_SENSOR_ID + " TEXT," +
    this.KEY_TIME_STRING + " TEXT ," + this.KEY_PROCESS_ID + " TEXT," +this.KEY_DATA_CONTENTS +
                " TEXT, " + "PRIMARY KEY(" + this.KEY_USER_ID + ", " + this.KEY_TIME_STRING + "))"

};