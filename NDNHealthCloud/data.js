/** 
 * File contains code for the data-object  
 * used to handle patient information
 **/


exports.DATA = { 
	// NOTE: contents will be returned when 
	// other modules "require" this 

	CURRENT_TIME: "CURRENT_TIME",

	// Data Cache Constructor
	csData: function (userID, sensorID, processID, timeString,
                 datafloat) {
        this.sensorID = sensorID;
        this.processID = processID;
        this.timeString = timeString;
        this.userID = userID;
        this.datafloat = datafloat;
    },

    // PIT Entry Constructor
    pitData: function (userID, sensorID, processID, timeString,
                ipAddr) {
        this.sensorID = sensorID;
        this.processID = processID;
        this.timeString = timeString;
        this.userID = userID;
        this.ipAddr = ipAddr;
    },

    // FIB entry constructor
    fibData: function(userID, timeString, ipAddr) {
        this.userID = userID;
        this.timeString = timeString;
        this.ipAddr = ipAddr;
    },

    getApplicationName: function () {
        return applicationName;
    },

    setApplicationName: function (applicationName) {
        this.applicationName = applicationName;
    },

    getSensorID: function () {
        return sensorID;
    },

    setSensorID: function (sensorID) {
        this.sensorID = sensorID;
    },

    getProcessID: function () {
        return processID;
    },

    setProcessID: function (processID) {
        this.processID = processID;
    },

    getTimeString: function () {
        return timeString;
    },

    setTimeString: function (timeString) {

        if (timeString === CURRENT_TIME) {
            var date = new Data();

            timeString = date.getFullYear() + "-" + date.getMonth() + "-" + date.getDay()
                + " " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
        }

        this.timeString = timeString;
    },

    getUserID: function () {
        return userID;
    },

    setUserID: function (userID) {
        this.userID = userID;
    },

    getIpAddr: function () {
        return ipAddr;
    },

    setIpAddr: function (ipAddr) {
        this.ipAddr = ipAddr;
    },

    getDatafloat: function () {
        return datafloat;
    },

    setDatafloat: function (datafloat) {
        this.datafloat = datafloat;
    }
};