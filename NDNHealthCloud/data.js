/** 
 * File contains code for the data-object  
 * used to handle patient information
 **/

exports.DATA = function () { 

    return {
    	
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
            return this.applicationName;
        },

        setApplicationName: function (applicationName) {
            this.applicationName = applicationName;
        },

        getSensorID: function () {
            return this.sensorID;
        },

        setSensorID: function (sensorID) {
            this.sensorID = sensorID;
        },

        getProcessID: function () {
            return this.processID;
        },

        setProcessID: function (processID) {
            this.processID = processID;
        },

        getTimeString: function () {
            return this.timeString;
        },

        setTimeString: function (timeString) {

            if (timeString === this.CURRENT_TIME) {
                var date = new Data();

                timeString = date.getFullYear() + "-" + date.getMonth() + "-" + date.getDay()
                    + " " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
            }

            this.timeString = timeString;
        },

        getUserID: function () {
            return this.userID;
        },

        setUserID: function (userID) {
            this.userID = userID;
        },

        getIpAddr: function () {
            return this.ipAddr;
        },

        setIpAddr: function (ipAddr) {
            this.ipAddr = ipAddr;
        },

        getDataFloat: function () {
            return this.datafloat;
        },

        setDataFloat: function (datafloat) {
            this.datafloat = datafloat;
        }
    }
};