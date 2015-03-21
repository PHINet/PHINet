/** 
 * File contains code for the Content Store
 * specified in the NDN documentation
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
 * Returns object that allows manipulation of ContentStore.
 */
exports.CS = function () {

	/*// NOTE: This fib entry is data only for testing
	var tempDBData1 = DBDataClass.DATA();
		tempDBData1.csData("serverTestUser", "serverTestSensor", 
					StringConst.DATA_CACHE, StringConst.CURRENT_TIME, "10,11,12,13,14,15");

	// NOTE: This fib entry is data only for testing
	var tempDBData2 = DBDataClass.DATA();
		tempDBData2.csData("CLOUD-SERVER", "serverTestSensor", 
					StringConst.DATA_CACHE, StringConst.CURRENT_TIME, "10,11,12,13,77");
*/

    return {

        /**
         * Method deletes a single, specific CS entry.
         *
         * @param userid associated with entry to be deleted
         * @param timestring associated with entry to be deleted
         * @return true if entry successfully deleted, false otherwise
         */
		deleteCSData: function (userid, timestring) {

            try {
                if (userid === undefined || userid === null || timestring == undefined || timestring == null) {
                    return false;
                } else {
                    client.query( "DELETE FROM ContentStore WHERE "
                    + StringConst.KEY_USER_ID + " = \'" +  DBDataObject.getUserID() + "\' AND " +
                    StringConst.KEY_TIME_STRING + " = \'" + timestring + "\'", function(err, result) {

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
                console.log("!!Error in ContentStore.deleteCSData(): " + err);
                return false;
            }
        },

        /**
         * Method updates a single, specific CS entry.
         *
         * @param dbDataObject object containing updated row contents
         * @return true if entry successfully updated, false otherwise
         */
		updateCSData: function (dbDataObject) {
            try {
                // perform minimal input validation
                if (dbDataObject === null || dbDataObject === undefined || dbDataObject.getUserID() === undefined) {
                    return false;
                } else {
                    client.query( "SELECT * FROM ContentStore WHERE "
                    + StringConst.KEY_USER_ID + " = \'" + dbDataObject.getUserID() + "\'", function(err, result) {

                        if (err) {
                            // table doesn't exist

                            console.log("error: " + err);
                        } else {

                            // TODO - update data and timestamp
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
         * Data is queried without timestring specification; multiple entries may be found.
         *
         * @param userid associated with entries to be returned
         * @return returned entries if found, otherwise null returned
         */
		getGeneralCSData: function (userid) {

            try {
                if (userid === null || userid === undefined) {
                    return false;
                } else {
                    var allCSEntries = [];
                    client.query( "SELECT * FROM ContentStore WHERE " + StringConst.KEY_USER_ID + " = \'" + userid + "\'",
                        function(err, result) {

                            // TODO - return false if no rows found
                            if (err) {
                                // table doesn't exist

                                console.log("error: " + err);
                            } else {
                                for (var i = 0; i < result.rows.length; i++) {
                                    // TODO - create db object for all and return
                                }
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
         * @param userid associated with entry to be returned
         * @param timestring associated with entry to be returned
         * @return entry if found, otherwise null returned
         */
		getSpecificCSData: function (userid, timestring) {
            try {
                if (userid === undefined || userid === null || timestring === undefined || timestring == null) {
                    return false;
                } else {
                    client.query( "SELECT * FROM ContentStore WHERE " + StringConst.KEY_USER_ID + " = \'" +
                    user + "\' AND " + StringConst.KEY_TIME_STRING + "= \'" + timestring + "\'", function(err, result) {

                        if (err) {
                            // table doesn't exist
                            // TODO - return false if no entry was found
                            console.log("error: " + err);
                        } else {
                            // TODO - if matching entry found, return
                        }

                    });
                }
            } catch (err) {
                console.log("!!Error in ContentStore.getSpecificCSData(): " + err);
                return false;
            }
        },

        /**
         * @param dbDataObject data object to be entered
         * @return true if data was successfully entered into DB, false otherwise
         */
		addCSData: function(dbDataObject)  {

           try {
               if (dbDataObject === null || dbDataObject === undefined || dbDataObject.getUserID() === undefined) {
                   console.log('returning false');
                   return false;
               } else {
                   client.query("INSERT INTO ContentStore(" + StringConst.KEY_USER_ID
                   + "," + StringConst.KEY_SENSOR_ID + "," + StringConst.KEY_TIME_STRING + ","
                   + StringConst.KEY_PROCESS_ID + "," + StringConst.KEY_DATA_CONTENTS
                   +") values($1, $2, $3, $4, $5)", [dbDataObject.getUserID(), dbDataObject.getSensorID(),
                       dbDataObject.getTimeString(),dbDataObject.getProcessID(), dbDataObject.getDataFloat()],

                       function(err, result) {

                           // TODO - utilize this function
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

