/** 
 * File contains code for the Forwarding Information
 * Base specified in the NDN documentation
 **/


 /** NOTE:
 	- code isn't fully functional; db intended
 	- future schema: ForwardingInformationBase(USER_ID text PRIMARY KEY, 
 				TIME_STRING, text, IP_ADDR text)

 */

var DBDataClass = require('./data').DATA;


exports.FIB =  {
	// NOTE: contents will be returned when 
	// other modules "require" this 


	// NOTE: This fib entry is data only for testing
	tempFIBArray : [new DBDataClass.fibData("serverTestUser", "NOW", "10.11.12.13")],

	addFIBData: function (DBDataObject) {

		// perform minimal input validation

		if (DBDataObject.getUserID() !== null && DBDataObject.getIpAddr() !== null 
				&& DBDataObject.getTimeString() !== null) {

			var i;
			for (i = 0; i < this.tempFIBArray; i++) {
				if (DBDataObject.getUserID() === this.tempFIBArray[i].getUserID()) {

					console.log("FIB entry already exists; cannot add");
					return;
				}
 			}

 			// entry passes input validation; now add
			this.tempFIBArray.append(DBDataObject);
		} else {
			console.log("Cannot add null entry to FIB");
		}
	},

	deleteFIBData: function (userid) {

		var i; 
		for (i = 0; i < this.tempFIBArray.length; i++) {
			if (userid === this.tempFIBArray[i].getUserID()) {

					this.tempFIBArray.splice(i, 1); // remove element from FIB
					console.log("Element successfully removed from FIB");
					return;
				}
		}

		console.log("Element couldn't be removed from FIB; no matching entry found");
	},

	updateFIBData: function (DBDataObject) {
		// perform minimal input validation

		if (DBDataObject.getUserID() !== null && DBDataObject.getIpAddr() !== null 
				&& DBDataObject.getTimeString() !== null) {

			var i;
			for (i = 0; i < this.tempFIBArray; i++) {
				if (DBDataObject.getUserID() === this.tempFIBArray[i].getUserID()) {

					this.tempFIBArray[i] = DBDataObject;

					console.log("FIB entry updated");
					return;
				}
 			}

 			console.log("wasn't able to find entry in FIB; update unsuccessful")

 			
		} else {
			console.log("Cannot update null entry to FIB");
		}
	},

	getFIBData: function (userid) {
		var i; 
		for (i = 0; i < this.tempFIBArray.length; i++) {
			if (userid === this.tempFIBArray[i].getUserID()) {

					console.log("Element successfully returned from FIB");
					return this.tempFIBArray[i];
				}
		}

		console.log("Element couldn't be returned from FIB; no entry found");
	},

	getAllFIBData: function () {
		return this.tempFIBArray;
	}
};