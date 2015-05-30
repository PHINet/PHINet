/** 
 * File contains code for the object that enables UDP communication
 */

var DBData = require('./data'); // used to create objects used by the database
var StringConst = require('./string_const').StringConst;

var PIT, FIB, CS;

var dgram = require("dgram"); // Node.js udp socket module
var socket = dgram.createSocket('udp4');
var utils = require('./utils.js');

var NDN_SENSOR_NET_PORT = 50056; // same across all applications
var mySensorID = "SERVER_SENSOR"; // TODO - rework; this isn't applicable to server
var myUserID = "CLOUD-SERVER"; // TODO - rework to find standard ID for server

var ndnjs_utils = require('./ndnjs_utils.js').ndn_utils;
var Data = require('./ndn-js/data.js').Data;
var Interest = require('./ndn-js/interest.js').Interest;
var Name = require('./ndn-js/name.js').Name;

/**
 * Returns object that handles majority of UDP communication. References to the core
 * NDN databases are passed to the module so that relevant queries can be performed.
 *
 * @param pitReference - reference to the PIT database
 * @param fibReference - reference to the FIB database
 * @param csReference - reference to the CS database
 */
exports.UDPComm = function(pitReference, fibReference, csReference) {

    PIT = pitReference;
    FIB = fibReference;
    CS = csReference;

	return {

        /**
         * Method initializes the UDP listener to specified PORT.
         */
		initializeListener : function () {

            // you must declare these here so that they will be in scope in the callback
            var handleInterestPacket = this.handleInterestPacket;
            var handleDataPacket = this.handleDataPacket;

            socket.bind(NDN_SENSOR_NET_PORT);
			socket.on('message', function(msg, rinfo) {

                // attempt to create both Interest and Data packet; only one should be valid
                var interest = ndnjs_utils.decodeInterest(msg);
                var data = ndnjs_utils.decodeData(msg);

                if (interest && !data) {

                    // Interest packet detected
                    handleInterestPacket(interest, rinfo.address, rinfo.port);
                } else if (!interest && data) {

                    // Data packet detected
                    handleDataPacket(data);
                } else {

                    // this shouldn't have happened; ignore for now

                    // TODO - this shouldn't have happened; handle error
                }
			});
		},

        /**
         * Method sends message to specified ip/port combination. It's inclusion within the
         * module allows other modules to send messages - such as from within server.js
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
         * @param interestPacket incoming ndn-js Interest packet after having been decoded
         * @param packetIP specifies IP that will receive reply if parse success
         * @param packetPort specifies IP that will receive reply if parse success
         */
        handleInterestPacket: function (interestPacket, packetIP, packetPort) {

            // decode paring characters "||" and then split into array for further parsing
            var nameComponent = interestPacket.getName().toUri().replace("%7C%7C", "||").split("/");

            // information extracted from our name format:
            // "/ndn/userID/sensorID/timeString/processID"
            // the indexes used are position + 1 (the addition 1 due to empty string in 0-index)
            var packetUserID = nameComponent[2];
            var packetSensorID = nameComponent[3];
            var packetTimeString = nameComponent[4];
            var packetProcessID = nameComponent[5];

            // check if packet is an INTEREST for FIB data
            if (packetProcessID === StringConst.INTEREST_FIB) {

                handleInterestFIBRequest(packetUserID, packetSensorID, packetIP, packetPort);
            }
            // check if packet is an INTEREST for CACHE data
            else if (packetProcessID === StringConst.INTEREST_CACHE_DATA) {

                handleInterestCacheRequest(packetUserID, packetSensorID, packetTimeString, packetProcessID,
                    packetIP, packetPort);
            } else {
                // unknown process id; drop packet
            }
        },

        /**
         * handles DATA packet as per NDN specification
         * Method parses packet then stores in cache if requested,
         * and sends out to satisfy any potential Interests.
         *
         * @param dataPacket incoming ndn-js Data packet after having been decoded
         */
        handleDataPacket: function (dataPacket) {

            // decode paring characters "||" and then split into array for further parsing
            var nameComponent = dataPacket.getName().toUri().replace("%7C%7C", "||").split("/");
            var dataContents = dataPacket.getContent().toString();

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

                        if (utils.isValidForTimeInterval(allValidPITEntries[i].getTimeString(), packetTimeString)) {
                            requestCount++;
                        }
                    }

                    if (requestCount > 0) { // positive request count, process packet now

                        // check if DATA packet contains FIB data
                        if (packetProcessID === StringConst.DATA_FIB) {

                            handleFIBData(dataContents);
                        }
                        // check if DATA packet contains CACHE data
                        else if (packetProcessID === StringConst.DATA_CACHE) {

                            handleCacheData(packetUserID, packetSensorID, packetTimeString,
                                packetProcessID, dataContents, allValidPITEntries);
                        } else {
                            // unknown process id; drop packet
                        }
                    }
                }
            });
        }
	};
};

/**
 * Method sends message to specified ip/port combination. It's exclusion from the module ensures
 * that any function from within this file may call it without worrying about scope issues.
 *
 * @param message content sent to receiver
 * @param ip - receiver's ip
 * @param port - receiver's port
 */
function sendMessage (message, ip, port) {

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
}

/**
 * Method handles incoming FIB data: if user data stored, it updates; otherwise, it inserts.
 *
 * @param packetFloatContent contents of FIB Data packet (i.e., "userID,userIP" string)
 */
function handleFIBData (packetFloatContent) {
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
}

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
function handleCacheData (packetUserID, packetSensorID, packetTimeString,
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

                // create and send NDN-compliant packet using ndn-js module
                var packetName = ndnjs_utils.createName(packetUserID, packetSensorID, packetTimeString, packetProcessID);
                var data = ndnjs_utils.createDataPacket(packetFloatContent, packetName);

                var encodedPacket = data.wireEncode();

                // send DATA packet to node that requested it
                sendMessage(encodedPacket, allValidPITEntries[i].ipAddr, NDN_SENSOR_NET_PORT);
            }
        });
    }
}

/**
 * returns entire FIB to user who requested it
 *
 * @param packetUserID userID of entity that requested FIB contents
 * @param packetSensorID sensorID of entity that requested FIB contents
 * @param packetIP specifies IP that will receive reply if parse success
 * @param packetPort specifies IP that will receive reply if parse success
 */
function  handleInterestFIBRequest (packetUserID, packetSensorID, packetIP, packetPort) {
    if (packetUserID && packetSensorID && packetIP && packetPort) {

        FIB.getAllFIBData(function(rowsTouched, allFIBData) {

            if (allFIBData && allFIBData.length > 0) {

                var fibContent = "";

                for (var i = 0; i < allFIBData.length; i++) {

                    // don't send requester node its own data; check first
                    if (allFIBData[i].ipAddr !== packetIP) {

                        // FIB entry syntax: "userID,userIP++"
                        fibContent +=  allFIBData[i].userID + "," + allFIBData[i].ipAddr + "++";
                    }
                }

                // create and send packet with ndn-js module
                var packetName = ndnjs_utils.createName(myUserID, mySensorID, StringConst.CURRENT_TIME,  StringConst.DATA_FIB);
                var data = ndnjs_utils.createDataPacket(fibContent, packetName);

                var encodedPacket = data.wireEncode();

                sendMessage(encodedPacket, packetIP, packetPort); // send interest packet

                return true;
            }
        });
    }
}

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
function handleInterestCacheRequest (packetUserID, packetSensorID, packetTimeString,
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


                // create and send packet with ndn-js module
                var packetName = ndnjs_utils.createName(csQueryResults[i].getUserID(), csQueryResults[i].getSensorID(),
                    csQueryResults[i].getTimeString(), csQueryResults[i].getProcessID());
                var data = ndnjs_utils.createDataPacket(csQueryResults[i].getDataFloat(), packetName);

                var encodedPacket = data.wireEncode();

                sendMessage(encodedPacket, packetIP, NDN_SENSOR_NET_PORT); // reply to interest with DATA from cache
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

                                    // create and send packet with ndn-js module
                                    var packetName = ndnjs_utils.createName(packetUserID, packetSensorID,
                                        packetTimeString,  packetProcessID);
                                    var interest = ndnjs_utils.createInterestPacket(packetName);

                                    var encodedPacket = interest.wireEncode();

                                    // ask for data from nodes in FIB via INTEREST packet
                                    sendMessage(encodedPacket, allFIBData[i].ipAddr, NDN_SENSOR_NET_PORT);
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
}