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

		deleteCSData: function (userid, timestring) {


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
		},

		updateCSData: function (DBDataObject) {
            // perform minimal input validation

            if (DBDataObject.getUserID() !== null && DBDataObject.getDataFloat() !== null
                && DBDataObject.getTimeString() !== null) {

                client.query( "SELECT * FROM ContentStore WHERE "
                + StringConst.KEY_USER_ID + " = \'" + DBDataObject.getUserID() + "\'", function(err, result) {

                    if (err) {
                        // table doesn't exist

                        console.log("error: " + err);
                    } else {

                        // TODO - update data and timestamp
                    }
                });


            } else {
                console.log("Cannot update null entry to FIB");
            }
		},

		// gets all data for specific user
		getGeneralCSData: function (userid) {

			var allCSEntries = [];
            client.query( "SELECT * FROM ContentStore WHERE " + StringConst.KEY_USER_ID + " = \'" + userid + "\'",
                function(err, result) {

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

            return null;
		},

		getSpecificCSData: function (userid, timestring) {
            client.query( "SELECT * FROM ContentStore WHERE " + StringConst.KEY_USER_ID + " = \'" +
                user + "\' AND " + StringConst.KEY_TIME_STRING + "= \'" + timestring + "\'", function(err, result) {

                if (err) {
                    // table doesn't exist

                    console.log("error: " + err);
                } else {
                   // TODO - if matching entry found, return
                }

            });
		}, 

		addCSData: function(dbDataObject)  {
			client.query("INSERT INTO ContentStore(" + StringConst.KEY_USER_ID 
			    + "," + StringConst.KEY_SENSOR_ID + "," + StringConst.KEY_TIME_STRING + "," 
			    + StringConst.KEY_PROCESS_ID + "," + StringConst.KEY_DATA_CONTENTS
			    +") values($1, $2, $3, $4, $5)", [dbDataObject.getUserID(), dbDataObject.getSensorID(),
                dbDataObject.getTimeString(),dbDataObject.getProcessID(), dbDataObject.getDataFloat()]);
		}
	}
};

