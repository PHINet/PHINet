
/**
 * File contains constant strings that are sent withing
 * packets to identify what processing should be invoked.
 *
 * Also contained are other useful string constants.
 */

exports.StringConst =  {
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
    CURRENT_TIME : "CURRENT_TIME"
};