/** 
 * File contains code for the Forwarding Information
 * Base specified in the NDN documentation
 **/


// TODO - document

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
 *
 */
exports.FIB =  function () {

	/*var tempDBData = DBDataClass.DATA();
		tempDBData.fibData("serverTestUser", StringConst.CURRENT_TIME, "10.11.12.13");*/

	return {

        /**
         *
         * @param userid
         * @returns {boolean}
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
         *
         * @param DBDataObject
         * @returns {boolean}
         */
		updateFIBData: function (DBDataObject) {
			// perform minimal input validation

            try {
                if (DBDataObject.getUserID() === undefined || DBDataObject === null || DBDataObject === undefined) {
                    return false;
                } else {
                    client.query( "SELECT * FROM ForwardingInformationBase WHERE "
                    + StringConst.KEY_USER_ID + " = \'" + DBDataObject.getUserID() + "\'", function(err, result) {

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
         *
         *
         * @param userid
         * @returns {boolean}
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
         *
         * @returns {boolean}
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
         *
         * @param dbDataObject
         * @returns {boolean}
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