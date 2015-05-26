
/**
 * File contains constant strings as well as methods for generating DB Schema.
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
    DB_CONNECTION_STRING: "YOU MUST FIRST ASK FOR PERMISSION",

    // db variables
    CS_DB : "ContentStore",
    CS_TEST_DB: "TestContentStore",

    PIT_DB : "PendingInterestTable",
    PIT_TEST_DB: "TestPendingInterestTable",

    FIB_DB : "ForwardingInformationBase",
    FIB_TEST_DB: "TestForwardingInformationBase",

    LOGIN_DB: "LoginCredentials",
    LOGIN_TEST_DB: "TestLoginCredentials",

    KEY_USER_ID : "_userID",
    KEY_SENSOR_ID : "sensorID",
    KEY_TIME_STRING : "timeString",
    KEY_PROCESS_ID : "processID",
    KEY_IP_ADDRESS : "ipAddress",
    KEY_DATA_CONTENTS : "dataContents",
    KEY_EMAIL: "email",
    KEY_PASSWORD: "password",
    KEY_ENTITY_TYPE: "entityType",  // this key represents doctor or patient status (DOCTOR or PATIENT, respectively)

    DOCTOR_ENTITY: "DOCTOR", // denotes doctor status in LOGIN_DB
    PATIENT_ENTITY: "PATIENT", // denotes patient status in LOGIN_DB

    /**
     * Creates and returns string that generates table and test-table.
     *
     * @param dbName allows code resuse when creating (identical) table and test-table
     * @returns {string} postgres query that creates table
     */
    createPITQuery : function (dbName) {
      return "CREATE TABLE " + dbName + "("
          +this.KEY_USER_ID + " TEXT ," + this.KEY_SENSOR_ID + " TEXT," +
          this.KEY_TIME_STRING + " TEXT," +this.KEY_PROCESS_ID + " TEXT," +this.KEY_IP_ADDRESS + " TEXT,"
          + "PRIMARY KEY(" + this.KEY_USER_ID + "," + this.KEY_TIME_STRING + ", "
          + this.KEY_IP_ADDRESS+ "))";
    },

    /**
     * Creates and returns string that generates table and test-table.
     *
     * @param dbName allows code resuse when creating (identical) table and test-table
     * @returns {string} postgres query that creates table
     */
    createFIBQuery : function(dbName) {
      return "CREATE TABLE " + dbName + "("
          +this.KEY_USER_ID + " TEXT PRIMARY KEY," + this.KEY_TIME_STRING +
          " TEXT, " +this.KEY_IP_ADDRESS + " TEXT)";
    },

    /**
     * Creates and returns string that generates table and test-table.
     *
     * @param dbName allows code resuse when creating (identical) table and test-table
     * @returns {string} postgres query that creates table
     */
    createCSQuery : function(dbName) {
        return "CREATE TABLE " + dbName + "("
        + this.KEY_USER_ID + " TEXT ," + this.KEY_SENSOR_ID + " TEXT," +
        this.KEY_TIME_STRING + " TEXT ," + this.KEY_PROCESS_ID + " TEXT," +this.KEY_DATA_CONTENTS +
        " TEXT, " + "PRIMARY KEY(" + this.KEY_USER_ID + ", " + this.KEY_TIME_STRING + "))";
    },

    /**
     * Creates and returns string that generates table and test-table.
     *
     * @param dbName allows code resuse when creating (identical) table and test-table
     * @returns {string} postgres query that creates table
     */
    createLoginDBQuery : function(dbName) {
        return "CREATE TABLE " + dbName + "("
        + this.KEY_USER_ID + " TEXT ," + this.KEY_EMAIL + " TEXT," +
        this.KEY_PASSWORD + " TEXT ," + this.KEY_ENTITY_TYPE + " TEXT, PRIMARY KEY( " + this.KEY_USER_ID + " ))"
    }
};