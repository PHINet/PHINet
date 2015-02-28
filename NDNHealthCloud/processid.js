

/**
 * File contains constant strings that are sent withing
 * packets to identify what processing should be invoked;
 * hence, ProcessID.
 */

exports.ProcessID =  {
	// notify neighbor that fib is desired (sent in interest packet)
    REQUEST_FIB : "ReturnFIB",

    // notify recipient that content in data packet is FIBData
    FIB_DATA : "FIBData"
};