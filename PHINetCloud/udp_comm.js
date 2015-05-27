/** 
 * File contains code for the object that enables UDP communication
 */

var InterestPacket = require('./interestpacket'); // used to create objects that construct NDN-valid InterestPackets
var DataPacket = require('./datapacket'); // used to create objects that construct NDN-valid DataPackets
var DBData = require('./data'); // used to create objects used by the database
var StringConst = require('./string_const').StringConst;

var PIT, FIB, CS;

var dgram = require("dgram"); // Node.js udp socket module
var socket = dgram.createSocket('udp4');

var NDN_SENSOR_NET_PORT = 50056; // same across all applications
var mySensorID = "SERVER_SENSOR"; // TODO - rework; this isn't applicable to server
var myUserID = "CLOUD-SERVER"; // TODO - rework to find standard ID for server

/**
 * Returns object that handles majority of UDP communication.
 *
 * @param PIT_STRING - used by PIT to query correct database (test or non-test DB)
 * @param FIB_STRING - used by FIB to query correct database (test or non-test DB)
 * @param CS_STRING - used by CS to query correct database (test or non-test DB)
 */
exports.UDPComm = function(PIT_STRING, FIB_STRING, CS_STRING) {

    PIT = require('./pit').PIT(PIT_STRING); // PendingInterestTable database module
    FIB = require('./fib').FIB(FIB_STRING); // ForwardingInformationBase database module
    CS = require('./cs').CS(CS_STRING); // ContentStore database module

	return {

        /**
         * Method initializes the UDP listener to specified PORT.
         */
		initializeListener : function () {

            socket.bind(NDN_SENSOR_NET_PORT);
			socket.on('message', function(msg, rinfo) {

                var message = msg.toString('utf8').split(" ");

                // --- debugging output ---
			    console.log('Received %d bytes from %s:%d\n', msg.length, rinfo.address, rinfo.port);
			    console.log("msg", message);
                // --- debugging output ---

                if (message[0] === "INTEREST-TYPE") {

                    // --- debugging output ---
                    console.log("handle interest");
                    // --- debugging output ---

                    this.handleInterestPacket(message, rinfo.address, rinfo.port);

                } else if (message[0] === "DATA-TLV") {

                    // --- debugging output ---
                    console.log('handle data');
                    // --- debugging output ---

                    this.handleDataPacket(message, rinfo.address, rinfo.port);

                } else {
                    // packet type not recognized, drop it
                }
			});
		},

        /**
         * Method sends message to specified ip/port combination.
         *
         * @param message content sent to receiver
         * @param ip - receiver's ip
         * @param port - receiver's port
         */
        sendMessage: function (message, ip, port) {

            var buffer = new Buffer(message, "utf-8");

            if (port == undefined || port === null) {
                port = NDN_SENSOR_NET_PORT;
            }

            socket.send(buffer, 0, buffer.length, port, ip,
                function(err) {

                    if (err) {
                        console.log("!!Error sending packet: " + err);
                    }
            });
        },

        /**
         * Handles INTEREST packet as per NDN specification
         *
         * Method parses packet then asks the following questions:
         * 1. Do I have the data? If so, I will reply with my own.
         * 2. Have I already sent an interest for this data? If so, I will add this request to PIT entry.
         *
         * @param packetDataArray incoming packet after having been "split" by space and placed array
         * @param packetIP specifies IP that will receive reply if parse success
         * @param packetPort specifies IP that will receive reply if parse success
         */
        handleInterestPacket: function (packetDataArray, packetIP, packetPort) {
            var nameComponent;

            for (var i = 0; i < packetDataArray.length; i++) {
                if (packetDataArray[i] === "NAME-COMPONENT-TYPE") {

                    // i+2 corresponds name as per NDN specification
                    // i = notifier (NAME-COMPONENT-TYPE), i+1 = length of name in bytes, i+2 = name
                    nameComponent = packetDataArray[i+2].split("/"); // split into various components

                } else {
                    // TODO - inspect other packet elements
                }
            }

            // information extracted from our name format:
            // "/ndn/userID/sensorID/timeString/processID"
            // the indexes used are position + 1 (the addition 1 due to empty string in 0-index)
            var packetUserID = nameComponent[2];
            var packetSensorID = nameComponent[3];
            var packetTimeString = nameComponent[4];
            var packetProcessID = nameComponent[5];

            // --- debugging output ---
            console.log("name: " + nameComponent);
            // --- debugging output ---

            // check if packet is an INTEREST for FIB data
            if (packetProcessID === StringConst.INTEREST_FIB) {

                this.handleInterestFIBRequest(packetUserID, packetSensorID, packetIP, packetIP, packetPort);
            }
            // check if packet is an INTEREST for CACHE data
            else if (packetProcessID === StringConst.INTEREST_CACHE_DATA) {

                this.handleInterestCacheRequest(packetUserID, packetSensorID, packetTimeString, packetProcessID,
                    packetIP, packetPort);
            } else {
                // unknown process id; drop packet
            }
        },

        /**
         * returns entire FIB to user who requested it
         *
         * @param packetUserID userID of entity that requested FIB contents
         * @param packetSensorID sensorID of entity that requested FIB contents
         * @param packetIP specifies IP that will receive reply if parse success
         * @param packetPort specifies IP that will receive reply if parse success
         * @return boolean - true if a packet sent, false otherwise (true indicates valid input; useful during testing)
         */
        handleInterestFIBRequest: function (packetUserID, packetSensorID, packetIP, packetPort)
        {
            if (!packetUserID || !packetSensorID || !packetIP || !packetPort) {
                return false;
            } else {

                FIB.getAllFIBData(function(rowsTouched, allFIBData) {

                    if (allFIBData === null || allFIBData.length === 0) {

                        // shouldn't be empty; client connects to server (i.e., enter server's FIB) before requesting data
                        return false;

                    } else {

                        var fibContent = "";

                        for (var i = 0; i < allFIBData.length; i++) {

                            // don't send requester node its own data; check first
                            if (allFIBData[i].ipAddr !== packetIP) {

                                // FIB entry syntax: "userID,userIP++"
                                fibContent +=  allFIBData[i].userID + "," + allFIBData[i].ipAddr + "++";
                            }
                        }

                        // create DATA packet and send entire FIB as single unit
                        var dataPacket = DataPacket.DataPacket();
                        dataPacket.DataPacket(myUserID, mySensorID,
                            StringConst.CURRENT_TIME,  StringConst.DATA_FIB, fibContent);

                        this.sendMessage(dataPacket.toString(), packetIP, packetPort); // send interest packet

                        return true;
                    }
                });
            }
        },

        /**
         * performs NDN logic on packet that requests data
         *
         * @param packetUserID userID associated with requested data from cache
         * @param packetSensorID sensorID associated with requested data from cache
         * @param packetTimeString timeString associated with requested data from cache
         * @param packetProcessID processID associated with requested data from cache
         * @param packetIP specifies IP that will receive reply if parse success
         * @param packetPort specifies IP that will receive reply if parse success
         * @return boolean - true if a packet sent, false otherwise (true indicates valid input; useful during testing)
         */
        handleInterestCacheRequest : function (packetUserID, packetSensorID, packetTimeString,
                                             packetProcessID, packetIP, packetPort)
        {
            // TODO - should packetPort be used?
            // TODO - rework with specific data once TIME variable valid!

            // first, check CONTENT STORE (cache) for requested information
            CS.getGeneralCSData(packetUserID, function(rowsTouched, csQueryResults) {

                // data was in cache; send to requester
                if (csQueryResults) {

                    for (var i = 0; i < csQueryResults.length; i++) {

                        // TODO - again, rework with specific date once TIME_STRING valid;
                        // TODO - remove loop and send as single unit

                        var dataPacket = DataPacket.DataPacket();
                        dataPacket.DataPacket(csQueryResults[i].getUserID(), csQueryResults[i].getSensorID(),
                            csQueryResults[i].getTimeString(), csQueryResults[i].getProcessID(), csQueryResults[i].getDataFloat());

                        this.sendMessage(dataPacket.toString(), packetIP, NDN_SENSOR_NET_PORT); // reply to interest with DATA from cache
                    }

                }
                // data wasn't in cache; check PIT to see if an interest has already been sent for data
                else {

                    // TODO - again, rework with specific date once TIME variable valid

                    PIT.getGeneralPITData(packetUserID, packetIP, function(rowsTouched, queryResults) {

                        // query returned NULL: no INTEREST sent yet; do so now
                        if (!rowsTouched || !queryResults) {

                            // add new request to PIT, then look into FIB before sending request
                            var newPITEntry = DBData.DATA();
                            newPITEntry.pitData(packetUserID, packetSensorID, packetProcessID, packetTimeString, packetIP);
                            PIT.insertPITData(newPITEntry, function(){});

                            FIB.getAllFIBData(function(rowCount, queryResults) {

                                if (queryResults === null || rowCount === 0) {
                                    // TODO - sophisticated way in which user deals with FIB

                                    // shouldn't be empty; client connects to server (i.e., enter server's FIB) before requesting data
                                    throw "!!Error: Cannot send message; FIB is empty.";
                                } else {

                                    for (var i = 0; i < allFIBData.length; i++) {

                                        // don't send data to same node that requested; check first
                                        if (allFIBData[i].ipAddr !== packetIP && allFIBData[i].ipAddr !== null) {

                                            var interestPacket = InterestPacket.InterestPacket();
                                            interestPacket.InterestPacket(packetUserID, packetSensorID,
                                                packetTimeString,  packetProcessID);

                                            // ask for data from nodes in FIB via INTEREST packet
                                            this.sendMessage(interestPacket.toString(), allFIBData[i].ipAddr, NDN_SENSOR_NET_PORT);
                                        }
                                    }
                                }
                            });
                        // query returned value: INTEREST has been sent; append current request to PIT entry
                        } else {

                            // add new request to PIT and wait, request has already been sent
                            var newPITEntry = DBData.DATA();
                            newPITEntry.pitData(packetUserID, packetSensorID, packetProcessID, packetTimeString, packetIP);

                            PIT.insertPITData(newPITEntry, function(){});
                        }
                    });
                }
            });
        },

        /**
         * Method returns true if the data interval is within request interval
         *
         * @param requestInterval a request interval; necessarily must contain two times (start and end)
         * @param dataInterval the time stamp on specific data
         * @return boolean - determination of whether dataInterval is within requestInterval
         */
        isValidForTimeInterval: function (requestInterval, dataInterval) {

            if (requestInterval === null || dataInterval === null) {
                return false;
            }

            var requestIntervals;

            // TIME_STRING FORMAT: "yyyy-MM-ddTHH:mm:ss.SSS||yyyy-MM-ddTHH:mm:ss.SSS"
            // the former is start interval, latter is end interval

            var beforeStartDate = false;
            var afterEndDate = false;

            var startDate, endDate, dataDate;

            try {
                requestIntervals = requestInterval.split("||"); // split interval into start/end


                // replace "T" with empty char "", so that comparison is easier
                requestIntervals[0] = requestIntervals[0].replace("T", " ");
                requestIntervals[1] = requestIntervals[1].replace("T", " ");
                dataInterval = dataInterval.replace("T", " ");

                startDate = new Date(requestIntervals[0]);
                endDate = new Date(requestIntervals[1]);

                if ( isNaN( startDate.getTime())  ||  isNaN( endDate.getTime()) ) {
                    return false; // invalid date detected; return false
                }

                dataDate = new Date(dataInterval);

                beforeStartDate = dataDate < startDate; // test if dataDate is before startDate
                afterEndDate = dataDate > endDate; // test if dataDate is after endDate

            } catch  (e) {
                console.log("!!Error when assessing validity of time interval: " + e);
                return false; // some problem occurred, default return is false
            }

            // if dataInterval is not before start and not after end, then its with interval
            return (!beforeStartDate && !afterEndDate) || requestIntervals[0] === dataInterval
                || requestIntervals[1] === dataInterval;
        },

        /**
         * handles DATA packet as per NDN specification
         * Method parses packet then stores in cache if requested,
         * and sends out to satisfy any potential Interests.
         *
         * @param packetDataArray incoming packet after having been "split" by space and placed array
         */
        handleDataPacket: function (packetDataArray)
        {
            var nameComponent = null;
            var dataContents = null;

            for (var i = 0; i < packetDataArray.length; i++) {
                if (packetDataArray[i] === "NAME-COMPONENT-TYPE") {
                    // i+2 corresponds name as per NDN standard
                    // i = notifier (NAME-COMPONENT-TYPE), i+1 = length of name in bytes, i+2 = name

                    nameComponent = packetDataArray[i+2].trim().split("/"); // split into various components

                } else if (packetDataArray[i] === "CONTENT-TYPE") {

                    // i+2 corresponds content as per NDN standard
                    // i = notifier (CONTENT-TYPE), i+1 = length of content-type in bytes, i+2 = content-type
                    dataContents = packetDataArray[i+2];
                } else {
                    // TODO - inspect other packet elements
                }
            }

            // information extracted from our name format:
            // "/ndn/userID/sensorID/timeString/processID"
            // the indexes used are position + 1 (the addition 1 due to empty string in 0-index)
            var packetUserID = nameComponent[2];
            var packetSensorID = nameComponent[3];
            var packetTimeString= nameComponent[4];
            var packetProcessID = nameComponent[5];

            // first, determine who wants the data
            PIT.getGeneralPITData(packetUserID, packetTimeString, function(rowsTouched, allValidPITEntries) {

                if (allValidPITEntries === null) {

                    // no one requested the data, merely drop it
                } else {

                    // determine if data packet's time interval matches any requests
                    var requestCount = 0;
                    for (var i = 0; i < allValidPITEntries.length; i++) {

                        if (this.isValidForTimeInterval(allValidPITEntries[i].getTimeString(), packetTimeString)) {
                            requestCount++;
                        }
                    }

                    if (requestCount > 0) { // positive request count, process packet now

                        // check if DATA packet contains FIB data
                        if (packetProcessID === StringConst.DATA_FIB) {

                            this.handleFIBData(dataContents);
                        }
                        // check if DATA packet contains CACHE data
                        else if (packetProcessID === StringConst.DATA_CACHE) {

                            this.handleCacheData(packetUserID, packetSensorID, packetTimeString,
                                packetProcessID, dataContents, allValidPITEntries);
                        } else {
                            // unknown process id; drop packet
                        }
                    }
                }
            });
        },

        /**
         * Method handles incoming FIB data: if user data stored, it updates; otherwise, it inserts.
         *
         * @param packetFloatContent contents of FIB Data packet (i.e., "userID,userIP" string)
         */
        handleFIBData: function (packetFloatContent) {
            // expected format: "userID,userIP"
            var packetFIBContent = packetFloatContent.split(","); // TODO - don't rely on this assumption

            var data = DBData.DATA();

            data.setUserID(packetFIBContent[0].trim());
            data.setIpAddr(packetFIBContent[1].trim());
            data.setTimeString(StringConst.CURRENT_TIME);

            // don't add data for self here
            if (data.userID !== myUserID) {

                FIB.getSpecificFIBData(data.userID, function(rowsTouched, queryResult) {
                    if (queryResult === null) {
                        // data packet contains requested fib data, store in fib now

                        FIB.insertFIBData(data, function(){});
                    } else {
                        // data was requested; second, update cache with new packet

                        FIB.updateFIBData(data, function(){});
                    }
                });
            }
        },

        /**
         * Method handles incoming Non-FIB data
         *
         * @param packetUserID userID associated with incoming Data packet
         * @param packetSensorID sensorID associated with incoming Data packet
         * @param packetTimeString timeString associated with incoming Data packet
         * @param packetProcessID processID associated with incoming Data packet
         * @param packetFloatContent contents of incoming Data packet
         * @param allValidPITEntries ArrayList of all PIT entries requesting this data
         */
        handleCacheData: function (packetUserID, packetSensorID, packetTimeString,
                                  packetProcessID, packetFloatContent, allValidPITEntries) {

            var data = DBData.DATA();
            data.csData(packetUserID, packetSensorID, packetTimeString, packetFloatContent);

            // TODO - again, rework for specific CS data once TIME_STRING valid

            // if data exists in cache, just update; otherwise insert
            CS.getGeneralCSData(packetUserID, function(rowsTouched, queryResult) {
                if (queryResult !== null) {

                    CS.updateCSData(data, function(){});
                } else {

                    // data not in cache, add now
                    CS.insertCSData(data, function(){});
                }
            });

            // now, send packets to each entity that requested the data
            for (var i = 0; i < allValidPITEntries.length; i++) {

                // data satisfies PIT entry; delete the entry
                PIT.deletePITData(allValidPITEntries[i].userID, allValidPITEntries[i].timeString,
                        allValidPITEntries[i].ipAddr, function(){});

                require('dns').lookup(require('os').hostname(), function (err, myIPAddress, fam) {

                    // a device other than this requested the DATA
                    if (allValidPITEntries[i].ipAddr !== myIPAddress) {

                        var dataPacket = DataPacket.DataPacket();
                        dataPacket.DataPacket(packetUserID,
                            packetSensorID, packetTimeString, packetProcessID, packetFloatContent);

                        // send DATA packet to node that requested it
                        this.sendMessage(dataPacket.toString(), allValidPITEntries[i].ipAddr, NDN_SENSOR_NET_PORT);
                    }
                });
            }
        }
	};
};