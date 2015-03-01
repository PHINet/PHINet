/** 
 * File contains code for the object
 * that enables UDP communication
 **/

var InterestPacketClass = require('./interestpacket');
var DataPacketClass = require('./datapacket');
var DBDataClass = require('./data');
var ProcessID = require('./processid').ProcessID;

var PIT = require('./pit').PIT();
var FIB = require('./fib').FIB();
var CS = require('./cs').CS();

var NDN_SENSOR_NET_PORT = 50056; // same across all applications
var dgram = require("dgram");
var socket = dgram.createSocket('udp4');

var mySensorID = "SERVER_SENSOR"; // TODO - rework; this isn't applicable to server
var myUserID = "SERVER"; // TODO - rework to find standard ID for server

exports.UDPComm = function() {
	
	return {

		initializeListener : function () {
			socket.bind(NDN_SENSOR_NET_PORT);
			socket.on('message', function(msg, rinfo) {
			  
			  var message = msg.toString('utf8').split(" ");

			  console.log("msg", message);

			  if (message[0] === "INTEREST-TYPE") {
			  	
			  	console.log("handle interest");
			  	handleInterestPacket(message);

			  } else if (message[0] === "DATA-TLV") {
			  	console.log('handle data');
			  	handleDataPacket(message);

			  } else {
			  	// packet type not recognized, drop it
			  }

			});
		}, 

		// TODO - rework so that it only appears once
		// function appears twice; once for different scopes
		sendMessage: function (message, ip) {

			console.log("outgoingmessage: ", message);

			var buffer = new Buffer(message, "utf-8");
			console.log("buffer created!");
			socket.send(buffer, 0, buffer.length, NDN_SENSOR_NET_PORT, 
				ip, function(err) {
					console.log("message sending error: ", err);
			});

			console.log("sent: ", buffer);
		}
	};
}


function sendMessage (message, ip) {

	console.log("outgoingmessage: ", message);
	socket.send(message, 0, message.length, NDN_SENSOR_NET_PORT, 
		ip, function(err) {
	});
}

/** handles INTEREST packet as per NDN specification
* Method parses packet then asks the following questions:
* 1. Do I have the data?
* 2. Have I already sent an interest for this data?
*/
function handleInterestPacket(packetDataArray) {
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
	console.log('after loop');

	// information extracted from our name format:
	// "/ndn/userID/sensorID/timestring/processID/ip"
	// the indexes used are position + 1 (+1 is due to properties)

	console.log("name component: ", nameComponent);

	var packetUserID = nameComponent[2];
	var packetSensorID = nameComponent[3];

	// TODO - is this order right? it appears to be but doesn't work with format; CHECK

	packetProcessID = nameComponent[4];
	packetTimeString = nameComponent[5];
	packetIP = nameComponent[6];

	if (packetProcessID === ProcessID.REQUEST_FIB) {
		console.log("in if statement1");
	    handleInterestFIBRequest(packetUserID, packetSensorID, packetTimeString, packetProcessID,
	            packetIP);

	} else {
	    // otherwise, assume cache data is requested
	    // TODO - rework so that others can be specified
	    console.log('in if statement2');
	    handleInterestCacheRequest(packetUserID, packetSensorID, packetTimeString, packetProcessID,
	            packetIP);
	}
}

/** returns entire FIB to user who requested it **/
function handleInterestFIBRequest(packetUserID, packetSensorID, packetTimeString,
                  packetProcessID, packetIP)
{

	console.log('interest fib req here');

	var allFIBData = FIB.getAllFIBData();

	if (allFIBData === null || allFIBData.length === 0) {

		console.log("in if statement");
	    // TODO - get own information, and send a data packet with no content
	    
	    var dataPacket = new DataPacketClass.DataPacket();
	    dataPacket.DataPacket(packetUserID, packetSensorID,
	            csDATA[i].timeString, csDATA[i].processID, csDATA[i].getDataFloat());

	    sendMessage(dataPacket.createDATA(), packetIP); // reply to interest with DATA from cache

	} else {

		console.log("all fib data: ", allFIBData);

		var i;
	    for (i = 0; i < allFIBData.length; i++) {

	        // don't send data to same node that requested; check first
	        if (allFIBData[i].ipAddr !== packetIP) {

	            // content returned in format: "userID,userIP"
	            var fibContent = allFIBData[i].userID + "," + allFIBData[i].ipAddr;

	            // TODO - use real time string
	            var dataPacket = DataPacketClass.DataPacket();
	            dataPacket.DataPacket(myUserID, mySensorID,
	                    "NOW",  ProcessID.FIB_DATA, fibContent);

	           	console.log('dataPacket', dataPacket);
	            sendMessage(dataPacket.toString(), packetIP); // send interest packet
	        }
	    }
	}
}

/** performs NDN logic on packet that requets data **/
function handleInterestCacheRequest (packetUserID, packetSensorID, packetTimeString,
                    packetProcessID, packetIP)
{
	// first, check CONTENT STORE (cache)

	// TODO - rework with specific data once TIMEvaris valid!
	var csDATA = CS.getGeneralCSData(packetUserID);//, packetTimeString);

	console.log("CS data: ", csDATA);

	if (csDATA !== null) {
	    // NOTE: params list = Context context, vartimestring, processID, varcontent

	    console.log("FIRST IF, cache request");
	    var i;
	    for (i = 0; i < csDATA.length; i++) {

	        // TODO - again, rework with specific date once TIMEvaris valid

	        var dataPacket = DataPacketClass.DataPacket();
	        dataPacket.DataPacket(csDATA[i].getUserID(), csDATA[i].getSensorID(),
	                csDATA[i].getTimeString(), csDATA[i].getProcessID(), csDATA[i].getDataFloat());

	        sendMessage(dataPacket.toString(), packetIP); // reply to interest with DATA from cache
	    }

	} else {

		console.log("FIRST ELSE, cache request");

	    // second, check PIT

	    // TODO - again, rework with specific date once TIMEvaris valid

	    if (PIT.getGeneralPITData(packetUserID, packetIP) === null) {

	    	console.log("SECOND IF, cache request");
	        // add new request to PIT, then look into FIB before sending request
	        var newPITEntry = DBDataClass.DATA();
	        newPITEntry.setUserID(packetUserID);
	        newPITEntry.setSensorID(packetSensorID);
	        newPITEntry.setTimeString(packetTimeString);
	        newPITEntry.setProcessID(packetProcessID);
	        newPITEntry.setIpAddr(packetIP);

	        PIT.addPITData(newPITEntry);

	        // TODO - access FIB intelligently
	        
	        var nextHopIP;

	        // first check for actual source in FIB, then send out broadly
	        /*DBData fibDATA = new FIB.getFIBData(packetUserID);
	        if (fibDATA == null) {
	            ArrayList<DBData> allFIBData = FIB.getAllFIBData();
	        } else {
	            nextHopIP = fibDATA.ipAddr;
	        }*/

	        var allFIBData = FIB.getAllFIBData();

	        if (allFIBData === null || allFIBData.length === 0) {
	        	console.log("THIRD IF, cache request");
	            // TODO - sophisticate way in which user deals with FIB

	            // FIB is empty, user must reconfigure
	            throw "Cannot send message; FIB is empty.";
	        } else {

	        	console.log("SECOND ELS, cache request");
	        	var i;
	            for (i = 0; i < allFIBData.length; i++) {

	                // don't send data to same node that requested; check first
	                if (allFIBData[i].ipAddr !== packetIP
	                        && allFIBData[i].ipAddr !== "null") {

	                    var interestPacket = InterestPacketClass.InterestPacket();
	                	interestPacket.InterestPacket(packetUserID, packetSensorID,
	                            packetTimeString,  packetProcessID, packetIP);

	                	sendMessage(interestPacket.toString(), allFIBData[i].ipAddr); // send interest packet
	                }
	            }
	        }
	    } else {

	    	console.log("THIRD ELSE, cache request");
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
}

/** handles DATA packet as per NDN specification
* Method parses packet then asks the following questions:
* 1. Is this data for me?
*/
function handleDataPacket (packetDataArray)
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
	packetTimevar= nameComponent[4].trim();
	packetProcessID = nameComponent[5].trim();

	// TODO - packet structure (floatContent inclusion, specifically)
	packetFloatContent = dataContents.trim();//nameComponent[5];

	// first, determine who wants the data
	var allValidPITEntries = PIT.getGeneralPITData(packetUserID, packetTimeString);

	// TODO - fix this logic, redo later
	if (false) {//allValidPITEntries == null || allValidPITEntries.length == 0) {


	    // no one requested the data, merely drop it
	} else {
	    // data was requested; second, update cache with new packet
	    var data = DBDataClass.DATA();

	    // TODO - user helper methods for this

	    if (packetProcessID === ProcessID.FIB_DATA) {

	        // data packet contains requested fib data, store in fib now

	        // expected format: "userID,userIP"
	        var packetFIBContent = packetFloatContent.split(","); // TODO - don't rely on this assumption

	        data.setUserID(packetFIBContent[0].trim());
	        data.setIpAddr(packetFIBContent[1].trim());
	        data.setTimeString("NOW"); // TODO - rework with real time

	        // don't add data for self here
	        if (data.userID !== myUserID) {

	            var fibCheckObject = FIB.getFIBData(data.userID);

	            if (fibCheckObject === null) {
	                FIB.addFIBData(data);
	            } else {
	                FIB.updateFIBData(data);
	            }
	        }

	    } else {

	        data.setUserID(packetUserID);
	        data.setSensorID(packetSensorID);
	        data.setTimeString(packetTimeString);
	        data.setProcessID(packetProcessID);
	        data.setDataFloat(packetFloatContent);
	        // TODO - change assumption that all other packets are cache data

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

	               	sendMessage(dataPacket.toString(), allValidPITEntries[i].ipAddr); // send DATA packet
	            }
	        }
	    }
	}
}






