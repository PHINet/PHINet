/** 
 * File contains code for the Forwarding Information
 * Base specified in the NDN documentation
 **/

var DBDataClass = require('./data');
var StringConst = require('./string_const').StringConst;
var pg = require('pg');
var client = new pg.Client(StringConst.DB_CONNECTION_STRING);

/**
 * Returns object that allows manipulation of FIB.
 */
exports.FIB =  function () {

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
         * Method deletes a single, specific FIB entry.
         *
         * @param userID associated with entry to be deleted
         * @param delCallback testing callback: rowCount is returned and checked against expected value
         * @return true if entry successfully deleted, false otherwise
         */
		deleteFIBData: function (userID, delCallback) {

            try {

                if (userID === null || userID === undefined) {
                    return false;
                } else {
                    client.query( "DELETE FROM ForwardingInformationBase WHERE "
                    + StringConst.KEY_USER_ID + " = \'" +  userID + "\'",

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
                console.log("!!Error in ForwardingInformationBase.deleteFIBData(): " + err);
                return false;
            }
        },

        /**
         * Method updates a single, specific FIB entry.
         *
         * @param dbDataObject object containing updated row contents
         * @param updateCallback testing callback: rowCount is returned and checked against expected value
         * @return true if entry successfully updated, false otherwise
         */
		updateFIBData: function (dbDataObject, updateCallback) {

            try {
                if (dbDataObject === null || dbDataObject === undefined || dbDataObject.getUserID() === undefined) {
                    return false;
                } else {
                    client.query( "UPDATE ForwardingInformationBase SET " + StringConst.KEY_TIME_STRING + " = \'"
                    + dbDataObject.getTimeString() + "\' ," + StringConst.KEY_IP_ADDRESS + " = \'"
                    + dbDataObject.getIpAddr() + "\'" + " WHERE " + StringConst.KEY_USER_ID + " = \'"
                    + dbDataObject.getUserID() + "\'" , function(err, result) {

                        if (err) {

                            updateCallback(0);  // error occurred - 0 rows modified; return
                        } else {

                            updateCallback(result.rowCount)
                        }

                    });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in ForwardingInformationBase.updateFIBData(): " + err);
                return false;
            }
        },

        /**
         * Method returns specific, single FIB entry.
         *
         * @param userID associated with entry to be returned
         * @param getSpecCallback testing callback: rowCount is returned and checked against expected value
         * @return entry if found, otherwise null returned
         */
		getSpecificFIBData: function (userID, getSpecCallback) {

            try {
                if (userID === null || userID === undefined) {
                    return false;
                } else {
                    client.query( "SELECT * FROM ForwardingInformationBase WHERE " + StringConst.KEY_USER_ID
                    + " = \'" + userID + "\'" , function(err, result) {

                        if (err) {

                            getSpecCallback(0);  // error occurred - 0 rows modified; return
                        } else {

                            if (result.rowCount > 0 ) {
                                var queriedRow = DBDataClass.DATA();
                                queriedRow.setUserID(result.rows[0]._userid);
                                queriedRow.setTimeString(result.rows[0].timestring);
                                queriedRow.setIpAddr(result.rows[0].ipaddress);

                                getSpecCallback(result.rowCount, queriedRow);
                            } else {

                                getSpecCallback(0, null);
                            }

                        }
                    });

                    return true;
                }

            } catch (err) {
                console.log("!!Error in ForwardingInformationBase.getSpecificFIBData(): " + err);
                return false;
            }
        },

        /**
         * Method used to query entire FIB table; useful when multi-casting interests
         *
         * @param getAllCallback testing callback: rowCount is returned and checked against expected value
         * @return entries if any exist, otherwise null returned
         */
		getAllFIBData: function (getAllCallback) {

            try {

                if (getAllCallback == null || getAllCallback == undefined) {
                    return false;
                } else {
                    client.query("SELECT * FROM ForwardingInformationBase", function (err, result) {

                        if (err) {
                            getAllCallback(0);  // error occurred - 0 rows modified; return

                        } else {

                            if (result.rowCount > 0) {
                                var queryResults = [];

                                for (var i = 0; i < result.rows.length; i++) {
                                    var queriedRow = DBDataClass.DATA();
                                    queriedRow.setUserID(result.rows[i]._userid);
                                    queriedRow.setTimeString(result.rows[i].timestring);
                                    queriedRow.setIpAddr(result.rows[i].ipaddress);

                                    queryResults.push(queriedRow);
                                }

                                getAllCallback(result.rowCount, queryResults);
                            } else {

                                getAllCallback(0, null);
                            }
                        }

                        return true;
                    });
                }
            } catch (err) {
                console.log("!!Error in ForwardingInformationBase.getAllFIBData(): " + err);
                return false;
            }
		},

        /**
         * Method allows insertion of valid data into forwarding information base.
         *
         * @param dbDataObject data object to be entered
         * @param insCallback testing callback: rowCount is returned and checked against expected value
         * @return true if data was successfully entered into DB, false otherwise
         */
		insertFIBData: function(dbDataObject, insCallback)  {

            try {
                if (dbDataObject === null || dbDataObject === undefined || dbDataObject.getUserID() === undefined
                            || dbDataObject.getUserID() === undefined) {
                    return false;
                } else {
                    client.query("INSERT INTO ForwardingInformationBase(" + StringConst.KEY_USER_ID
                    + "," + StringConst.KEY_TIME_STRING + ","  + StringConst.KEY_IP_ADDRESS
                    +") values($1, $2, $3)", [dbDataObject.getUserID(), dbDataObject.getTimeString(), dbDataObject.getIpAddr()],
                    function(err, result) {

                        if (err) {
                            insCallback(0);  // error occurred - 0 rows modified; return
                        } else {
                            insCallback(result.rowCount);
                        }
                    });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in ForwardingInformationBase.insertFIBData(): " + err);
                return false;
            }
		}
	}
};