/** 
 * File contains code for the Pending Interest
 * Table specified in the NDN documentation
 **/


 /** NOTE:
 	- code isn't fully functional; db intended
 	- future schema: PendingInterestTable(USER_ID text, SENSOR_ID text, TIME_STRING, text, 
 			PROCESS_ID text, IP_ADDR text, PRIMARY KEY(USER_ID, TIME_STRING, IP_ADDR))

 */

var DBDataClass = require('./data').DATA;

exports.PIT = {

	// NOTE: This fib entry is data only for testing
	tempPITArray : [new DBDataClass.pitData("serverTestUser", "serverTestSensor", 
				"NOW", "testprocessID", "10.11.12.13")],

	addPITData: function (DBDataObject) {

		// perform minimal input validation

		if (DBDataObject.getUserID() !== null && DBDataObject.getIpAddr() !== null 
				&& DBDataObject.getTimeString() !== null) {

			var i;
			for (i = 0; i < this.tempPITArray; i++) {
				if (DBDataObject.getUserID() === this.tempPITArray[i].getUserID() 
					&& DBDataObject.getIpAddr() ===  this.tempPITArray[i].getIpAddr()
					&& DBDataObject.getTimeString() === this.tempPITArray[i].getTimeString()) {

					console.log("PIT entry already exists; cannot add");
					return;
				}
 			}

 			// entry passes input validation; now add
			this.tempPITArray.append(DBDataObject);
		} else {
			console.log("Cannot add null entry to PIT");
		}
	},

	deletePITData: function (userid, timestring, ipaddr) {

		var i; 
		for (i = 0; i < this.tempPITArray.length; i++) {
			if (userid === this.tempPITArray[i].getUserID() 
					&& timestring ===  this.tempPITArray[i].getIpAddr()
					&& ipaddr === this.tempPITArray[i].getTimeString()) {

					this.tempPITArray.splice(i, 1); // remove element from PIT
					console.log("Element successfully removed from PIT");
					return;
				}
		}

		console.log("Element couldn't be removed from pit; no matching entry found");
	},

	updatePITData: function (DBDataObject) {
		// perform minimal input validation

		if (DBDataObject.getUserID() !== null && DBDataObject.getIpAddr() !== null 
				&& DBDataObject.getTimeString() !== null) {

			var i;
			for (i = 0; i < this.tempPITArray; i++) {
				if (DBDataObject.getUserID() === this.tempPITArray[i].getUserID() 
					&& DBDataObject.getIpAddr() ===  this.tempPITArray[i].getIpAddr()
					&& DBDataObject.getTimeString() === this.tempPITArray[i].getTimeString()) {

					this.tempPITArray[i] = DBDataObject;

					console.log("PIT entry updated");
					return;
				}
 			}

 			console.log("wasn't able to find entry in pit; update unsuccessful")

 			
		} else {
			console.log("Cannot update null entry to PIT");
		}
	},

	getPITData: function (userid, timestring, ipaddr) {
		var i; 
		for (i = 0; i < this.tempPITArray.length; i++) {
			if (userid === this.tempPITArray[i].getUserID() 
					&& timestring ===  this.tempPITArray[i].getIpAddr()
					&& ipaddr === this.tempPITArray[i].getTimeString()) {

					console.log("Element successfully returned from PIT");
					return this.tempPITArray[i];
				}
		}

		console.log("Element couldn't be returned from pit; no entry found");
	}
};