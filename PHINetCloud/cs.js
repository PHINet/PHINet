/** 
 * File contains code for the Content Store
 * specified in the NDN documentation
 */
    
var DBDataClass = require('./data');
var StringConst = require('./string_const').StringConst;
var postgresDB = require('pg'); // postgres database module
var client = new postgresDB.Client(StringConst.DB_CONNECTION_STRING);

/**
 * Returns object that allows manipulation of ContentStore.
 */
exports.CS = function () {

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
         * Method deletes a single, specific CS entry.
         *
         * @param userID associated with entry to be deleted
         * @param timeString associated with entry to be deleted
         * @param delCallback testing callback: rowCount is returned and checked against expected value
         * @return true if entry successfully deleted, false otherwise
         */
		deleteCSData: function (userID, timeString, delCallback) {

            try {
                if (userID === undefined || userID === null || timeString == undefined || timeString == null) {
                    return false;
                } else {
                    client.query( "DELETE FROM ContentStore WHERE "
                        + StringConst.KEY_USER_ID + " = \'" +  userID + "\' AND " +
                        StringConst.KEY_TIME_STRING + " = \'" + timeString + "\'",

                        function(err, result) {
                            if (err) {
                                delCallback(0);  // error occurred - 0 rows modified; return
                            } else {

                                delCallback(result.rowCount);
                            }
                    });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in ContentStore.deleteCSData(): " + err);
                return false;
            }
        },

        /**
         * Method updates a single, specific CS entry.
         *
         * @param dbDataObject object containing updated row contents
         * @param updateCallback testing callback: rowCount is returned and checked against expected value
         * @return true if entry successfully updated, false otherwise
         */
		updateCSData: function (dbDataObject, updateCallback) {

            try {
                // perform minimal input validation
                if (dbDataObject === null || dbDataObject === undefined || dbDataObject.getUserID() === undefined
                            || dbDataObject.getUserID() === undefined) {
                    return false;
                } else {
                    client.query( "SELECT * FROM ContentStore WHERE " + StringConst.KEY_USER_ID +
                             " = \'" + dbDataObject.getUserID() + "\'", function(err, result) {

                        if (err) {
                            updateCallback(0); // error occurred - 0 rows modified; return
                        } else {
                            // TODO - update data and timestamp

                            updateCallback(result.rowCount);
                        }
                    });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in ContentStore.updateCSData(): " + err);
                return false;
            }
        },

        /**
         * Data is queried without timeString specification; multiple entries may be found.
         *
         * @param userID associated with entries to be returned
         * @param getGenCallback testing callback: rowCount is returned and checked against expected value
         * @return returned entries if found, otherwise null returned
         */
		getGeneralCSData: function (userID, getGenCallback) {

            try {
                if (userID === null || userID === undefined) {
                    return false;
                } else {
                    var allCSEntries = [];
                    client.query( "SELECT * FROM ContentStore WHERE " + StringConst.KEY_USER_ID + " = \'" + userID + "\'",
                        function(err, result) {

                            // TODO - return false if no rows found
                            if (err) {
                                // table doesn't exist

                                getGenCallback(0); // error occurred - 0 rows modified; return
                            } else {

                                for (var i = 0; i < result.rows.length; i++) {
                                    allCSEntries.push(result.rows[i]);

                                    // TODO - create db object for all and return
                                }

                                getGenCallback(result.rowCount);
                            }

                            return allCSEntries;
                        });
                }
            } catch (err) {
                console.log("!!Error in ContentStore.getGeneralCSData(): " + err);
                return false;
            }
		},

        /**
         * Method returns a single, specific CS entry if it exists.
         *
         * @param userID associated with entry to be returned
         * @param timeString associated with entry to be returned
         * @param getSpecCallback testing callback: rowCount is returned and checked against expected value
         * @return entry if found, otherwise null returned
         */
		getSpecificCSData: function (userID, timeString, getSpecCallback) {

            try {
                if (userID === undefined || userID === null || timeString === undefined || timeString == null) {
                    return false;
                } else {

                    client.query( "SELECT * FROM ContentStore WHERE " + StringConst.KEY_USER_ID + " = \'" +
                        user + "\' AND " + StringConst.KEY_TIME_STRING + "= \'" + timeString + "\'", function(err, result) {

                        if (err) {
                            // table doesn't exist
                            // TODO - return false if no entry was found

                            getSpecCallback(0); // error occurred - 0 rows modified; return
                        } else {
                            // TODO - if matching entry found, return

                            getSpecCallback(result.rowCount);
                        }

                    });
                }
            } catch (err) {
                console.log("!!Error in ContentStore.getSpecificCSData(): " + err);
                return false;
            }
        },

        /**
         *    * // TODO - update doc
         *
         * @param dbDataObject data object to be entered
         * @param insCallback testing callback: rowCount is returned and checked against expected value
         * @return true if data was successfully entered into DB, false otherwise
         */
		insertCSData: function(dbDataObject, insCallback)  {

           try {
               if (dbDataObject === null || dbDataObject === undefined || dbDataObject.getUserID() === undefined
                        || dbDataObject.getUserID() === undefined) {
                   return false;
               } else {

                   client.query("INSERT INTO ContentStore(" + StringConst.KEY_USER_ID
                       + "," + StringConst.KEY_SENSOR_ID + "," + StringConst.KEY_TIME_STRING + ","
                       + StringConst.KEY_PROCESS_ID + "," + StringConst.KEY_DATA_CONTENTS
                       +") values($1, $2, $3, $4, $5)", [dbDataObject.getUserID(), dbDataObject.getSensorID(),
                           dbDataObject.getTimeString(),dbDataObject.getProcessID(), dbDataObject.getDataFloat()],

                       function(err, result) {

                           if (err) {
                               insCallback(0); // error occurred - 0 rows modified; return
                           } else {
                               insCallback(result.rowCount);
                           }
                   });

                   return true;
               }
           } catch (err) {
               console.log("!!Error in ContentStore.addCSData(): " + err);
               return false;
           }
        }
	}
};

