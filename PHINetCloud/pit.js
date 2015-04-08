/** 
 * File contains code for the Pending Interest
 * Table specified in the NDN documentation
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
 * Returns object that allows manipulation of PIT.
 */
exports.PIT = function () {

	// NOTE: temp var
	/*var tempDBData = DBDataClass.DATA();
	tempDBData.pitData("serverTestUser", "serverTestSensor", 
					StringConst.INTEREST_CACHE_DATA, StringConst.CURRENT_TIME, 
					 "10.11.12.13");*/

	return {

        /**
         * Method deletes a single, specific PIT entry.
         *
         * @param userid associated with entry to be deleted
         * @param timestring associated with entry to be deleted
         * @param ipaddr associated with entry to be deleted
         * @return true if entry successfully deleted, false otherwise
         */
        deletePITData: function (userid, timestring, ipaddr) {

            try {
                if (userid === undefined || userid === null || timestring === undefined || timestring === null ||
                                ipaddr === undefined === ipaddr === null) {
                    return false;
                } else {
                    client.query( "DELETE FROM PendingInterestTable WHERE "
                    + StringConst.KEY_USER_ID + " = \'" +  DBDataObject.getUserID() + "\' AND " +
                    StringConst.KEY_TIME_STRING + " = \'" + timestring + "\' AND " + StringConst.KEY_TIME_STRING +
                    " = \'" + ipaddr + "\'", function(err, result) {

                        if (err) {
                            // table doesn't exist

                            console.log("error: " + err);
                        } else {

                            // TODO - perform some check
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
         * @return true if entry successfully updated, false otherwise
         */
		updatePITData: function (dbDataObject) {
			// perform minimal input validation

            try {
                if (dbDataObject === null || dbDataObject === undefined || dbDataObject.getUserID() === undefined) {
                    return false;
                } else {
                    client.query( "SELECT * FROM PendingInterestTable WHERE "

                        // TODO - also check other params

                    + StringConst.KEY_USER_ID + " = \'" +dbDataObject.getUserID() + "\'", function(err, result) {

                        if (err) {
                            // table doesn't exist

                            console.log("error: " + err);
                        } else {

                            // TODO - update timestamp
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
         * @param userid specifies which PIT entries should be returned, together with ipaddr
         * @param ipaddr specifies which PIT entries should be returned
         * @return ArrayList of data for userID param
         */
		getGeneralPITData: function (userid,  ipaddr) {

            try {
                if (userid === null || userid === undefined || ipaddr === null || ipaddr == undefined) {
                    return false;
                } else {
                    var allPITEntries = [];
                    client.query( "SELECT * FROM PendingInterestTable WHERE " + StringConst.KEY_USER_ID +
                        " =\'" + userid + "\' AND " + StringConst.KEY_IP_ADDRESS + " = \'" + ipaddr + "\'"
                        , function(err, result) {

                            if (err) {
                                // table doesn't exist

                                console.log("error: " + err);
                            } else {
                                for (var i = 0; i < result.rows.length; i++) {
                                    // TODO - create db object for all and return
                                }
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
         * @param dbDataObject data object to be entered
         * @return true if data was successfully entered into DB, false otherwise
         */
		addPITData: function(dbDataObject)  {

            try {
                if (dbDataObject === null || dbDataObject === undefined || dbDataObject.getUserID() === undefined) {
                    return false;
                } else {
                    client.query("INSERT INTO PendingInterestTable(" + StringConst.KEY_USER_ID
                        + "," + StringConst.KEY_SENSOR_ID + "," +StringConst. KEY_TIME_STRING + ","
                        + StringConst.KEY_PROCESS_ID + "," + StringConst.KEY_IP_ADDRESS
                        +") values($1, $2, $3, $4, $5)",
                        [dbDataObject.getUserID(), dbDataObject.getSensorID(), dbDataObject.getTimeString(),
                            dbDataObject.getProcessID(), dbDataObject.getIpAddr()]);

                    return true;
                }
            }
            catch (err) {
                console.log("!!Error in PendingInterestTable.addPITData(): " + err);
                return false;
            }
		}
	}
};