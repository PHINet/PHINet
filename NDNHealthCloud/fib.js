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

exports.FIB =  function () {

	/*var tempDBData = DBDataClass.DATA();
		tempDBData.fibData("serverTestUser", StringConst.CURRENT_TIME, "10.11.12.13");*/

	return {

		deleteFIBData: function (userid) {

            client.query( "DELETE FROM ForwardingInformationBase WHERE "
            + StringConst.KEY_USER_ID + " = \'" +  DBDataObject.getUserID() + "\'", function(err, result) {

                if (err) {
                    // table doesn't exist

                    console.log("error: " + err);
                } else {

                    // TODO - perform some check
                }
            });
		},

		updateFIBData: function (DBDataObject) {
			// perform minimal input validation

			if (DBDataObject.getUserID() !== null && DBDataObject.getIpAddr() !== null 
					&& DBDataObject.getTimeString() !== null) {

                client.query( "SELECT * FROM ForwardingInformationBase WHERE "
                + StringConst.KEY_USER_ID + " = \'" + DBDataObject.getUserID() + "\'", function(err, result) {

                    if (err) {
                        // table doesn't exist

                        console.log("error: " + err);
                    } else {

                        // TODO - update IP and timestamp
                    }
                });

	 			
			} else {
				console.log("Cannot update null entry to FIB");
			}
		},

		getFIBData: function (userid) {

            client.query( "SELECT * FROM ForwardingInformationBase", function(err, result) {

                if (err) {
                    // table doesn't exist

                    console.log("error: " + err);
                } else {

                    for (var i = 0; i < result.rows.length; i++) {
                        if (result.rows[i].userid === userid) {

                            // TODO - create db object and return

                        }
                    }
                }
            });

            return null;
		},

		getAllFIBData: function () {

            var allFIBEntries = [];
            client.query( "SELECT * FROM ForwardingInformationBase", function(err, result) {

                if (err) {
                    // table doesn't exist

                    console.log("error: " + err);
                } else {
                    for (var i = 0; i < result.rows.length; i++) {
                        // TODO - create db object for all and return
                    }
                }

                return allFIBEntries;
            });
		}, 

		insertFIBData: function(dbDataObject)  {
			client.query("INSERT INTO ForwardingInformationBase(" + StringConst.KEY_USER_ID 
			 + "," + StringConst.KEY_TIME_STRING + ","  + StringConst.KEY_IP_ADDRESS
			 +") values($1, $2, $3)", [dbDataObject.getUserID(), dbDataObject.getTimeString(), dbDataObject.getIpAddr()]);
		}
	}
};