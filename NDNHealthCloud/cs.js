/** 
 * File contains code for the Content Store
 * specified in the NDN documentation
 **/

 /** NOTE:
 	- code isn't fully functional; db intended
 	- future schema: ContentStore(USER_ID text, SENSOR_ID text, TIME_STRING, text, 
 			PROCESS_ID text, DATA_CONTENTS text, PRIMARY KEY(USER_ID, TIME_STRING))

 */

var DBDataClass = require('./data');

exports.CS = function () {

	// NOTE: This fib entry is data only for testing
	var tempDBData1 = DBDataClass.DATA();
		tempDBData1.csData("serverTestUser", "serverTestSensor", 
					"NOW", "testprocessID", "10,11,12,13,14,15");

	// NOTE: This fib entry is data only for testing
	var tempDBData2 = DBDataClass.DATA();
		tempDBData2.csData("SERVER", "serverTestSensor", 
					"NOW", "testprocessID", "10,11,12,13,14,15,99,99,99");

	return {

		// NOTE: This fib entry is data only for testing
		tempCSArray : [tempDBData1, tempDBData2],

		addCSData: function (DBDataObject) {

			// perform minimal input validation

			if (DBDataObject.getUserID() !== null && DBDataObject.getIpAddr() !== null 
					&& DBDataObject.getTimeString() !== null) {

				var i;
				for (i = 0; i < this.tempCSArray; i++) {
					if (DBDataObject.getUserID() === this.tempCSArray[i].getUserID() 
						&& DBDataObject.getTimeString() === this.tempCSArray[i].getTimeString()) {

						console.log("CS entry already exists; cannot add");
						return;
					}
	 			}

	 			// entry passes input validation; now add
				this.tempCSArray.push(DBDataObject);
			} else {
				console.log("Cannot add null entry to CS");
			}
		},

		deleteCSData: function (userid, timestring) {

			var i; 
			for (i = 0; i < this.tempCSArray.length; i++) {
				if (userid === this.tempCSArray[i].getUserID() 
						&& timestring ===  this.tempCSArray[i].getIpAddr()) {

						this.tempCSArray.splice(i, 1); // remove element from CS
						console.log("Element successfully removed from CS");
						return;
					}
			}

			console.log("Element couldn't be removed from CS; no matching entry found");
		},

		updateCSData: function (DBDataObject) {
			// perform minimal input validation

			if (DBDataObject.getUserID() !== null && DBDataObject.getIpAddr() !== null 
					&& DBDataObject.getTimeString() !== null) {

				var i;
				for (i = 0; i < this.tempCSArray; i++) {
					if (DBDataObject.getUserID() === this.tempCSArray[i].getUserID() 
						&& DBDataObject.getTimeString() === this.tempCSArray[i].getTimeString()) {

						this.tempCSArray[i] = DBDataObject;

						console.log("CS entry updated");
						return;
					}
	 			}

	 			console.log("wasn't able to find entry in CS; update unsuccessful")

	 		} else {
				console.log("Cannot update null entry to CS");
			}
		},

		// gets all data for specific user
		getGeneralCSData: function (userid) {

			var allUserData = [];

			var i; 
			for (i = 0; i < this.tempCSArray.length; i++) {
				if (userid === this.tempCSArray[i].getUserID()) {

						console.log("Element successfully returned from CS");
						allUserData.push(this.tempCSArray[i]);
					}
			}

			if (allUserData.length === 0) {
				return null;
			} else {
				return allUserData;
			}

			

			console.log("Element couldn't be returned from CS; no entry found");
		},

		getSpecificCSData: function (userid, timestring) {
			var i; 
			for (i = 0; i < this.tempCSArray.length; i++) {
				if (userid === this.tempCSArray[i].getUserID() 
						&& ipaddr === this.tempCSArray[i].getTimeString()) {

						console.log("Element successfully returned from CS");
						return this.tempCSArray[i];
					}
			}

			console.log("Element couldn't be returned from CS; no entry found");
		}
	}
};

