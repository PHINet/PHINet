/** 
 * File contains code for the Pending Interest
 * Table specified in the NDN documentation
 **/

var DBDataClass = require('./data');
var StringConst = require('./string_const').StringConst;
var pg = require('pg');
var client = new pg.Client(StringConst.DB_CONNECTION_STRING);

/**
 * Returns object that allows manipulation of PIT.
 */
exports.PIT = function () {

    /**
     * Function invocation connects to DB
     */
    (function connectClient () {
        client.connect(function(err) {
            if(err) {
                return console.error('could not connect to postgres', err);
            }
        });
    })();

	return {

        /**
         * Method deletes a single, specific PIT entry.
         *
         * @param userID associated with entry to be deleted
         * @param timeString associated with entry to be deleted
         * @param ipAddr associated with entry to be deleted
         * @param delCallback testing callback: rowCount is returned and checked against expected value
         * @return true if entry successfully deleted, false otherwise
         */
        deletePITData: function (userID, timeString, ipAddr, delCallback) {

            try {
                if (userid === undefined || userID === null || timeString === undefined || timeString === null ||
                                ipAddr === undefined === ipAddr === null) {
                    return false;
                } else {
                    client.query( "DELETE FROM PendingInterestTable WHERE "
                    + StringConst.KEY_USER_ID + " = \'" +  userID + "\' AND " + StringConst.KEY_TIME_STRING + " = \'"
                     + timeString + "\' AND " + StringConst.KEY_TIME_STRING + " = \'" + ipAddr + "\'",

                        function(err, result) {

                            if (err) {
                                delCallback(0);  // error occurred - 0 rows modified; return
                            } else {

                                delCallback(result.rowCount);
                            }
                    });

                    return true;
                }
            }
            catch (err) {
                console.log("!!Error in PendingInterestTable.deletePITData(): " + err);
                return false;
            }
		},

        /**
         * Method updates a single, specific PIT entry.
         *
         * @param dbDataObject object containing updated row contents
         * @param updateCallback testing callback: rowCount is returned and checked against expected value
         * @return true if entry successfully updated, false otherwise
         */
		updatePITData: function (dbDataObject, updateCallback) {
			// perform minimal input validation

            try {
                if (dbDataObject === null || dbDataObject === undefined || dbDataObject.getUserID() === undefined
                        || dbDataObject.getUserID() === undefined) {
                    return false;
                } else {
                    client.query( "SELECT * FROM PendingInterestTable WHERE "

                        // TODO - also check other params

                    + StringConst.KEY_USER_ID + " = \'" +dbDataObject.getUserID() + "\'",

                        function(err, result) {

                            if (err) {
                                updateCallback(0);  // error occurred - 0 rows modified; return
                            } else {

                                // TODO - update timestamp

                                updateCallback(result.rowCount);
                            }
                    });

                    return true;
                }

            }
            catch (err) {
                console.log("!!Error in PendingInterestTable.updatePITData(): " + err);
                return false;
            }
		},

        /**
         * Data is queried without ipAddr specification; multiple entries may be found.
         *
         * @param userID specifies which PIT entries should be returned, together with ipAddr
         * @param ipAddr specifies which PIT entries should be returned
         * @param getGenCallback testing callback: rowCount is returned and checked against expected value
         * @return ArrayList of data for userID param
         */
		getGeneralPITData: function (userID, ipAddr, getGenCallback) {

            try {
                if (userid === null || userID === undefined || ipAddr === null || ipAddr == undefined) {
                    return false;
                } else {
                    var allPITEntries = [];
                    client.query( "SELECT * FROM PendingInterestTable WHERE " + StringConst.KEY_USER_ID +
                        " =\'" + userID + "\' AND " + StringConst.KEY_IP_ADDRESS + " = \'" + ipAddr + "\'"
                        , function(err, result) {

                            if (err) {
                                // table doesn't exist
                                getGenCallback(0);  // error occurred - 0 rows modified; return


                                console.log("error: " + err);
                            } else {
                                for (var i = 0; i < result.rows.length; i++) {
                                    // TODO - create db object for all and return
                                }

                                getGenCallback(result.rowCount);
                            }

                            return allPITEntries;
                        });
                }
            }
            catch (err) {
                console.log("!!Error in PendingInterestTable.getGeneralPITData(): " + err);
                return false;
            }
		},

        /**
         * TODO
         *
         * @param userID
         * @param ipAddr
         * @param timeString
         * @param getSpecCallback testing callback: rowCount is returned and checked against expected value
         */
        getSpecificPITData: function(userID,  ipAddr, timeString, getSpecCallback) {
            // TODO
        },

        /**
         * // TODO -
         *
         * @param dbDataObject data object to be entered
         * @param delCallback testing callback: rowCount is returned and checked against expected value
         * @return true if data was successfully entered into DB, false otherwise
         */
		insertPITData: function(dbDataObject, insCallback)  {

            try {
                if (dbDataObject === null || dbDataObject === undefined || dbDataObject.getUserID() === undefined
                        || dbDataObject.getUserID() === undefined) {
                    return false;
                } else {
                    client.query("INSERT INTO PendingInterestTable(" + StringConst.KEY_USER_ID
                        + "," + StringConst.KEY_SENSOR_ID + "," +StringConst. KEY_TIME_STRING + ","
                        + StringConst.KEY_PROCESS_ID + "," + StringConst.KEY_IP_ADDRESS
                        +") values($1, $2, $3, $4, $5)",
                        [dbDataObject.getUserID(), dbDataObject.getSensorID(), dbDataObject.getTimeString(),
                            dbDataObject.getProcessID(), dbDataObject.getIpAddr()],

                        function(err, result) {
                            if (err) {
                                insCallback(0);  // error occurred - 0 rows modified; return
                            } else {

                                insCallback(result.rowCount);
                            }

                        });

                    return true;
                }
            }
            catch (err) {
                console.log("!!Error in PendingInterestTable.insertPITData(): " + err);
                return false;
            }
		}
	}
};