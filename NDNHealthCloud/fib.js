/** 
 * File contains code for the Forwarding Information
 * Base specified in the NDN documentation
 **/


 /** NOTE:
 	- code isn't fully functional; db intended
 	- future schema: ForwardingInformationBase(USER_ID text PRIMARY KEY, 
 				TIME_STRING, text, IP_ADDR text)
 */

var DBDataClass = require('./data');
var StringConst = require('./string_const').StringConst;

exports.FIB =  function () {

	var tempDBData = DBDataClass.DATA();
		tempDBData.fibData("serverTestUser", StringConst.CURRENT_TIME, "10.11.12.13");

	return {
		
		// NOTE: This fib entry is data only for testing
		tempFIBArray : [tempDBData],

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
				this.tempFIBArray.push(DBDataObject);
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

			if (this.tempFIBArray.length === 0) {
				return null;
			} else {
				return this.tempFIBArray;
			}	
		}
	}
};