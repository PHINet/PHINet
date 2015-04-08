/** 
 * File contains code for the Forwarding Information
 * Base specified in the NDN documentation
 **/

var DBDataClass = require('./data');
var StringConst = require('./string_const').StringConst;

var pg = require('pg');

var client = new pg.Client(StringConst.DB_CONNECTION_STRING);
client.connect(function(err) {
  if(err) {
    return console.error('could not connect to postgres', err);
  } 
  client.query('SELECT NOW() AS "theTime"', function(err, result) {
    if(err) {
      return console.error('error running query', err);
    }
    console.log("the time: " + result.rows[0].theTime);
    //output: Tue Jan 15 2013 19:12:47 GMT-600 (CST)
    client.end();
  });
});

/**
 * Returns object that allows manipulation of FIB.
 */
exports.FIB =  function () {

	/*var tempDBData = DBDataClass.DATA();
		tempDBData.fibData("serverTestUser", StringConst.CURRENT_TIME, "10.11.12.13");*/

	return {

        /**
         * Method deletes a single, specific FIB entry.
         *
         * @param userid associated with entry to be deleted
         * @return true if entry successfully deleted, false otherwise
         */
		deleteFIBData: function (userid) {

            try {

                if (userid === null || userid === undefined) {
                    return false;
                } else {
                    client.query( "DELETE FROM ForwardingInformationBase WHERE "
                    + StringConst.KEY_USER_ID + " = \'" +  DBDataObject.getUserID() + "\'", function(err, result) {

                        if (err) {
                            // table doesn't exist

                            console.log("error: " + err);
                        } else {

                            // TODO - perform some check
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
         * @param data object containing updated row contents
         * @return true if entry successfully updated, false otherwise
         */
		updateFIBData: function (dbDataObject) {
			// perform minimal input validation

            try {
                if (dbDataObject.getUserID() === undefined || dbDataObject === null 
                    || dbDataObject === undefined) {
                    return false;
                } else {
                    client.query( "SELECT * FROM ForwardingInformationBase WHERE "
                    + StringConst.KEY_USER_ID + " = \'" + dbDataObject.getUserID() + "\'", function(err, result) {

                        if (err) {
                            // table doesn't exist

                            console.log("error: " + err);
                        } else {

                            // TODO - update IP and timestamp
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
         * @param userid associated with entry to be returned
         * @return entry if found, otherwise null returned
         */
		getFIBData: function (userid) {

            try {
                if (userid === null || userid === undefined) {
                    return false;
                } else {
                    client.query( "SELECT * FROM ForwardingInformationBase", function(err, result) {

                        if (err) {
                            // table doesn't exist

                            // TODO - return false if no entry found
                            console.log("error: " + err);
                        } else {

                            for (var i = 0; i < result.rows.length; i++) {
                                if (result.rows[i].userid === userid) {

                                    // TODO - create db object and return

                                }
                            }
                        }
                    });

                    return true;
                }

            } catch (err) {
                console.log("!!Error in ForwardingInformationBase.getFIBData(): " + err);
                return false;
            }
        },

        /**
         * Method used to query entire FIB table; useful when multi-casting interests
         *
         * @return entries if any exist, otherwise null returned
         */
		getAllFIBData: function () {

            try {
                var allFIBEntries = [];
                client.query( "SELECT * FROM ForwardingInformationBase", function(err, result) {

                    if (err) {
                        // table doesn't exist

                        // TODO - return false if nothing found

                        console.log("error: " + err);
                    } else {
                        for (var i = 0; i < result.rows.length; i++) {
                            // TODO - create db object for all and return
                        }
                    }

                    return allFIBEntries;
                });
            } catch (err) {
                console.log("!!Error in ForwardingInformationBase.getAllFIBData(): " + err);
                return false;
            }
		},

        /**
         * @param dbDataObject data object to be entered
         * @return true if data was successfully entered into DB, false otherwise
         */
		insertFIBData: function(dbDataObject)  {

            try {
                if (dbDataObject === null || dbDataObject === undefined || dbDataObject.getUserID() === undefined) {
                    return false;
                } else {
                    client.query("INSERT INTO ForwardingInformationBase(" + StringConst.KEY_USER_ID
                    + "," + StringConst.KEY_TIME_STRING + ","  + StringConst.KEY_IP_ADDRESS
                    +") values($1, $2, $3)", [dbDataObject.getUserID(), dbDataObject.getTimeString(), dbDataObject.getIpAddr()],
                    function(err, result) {
                        // TODO - utilize this function
                    });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in ForwardingInformationBase.getSpecificFIBData(): " + err);
                return false;
            }
		}
	}
};