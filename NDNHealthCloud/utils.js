/**
 * File contains code for miscellaneous functions.
 */

exports.Utils = {
	
	/** method returns current time **/
	getCurrentTime: function () {

        // format is YYYY-MM-DD

        // TODO - rework this sloppy date-creation

 		var date = new Date();
 		var utcString = date.toUTCString().split(" ");

 		var day = utcString[1]; // day is index 1 

 		var month = date.getMonth() + 1; // month is 0-indexed, so add 1
 		
 		var year = utcString[3]; // year is index 3
 		
 		// if month is single digit, append 0 to front
 		if (month <= 9) { 
 			month = "0" + (month).toString()
 		}

       	var timeString = year + "-" + month + "-" + day;

       	return timeString;
	}

}