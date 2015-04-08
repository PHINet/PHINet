/** 
 * File contains code for the object
 * that enables UDP communication
 **/

var InterestPacketClass = require('./interestpacket');
var DataPacketClass = require('./datapacket');
var DBDataClass = require('./data');
var StringConst = require('./string_const').StringConst;

var PIT = require('./pit').PIT();
var FIB = require('./fib').FIB();
var CS = require('./cs').CS();

var NDN_SENSOR_NET_PORT = 50056; // same across all applications
var dgram = require("dgram");
var socket = dgram.createSocket('udp4');

var mySensorID = "SERVER_SENSOR"; // TODO - rework; this isn't applicable to server
var myUserID = "CLOUD-SERVER"; // TODO - rework to find standard ID for server

/**
 * Returns object that handles majority of UDP communication.
 */
exports.UDPComm = function() {
	
	return {

        /**
         * Method initializes the UDP listener to specified PORT.
         */
		initializeListener : function () {
			socket.bind(NDN_SENSOR_NET_PORT);
			socket.on('message', function(msg, rinfo) {
			  
			  var message = msg.toString('utf8').split(" ");

			  console.log('Received %d bytes from %s:%d\n', msg.length, rinfo.address, rinfo.port);

			  console.log("msg", message);

			  if (message[0] === "INTEREST-TYPE") {
			  	
			  	console.log("handle interest");
                  this.handleInterestPacket(message, rinfo.address, rinfo.port);

			  } else if (message[0] === "DATA-TLV") {
			  	console.log('handle data');
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

            if (port == undefined) {
                port = NDN_SENSOR_NET_PORT;
            }

            socket.send(buffer, 0, buffer.length, port,
                ip, function(err) {
                });
        },

        /**
         * handles INTEREST packet as per NDN specification
         * Method parses packet then asks the following questions:
         * 1. Do I have the data?
         * 2. Have I already sent an interest for this data?
         *
         * @param packetDataArray incoming packet after having been "split" by space and placed array
         * @param nonPrivateIP specifies IP that will receive reply if parse success
         * @param nonPrivatePort specifies IP that will receive reply if parse success
         */
        handleInterestPacket: function (packetDataArray, nonPrivateIP, nonPrivatePort) {
            var nameComponent;
            var i;

            for (i = 0; i < packetDataArray.length; i++) {
                if (packetDataArray[i] === "NAME-COMPONENT-TYPE") {

                    // i+2 corresponds name as per NDN standard
                    // i = notifier (NAME-COMPONENT-TYPE), i+1 = bytes, i+2 = name
                    nameComponent = packetDataArray[i+2].split("/"); // split into various components

                } else {
                    // TODO - inspect other packet elements
                }
            }

            // information extracted from our name format:
            // "/ndn/userID/sensorID/timestring/processID/ip"
            // the indexes used are position + 1 (+1 is due to properties)

            var packetUserID = nameComponent[2];
            var packetSensorID = nameComponent[3];
            var packetTimeString = nameComponent[4];
            var packetProcessID = nameComponent[5];
            var packetIP = nameComponent[6];

            console.log("name: " + nameComponent);

            if (packetProcessID === StringConst.INTEREST_FIB) {

                this.handleInterestFIBRequest(packetUserID, packetSensorID, packetIP, nonPrivateIP, nonPrivatePort);

            } else if (packetProcessID === StringConst.INTEREST_CACHE_DATA) {
                console.log("interest cache data");
                this.handleInterestCacheRequest(packetUserID, packetSensorID, packetTimeString, packetProcessID,
                    packetIP, nonPrivateIP, nonPrivatePort);
            } else {
                console.log("else");
                // unknown process id; drop packet
            }
        },

        /**
         * returns entire FIB to user who requested it
         *
         * @param packetUserID userID of entity that requested FIB contents
         * @param packetSensorID sensorID of entity that requested FIB contents
         * @param packetIP ip of entity that requested FIB contents
         * @param nonPrivateIP specifies IP that will receive reply if parse success
         * @param nonPrivatePort specifies IP that will receive reply if parse success
         */
        handleInterestFIBRequest: function (packetUserID, packetSensorID, packetIP, nonPrivateIP, nonPrivatePort)
        {
            var allFIBData = FIB.getAllFIBData();

            if (allFIBData === null || allFIBData.length === 0) {

                // TODO - get own information, and send a data packet with no content

                var dataPacket = new DataPacketClass.DataPacket();
                dataPacket.DataPacket(packetUserID, packetSensorID,
                    csDATA[i].timeString, csDATA[i].processID, csDATA[i].getDataFloat());

                this.sendMessage(dataPacket.createDATA(), packetIP); // reply to interest with DATA from cache

            } else {

                var i;
                for (i = 0; i < allFIBData.length; i++) {

                    // don't send data to same node that requested; check first
                    if (allFIBData[i].ipAddr !== packetIP) {

                        // content returned in format: "userID,userIP"
                        var fibContent = allFIBData[i].userID + "," + allFIBData[i].ipAddr;

                        // TODO - use real time string
                        var dataPacket = DataPacketClass.DataPacket();
                        dataPacket.DataPacket(myUserID, mySensorID,
                            StringConst.CURRENT_TIME,  StringConst.DATA_FIB, fibContent);

                        this.sendMessage(dataPacket.toString(), nonPrivateIP, nonPrivatePort); // send interest packet
                    }
                }
            }
        },

        /**
         * performs NDN logic on packet that requests data
         *
         * @param packetUserID userID associated with requested data from cache
         * @param packetSensorID sensorID associated with requested data from cache
         * @param packetTimeString timeString associated with requested data from cache
         * @param packetProcessID processID associated with requested data from cache
         * @param packetIP ip of entity that requested data from cache
         * @param nonPrivateIP specifies IP that will receive reply if parse success
         * @param nonPrivatePort specifies IP that will receive reply if parse success
         */
        handleInterestCacheRequest: function (packetUserID, packetSensorID, packetTimeString,
                                             packetProcessID, packetIP, nonPrivateIP, nonPrivatePort)
        {
            // first, check CONTENT STORE (cache)

            // TODO - rework with specific data once TIME variable valid!
            var csDATA = CS.getGeneralCSData(packetUserID);//, packetTimeString);

            if (csDATA !== null) {
                console.log("if statement, cache request");
                for (i = 0; i < csDATA.length; i++) {

                    // TODO - again, rework with specific date once TIMEvaris valid

                    var dataPacket = DataPacketClass.DataPacket();
                    dataPacket.DataPacket(csDATA[i].getUserID(), csDATA[i].getSensorID(),
                        csDATA[i].getTimeString(), csDATA[i].getProcessID(), csDATA[i].getDataFloat());

                    this.sendMessage(dataPacket.toString(), nonPrivateIP, nonPrivatePort); // reply to interest with DATA from cache
                }

            } else {

                // second, check PIT

                // TODO - again, rework with specific date once TIME variable valid

                if (PIT.getGeneralPITData(packetUserID, packetIP) === null) {

                    // add new request to PIT, then look into FIB before sending request
                    var newPITEntry = DBDataClass.DATA();
                    newPITEntry.setUserID(packetUserID);
                    newPITEntry.setSensorID(packetSensorID);
                    newPITEntry.setTimeString(packetTimeString);
                    newPITEntry.setProcessID(packetProcessID);
                    newPITEntry.setIpAddr(packetIP);

                    PIT.addPITData(newPITEntry);

                    var allFIBData = FIB.getAllFIBData();

                    if (allFIBData === null || allFIBData.length === 0) {
                        // TODO - sophisticate way in which user deals with FIB

                        // FIB is empty, user must reconfigure
                        throw "Cannot send message; FIB is empty.";
                    } else {

                        var i;
                        for (i = 0; i < allFIBData.length; i++) {

                            // don't send data to same node that requested; check first
                            if (allFIBData[i].ipAddr !== packetIP
                                && allFIBData[i].ipAddr !== "null") {

                                var interestPacket = InterestPacketClass.InterestPacket();
                                interestPacket.InterestPacket(packetUserID, packetSensorID,
                                    packetTimeString,  packetProcessID, packetIP);

                                this.sendMessage(interestPacket.toString(), allFIBData[i].ipAddr); // send interest packet
                            }
                        }
                    }
                } else {

                    // add new request to PIT and wait, request has already been sent
                    var newPITEntry = DBDataClass.DATA();
                    newPITEntry.setUserID(packetUserID);
                    newPITEntry.setSensorID(packetSensorID);
                    newPITEntry.setTimeString(packetTimeString);
                    newPITEntry.setProcessID(packetProcessID);
                    newPITEntry.setIpAddr(packetIP);

                    PIT.addPITData(newPITEntry);
                }
            }
        },

        /**
         * Method returns true if the data interval is within request interval
         *
         * @param requestInterval a request interval; necessarily must contain two times (start and end)
         * @param dataInterval the time stamp on specific data
         * @return determination of whether dataInterval is within requestInterval
         */
        isValidForTimeInterval: function (requestInterval, dataInterval) {

            var requestIntervals = requestInterval.split("\\|\\|"); // split interval into start/end

            // TIME_STRING FORMAT: "yyyy-MM-dd||yyyy-MM-dd"; the former is start, latter is end

            var startDate = new Date(requestIntervals[0]);
            var endDate = new Date(requestIntervals[1]);
            var dataDate = new Date(dataInterval);

            var beforeStartDate = dataDate < startDate;
            var afterEndDate = dataDate > endDate;

            // if dataInterval is not before start and not after end, then its with interval
            return (!beforeStartDate && !afterEndDate);
        },

        /**
         * handles DATA packet as per NDN specification
         * Method parses packet then stores in cache if requested,
         * and sends out to satisfy any potential Interests.
         *
         * @param packetDataArray incoming packet after having been "split" by space and placed array
         */
        handleDataPacket : function (packetDataArray)
        {
            var nameComponent = null;
            var dataContents = null;
            var i;

            for (i = 0; i < packetDataArray.length; i++) {
                if (packetDataArray[i] === "NAME-COMPONENT-TYPE") {
                    // i+2 corresponds name as per NDN standard
                    // i = notifier (NAME-COMPONENT-TYPE), i+1 = bytes, i+2 = name

                    nameComponent = packetDataArray[i+2].trim().split("/"); // split into various components

                } else if (packetDataArray[i] === "CONTENT-TYPE") {

                    // i+2 corresponds content as per NDN standard
                    // i = notifier (CONTENT-TYPE), i+1 = bytes, i+2 = content
                    dataContents = packetDataArray[i+2];
                } else {
                    // TODO - inspect other packet elements
                }
            }

            // information extracted from our name format:
            // "/ndn/userID/sensorID/timestring/processID/floatContent"
            // the indexes used are position + 1 (+1 is due to properties)
            packetUserID = nameComponent[2].trim();
            packetSensorID = nameComponent[3].trim();
            packetTimeString= nameComponent[4].trim();
            packetProcessID = nameComponent[5].trim();
            packetFloatContent = dataContents.trim();

            // first, determine who wants the data
            var allValidPITEntries = PIT.getGeneralPITData(packetUserID, packetTimeString);

            if (allValidPITEntries === null) {

                // no one requested the data, merely drop it
            } else {

                // determine if data packet's time interval matches any requests
                var requestCount = 0;
                for (i = 0; i < allValidPITEntries.length; i++) {

                    if (this.isValidForTimeInterval(allValidPITEntries[i].getTimeString(), packetTimeString)) {
                        requestCount++;
                    }
                }

                if (requestCount > 0) { // positive request count, process packet now

                    if (packetProcessID === StringConst.DATA_FIB) {

                        this.handleFIBData(packetFloatContent);
                    } else if (packetProcessID === StringConst.DATA_CACHE) {

                        this.handleCacheData(packetUserID, packetSensorID, packetTimeString,
                            packetProcessID, packetFloatContent, allValidPITEntries);
                    } else {
                        // unknown process id; drop packet
                    }
                }
            }
        },

        /**
         * Method handles incoming FIB data
         *
         * @param packetFloatContent contents of FIB Data packet (i.e., "userID,userIP" string)
         */
        handleFIBData: function (packetFloatContent) {
            // expected format: "userID,userIP"
            var packetFIBContent = packetFloatContent.split(","); // TODO - don't rely on this assumption

            var data = DBDataClass.DATA();

            data.setUserID(packetFIBContent[0].trim());
            data.setIpAddr(packetFIBContent[1].trim());
            data.setTimeString(StringConst.CURRENT_TIME);

            // don't add data for self here
            if (data.userID !== myUserID) {

                var fibCheckObject = FIB.getFIBData(data.userID);

                if (fibCheckObject === null) {
                    // data packet contains requested fib data, store in fib now

                    FIB.addFIBData(data);
                } else {
                    // data was requested; second, update cache with new packet

                    FIB.updateFIBData(data);
                }
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

            var data = DBDataClass.DATA();

            data.setUserID(packetUserID);
            data.setSensorID(packetSensorID);
            data.setTimeString(packetTimeString);
            data.setProcessID(packetProcessID);
            data.setDataFloat(packetFloatContent);

            // if data exists in cache, just update

            // TODO - again, rework for specific CS data once TIMEvaris valid

            if (CS.getGeneralCSData(packetUserID) !== null) {

                CS.updateCSData(data);
            } else {

                // data not in cache, add now
                CS.addCSData(data);
            }

            // now, send packets to each entity that requested the data
            var i;
            for (i = 0; i < allValidPITEntries.length; i++) {

                // data satisfies PIT entry; delete the entry
                PIT.deletePITEntry(allValidPITEntries[i].userID,
                    allValidPITEntries[i].timeString, allValidPITEntries[i].ipAddr);

                if (allValidPITEntries[i].ipAddr === deviceIP) {
                    // this device requested the data, notify
                    // TODO - notify of reception of requested data

                } else {

                    // NOTE: params list = Context context, vartimestring, processID, varcontent
                    var dataPacket = DataPacketClass.DataPacket();
                    dataPacket.DataPacket(packetUserID,
                        packetSensorID, packetTimeString, packetProcessID, packetFloatContent);

                    this.sendMessage(dataPacket.toString(), allValidPITEntries[i].ipAddr); // send DATA packet
                }
            }
        }
	};
};
