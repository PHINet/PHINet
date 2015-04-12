/** 
 * File contains code for the data-object  
 * used to handle patient information
 **/

var StringConst = require('./string_const').StringConst;
var Utils = require('./utils').Utils;

/**
 *
 */
exports.DATA = function () { 

    return {

        /**
         * constructor for CS
         *
         * @param sensorID associated with CS data
         * @param processID associated with CS data
         * @param timeString associated with CS data
         * @param userID associated with CS data
         * @param dataFloat contents associated with CS data
         */
    	csData: function (userID, sensorID, processID, timeString,
                     dataFloat) {

             // if current time requested, provide it
            if (timeString === StringConst.CURRENT_TIME) {
                timeString = Utils.getCurrentTime();
            } 

            this.sensorID = sensorID;
            this.processID = processID;
            this.timeString = timeString;
            this.userID = userID;
            this.dataFloat = dataFloat;
        },

        /**
         * constructor for either PIT
         *
         * @param sensorID associated with PIT data
         * @param processID associated with PIT data
         * @param timeString associated with PIT data
         * @param userID associated with PIT data
         * @param ipAddr associated with request
         */
        pitData: function (userID, sensorID, processID, timeString,
                    ipAddr) {

            // if current time requested, provide it
            if (timeString === StringConst.CURRENT_TIME) {
                timeString = Utils.getCurrentTime();
            }

            this.sensorID = sensorID;
            this.processID = processID;
            this.timeString = timeString;
            this.userID = userID;
            this.ipAddr = ipAddr;
        },

        /**
         * unambiguous FIB DBData constructor
         *
         * @param userID associated with FIB data
         * @param timeString associated with FIB data
         * @param ipAddr associated with FIB data
         */
        fibData: function(userID, timeString, ipAddr) {

            // if current time requested, provide it
            if (timeString === StringConst.CURRENT_TIME) {
                timeString = Utils.getCurrentTime();
            }
            
            this.userID = userID;
            this.timeString = timeString;
            this.ipAddr = ipAddr;
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

        /**
         * Method sets object's timeString. If CURRENT_TIME chose, invoke Utils method.
         */
        setTimeString: function (timeString) {

            if (timeString === StringConst.CURRENT_TIME) {
                timeString = Utils.getCurrentTime();
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
            return this.dataFloat;
        },

        setDataFloat: function (dataFloat) {
            this.dataFloat = dataFloat;
        }
    }
};