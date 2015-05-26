/**
 * File contains code for miscellaneous functions.
 */

exports.Utils = {

    /**
     * Date format is "YYYY-MM-DDTHH:mm:ss.SSS", where 'SSS' is milliseconds and 'T' is a parsing character
     *
     * @return String UTC-compliant current time
     */
	getCurrentTime: function () {

     	var date = new Date();
 		var utcString = date.toUTCString().split(" ");

 		var day = utcString[1]; // day is index 1
 		var month = date.getMonth() + 1; // month is 0-indexed, so add 1

 		var year = utcString[3]; // year is index 3

        var milliseconds = date.getMilliseconds();
        var seconds = date.getSeconds();
        var minutes = date.getMinutes();
        var hours = date.getHours();
 		
 		// if month is single digit, append 0 to front
 		if (month <= 9) { 
 			month = "0" + (month).toString()
 		}

       	return year + "-" + month + "-" + day + "T" + hours + ":" + minutes + ":" + seconds + "." + milliseconds;
	}
};