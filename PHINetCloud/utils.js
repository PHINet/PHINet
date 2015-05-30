/**
 * File contains code for miscellaneous functions.
 */

var bcrypt = require('bcrypt');


exports.Utils = {

    /**
     * Date format is "YYYY-MM-DDTHH.mm.ss.SSS", where 'SSS' is milliseconds and 'T' is a parsing character
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
	},

	/**
	 * TODO - document
	 * TODO - test
	 *
	 * @param password
	 * @param callback
	 */
	encryptPassword : function(password, callback) {

		if (!password || !callback) {
			throw "!!Error: invalid input to utils.encryptPassword()!";
		} else {
            bcrypt.genSalt(10, function(err, salt) {
                if (err)
                    return callback(err);

                bcrypt.hash(password, salt, function(err, hash) {
                    return callback(err, hash);
                });

            })
        }
 	},

	/**
	 * TODO - document
	 *
	 * TODO - test
	 *
	 * @param password
	 * @param userPassword
	 * @param callback
	 */
	comparePassword : function(password, userPassword, callback) {

		if (!password || !userPassword || !callback) {
			throw "!!Error: invalid input to utils.comparePassword()!";
		} else {
            bcrypt.compare(password, userPassword, function(err, isPasswordMatch) {
                if (err)
                    return callback(err);
                return callback(null, isPasswordMatch);
            });
        }
	},

    /**
     * Method returns true if the data interval is within request interval
     *
     * @param requestInterval a request interval; necessarily must contain two times (start and end)
     * @param dataInterval the time stamp on specific data
     * @return boolean - determination of whether dataInterval is within requestInterval
     */
    isValidForTimeInterval : function (requestInterval, dataInterval) {

        if (requestInterval === null || dataInterval === null) {
            return false;
        }

        var requestIntervals;

        // TIME_STRING FORMAT: "yyyy-MM-ddTHH.mm.ss.SSS||yyyy-MM-ddTHH.mm.ss.SSS"
        // the former is start interval, latter is end interval

        var beforeStartDate = false;
        var afterEndDate = false;

        var startDate, endDate, dataDate;

        try {
            requestIntervals = requestInterval.split("||"); // split interval into start/end

            // replace "T" with empty char "", so that comparison is easier
            // also, application-specific time syntax involves '.', but javascript only recognizes ':' - replace now
            requestIntervals[0] = requestIntervals[0].replace("T", " ").split(".").join(":");
            requestIntervals[1] = requestIntervals[1].replace("T", " ").split(".").join(":");
            dataInterval = dataInterval.replace("T", " ").split(".").join(":");

            startDate = new Date(Date.parse(requestIntervals[0]));
            endDate = new Date(Date.parse(requestIntervals[1]));

            if ( isNaN( startDate.getTime())  ||  isNaN( endDate.getTime()) ) {
                return false; // invalid date detected; return false
            }

            dataDate = new Date(dataInterval);

            beforeStartDate = dataDate < startDate; // test if dataDate is before startDate
            afterEndDate = dataDate > endDate; // test if dataDate is after endDate

        } catch  (e) {
            return false; // some problem occurred, default return is false
        }

        // if dataInterval is not before start and not after end, then its with interval
        return (!beforeStartDate && !afterEndDate) || requestIntervals[0] === dataInterval
            || requestIntervals[1] === dataInterval;
    }
};
