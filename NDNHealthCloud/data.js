/** 
 * File contains code for the data-object  
 * used to handle patient information
 **/


exports.data = function() {
	// NOTE: contents will be returned when 
	// other modules "require" this 

	var CURRENT_TIME = "CURRENT_TIME";

	// Data Cache Constructor
	function cacheData(applicationName, sensorID, processID, timeString,
                 userID, datafloat) {
        this.applicationName = applicationName;
        this.sensorID = sensorID;
        this.processID = processID;
        this.timeString = timeString;
        this.userID = userID;
        this.datafloat = datafloat;
    }

    // PIT Entry Constructor
    function pitData(applicationName, sensorID, processID, timeString,
                userID, ipAddr) {
        this.applicationName = applicationName;
        this.sensorID = sensorID;
        this.processID = processID;
        this.timeString = timeString;
        this.userID = userID;
        this.ipAddr = ipAddr;
    }

    function getApplicationName() {
        return applicationName;
    }

    function setApplicationName(applicationName) {
        this.applicationName = applicationName;
    }

    function getSensorID() {
        return sensorID;
    }

    function setSensorID(sensorID) {
        this.sensorID = sensorID;
    }

    function getProcessID() {
        return processID;
    }

    function setProcessID(processID) {
        this.processID = processID;
    }

    function getTimeString() {
        return timeString;
    }

    function setTimeString(timeString) {

        /*if (timeString.equals(CURRENT_TIME)) {
            SimpleDateFormat formatUTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ssZ");
            formatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
            timeString =  formatUTC.format(new Date()).toString();
        }*/

        this.timeString = timeString;
    }

    function getUserID() {
        return userID;
    }

    function setUserID(userID) {
        this.userID = userID;
    }

    function getIpAddr() {
        return ipAddr;
    }

    function setIpAddr(ipAddr) {
        this.ipAddr = ipAddr;
    }

    function getDatafloat() {
        return datafloat;
    }

    function setDatafloat(datafloat) {
        this.datafloat = datafloat;
    }
}