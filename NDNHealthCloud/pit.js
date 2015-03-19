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

exports.PIT = function () {

	// NOTE: temp var
	/*var tempDBData = DBDataClass.DATA();
	tempDBData.pitData("serverTestUser", "serverTestSensor", 
					StringConst.INTEREST_CACHE_DATA, StringConst.CURRENT_TIME, 
					 "10.11.12.13");*/

	return {

		deletePITData: function (userid, timestring, ipaddr) {

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
		},

		updatePITData: function (DBDataObject) {
			// perform minimal input validation

            if (DBDataObject.getUserID() !== null && DBDataObject.getIpAddr() !== null
                && DBDataObject.getTimeString() !== null) {

                client.query( "SELECT * FROM PendingInterestTable WHERE "

                    // TODO - also check other params

                + StringConst.KEY_USER_ID + " = \'" + DBDataObject.getUserID() + "\'", function(err, result) {

                    if (err) {
                        // table doesn't exist

                        console.log("error: " + err);
                    } else {

                        // TODO - update timestamp
                    }
                });


            } else {
                console.log("Cannot update null entry to FIB");
            }
		},

		// gets all data requested for specific id/ip combination
		getGeneralPITData: function (userid,  ipaddr) {
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
		}, 

		addPITData: function(dbDataObject)  {
			client.query("INSERT INTO PendingInterestTable(" + StringConst.KEY_USER_ID
			    + "," + StringConst.KEY_SENSOR_ID + "," +StringConst. KEY_TIME_STRING + ","
			    + StringConst.KEY_PROCESS_ID + "," + StringConst.KEY_IP_ADDRESS
			    +") values($1, $2, $3, $4, $5)", 
				[dbDataObject.getUserID(), dbDataObject.getSensorID(), dbDataObject.getTimeString(),
                    dbDataObject.getProcessID(), dbDataObject.getIpAddr()]);
		}
	}
};