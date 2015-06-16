/** 
 * File contains code for the object that enables UDP communication
 */

var DBData = require('./data'); // used to create objects used by the database
var StringConst = require('./string_const').StringConst;

var PIT, FIB, CS, USER_CREDENTIALS;

var dgram = require("dgram"); // Node.js udp socket module
var socket = dgram.createSocket('udp4');
var utils = require('./utils.js').Utils;
var analytics = require('./analytics.js').analytics();

var NDN_SENSOR_NET_PORT = 50056; // same across all applications
var mySensorID = "SERVER_SENSOR"; // TODO - rework; this isn't applicable to server
var myUserID = "CLOUD-SERVER"; // TODO - rework to find standard ID for server

var ndnjs_utils = require('./ndnjs_utils.js').ndn_utils;
var Data = require('./ndn-js/data.js').Data;
var Interest = require('./ndn-js/interest.js').Interest;
var Name = require('./ndn-js/name.js').Name;

var recentLoginValidations = []; // TODO - replace this temporary data structure
var recentRegisterValidations = []; // TODO - replace this temporary data structure

/**
 * Returns object that handles majority of UDP communication. References to the core
 * NDN databases are passed to the module so that relevant queries can be performed.
 *
 * @param pitReference - reference to the PIT database
 * @param fibReference - reference to the FIB database
 * @param csReference - reference to the CS database
 * @param ucReference - reference to the USER_CREDENTIAL database
 */
exports.UDPComm = function(pitReference, fibReference, csReference, ucReference) {

    PIT = pitReference;
    FIB = fibReference;
    CS = csReference;
    USER_CREDENTIALS = ucReference;

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

                console.log("message found: " + msg);

                try {
                    // attempt to create both Interest and Data packet; only one should be valid
                    var interest = ndnjs_utils.decodeInterest(msg);
                    var data = ndnjs_utils.decodeData(msg);

                    if (interest && !data) {
                         console.log("interest packet found");

                        // Interest packet detected
                        handleInterestPacket(interest, rinfo.address, rinfo.port);
                    } else if (!interest && data) {
                        console.log("data packet found");

                        // Data packet detected
                        handleDataPacket(data, rinfo.address);
                    } else {
                        // unknown packet type; drop it
                    }
                } catch (e) {
                    console.log("Something went wrong. Unable to parse packet. Error: " + e);
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

            var buffer = message.buffer; // message param is either encoded Interest or Data object; get its buffer

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

            console.log("interest name: " + nameComponent);

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
            }
            // a client application requests login; initiate process now
            else if (packetProcessID === StringConst.LOGIN_REQUEST) {

                handleInterestLoginRequest(packetUserID, packetIP, packetPort);
            }
            // a client application requests register; initiate process now
            else if (packetProcessID === StringConst.REGISTER_REQUEST) {

                handleInterestRegisterRequest(packetUserID, packetIP, packetPort);
            }
            // a client requests result of login
            else if (packetProcessID === StringConst.INTEREST_LOGIN_RESULT) {

                handleLoginResultRequest(packetUserID, packetIP, packetPort);
            }
            // a client requests the result of registration
            else if (packetProcessID === StringConst.INTEREST_REGISTER_RESULT) {

                handleRegisterResultRequest(packetUserID, packetIP, packetPort);
            }
            // client requests mode analytic task on data
            else if (packetProcessID === StringConst.MODE_ANALYTIC) {

                handleModeAnalyticTask(packetUserID, packetSensorID, packetTimeString, packetIP, packetPort);
            }
            // client requests median analytic task on data
            else if (packetProcessID === StringConst.MEDIAN_ANALYTIC) {
                handleMedianAnalyticTask(packetUserID, packetSensorID, packetTimeString, packetIP, packetPort);
            }
            // client requests mean analytic task on data
            else if (packetProcessID === StringConst.MEAN_ANALYTIC) {
                handleMeanAnalyticTask(packetUserID, packetSensorID, packetTimeString, packetIP, packetPort);
            }
            // client requests initialization of sync request
            else if (packetProcessID === StringConst.INITIATE_SYNCH_REQUEST) {

                handleSynchRequest(packetUserID, packetSensorID, packetTimeString, packetIP, packetPort);
            } else  {
                // unknown process id; drop packet
            }
        },

        /**
         * handles DATA packet as per NDN specification
         * Method parses packet then stores in cache if requested,
         * and sends out to satisfy any potential Interests.
         *
         * @param dataPacket incoming ndn-js Data packet after having been decoded
         * @param hostIP used to query PIT for previous entries
         */
        handleDataPacket: function (dataPacket, hostIP) {

            // decode paring characters "||" and then split into array for further parsing
            var nameComponent = dataPacket.getName().toUri().replace("%7C%7C", "||").split("/");
            var dataContents = dataPacket.getContent().toString();

            // information extracted from our name format:
            // "/ndn/userID/sensorID/timeString/processID"
            // the indexes used are position + 1 (the addition 1 due to empty string in 0-index)
            var packetUserID = nameComponent[2];
            var packetSensorID = nameComponent[3];
            var packetTimeString = nameComponent[4];
            var packetProcessID = nameComponent[5];

            console.log("before general pit data, name: " + nameComponent);

            // first, determine who wants the data
            PIT.getGeneralPITData(packetUserID, hostIP, function(rowsTouched, allValidPITEntries) {

                console.log("length of returned pit entries: " + allValidPITEntries);

                if (allValidPITEntries === null) {

                    // no one requested the data, merely drop it
                } else {

                    // determine if data packet's time interval matches any requests
                    var requestFoundWithinInterval = false;
                    for (var i = 0; i < allValidPITEntries.length; i++) {

                        if (utils.isValidForTimeInterval(allValidPITEntries[i].getTimeString(), packetTimeString)) {
                            requestFoundWithinInterval = true;
                            break;
                        }

                        if ((packetProcessID === StringConst.LOGIN_CREDENTIAL_DATA || packetProcessID === StringConst.REGISTER_CREDENTIAL_DATA)
                            && allValidPITEntries[i].getProcessID() === StringConst.CREDENTIAL_REQUEST) {

                            /**
                             * login/register packets (currently) are valid irrespective of time, break if match found
                             * server sends an Interest with processID CREDENTIAL_REQUEST and client responds with
                             * Data with processID LOGIN_CREDENTIAL_DATA/REGINSTER_CREDENTIAL_DATA
                             */

                            requestFoundWithinInterval = true;
                            break;
                        }

                        if (packetProcessID === StringConst.SYNCH_DATA_REQUEST
                            && allValidPITEntries[i].getTimeString() === packetTimeString) {

                            /**
                             * TODO - doc here (the user ids won't match and both time strings are intervals)
                             */

                            requestFoundWithinInterval = true;
                            break;
                        }
                    }

                    if (requestFoundWithinInterval) { // positive request count, process packet now

                        // check if DATA packet contains FIB data
                        if (packetProcessID === StringConst.DATA_FIB) {

                            handleFIBData(dataContents);
                        }
                        // check if DATA packet contains CACHE data
                        else if (packetProcessID === StringConst.DATA_CACHE) {

                            handleCacheData(packetUserID, packetSensorID, packetTimeString,
                                packetProcessID, dataContents, allValidPITEntries);
                        }
                        // client has sent login credentials to server for validation
                        else if (packetProcessID === StringConst.LOGIN_CREDENTIAL_DATA) {

                            handleLoginData(dataContents);
                        }
                        // client has sent register credentials to server for validation
                        else if (packetProcessID === StringConst.REGISTER_CREDENTIAL_DATA) {

                             handleRegisterData(dataContents);
                        }
                        // client has responded to server's synchronization request
                        else if (packetProcessID === StringConst.SYNCH_DATA_REQUEST) {

                            handleSynchRequestData(packetUserID, packetTimeString, dataContents);
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

    var buffer = message.buffer; // message param is either encoded Interest or Data object; get its buffer

    if (port == undefined || port === null) {
        port = NDN_SENSOR_NET_PORT;
    }

    console.log("sending: " + message);

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
 * Method handles login user-credential Data packet sent by client. If credentials
 * match user in database, then store success so that it can be queried.
 *
 * @param dataContents of user-credential Data packet
 */
function handleLoginData(dataContents) {

    if (dataContents) {

        // syntax is "userid,password"
        var loginCredentials = dataContents.split(",");

        var userID = loginCredentials[0].trim();
        var password = loginCredentials[1].trim();

        USER_CREDENTIALS.getUser(userID, function(rowCount, queryResult) {

            // only one row (user) should have been found and returned
            if (rowCount === 1 && queryResult) {

                utils.comparePassword(password, queryResult.getPassword(),
                    function (err, isPasswordMatch) {

                        // login successful
                        if (isPasswordMatch) {

                            // store result now so that user can query it
                            recentLoginValidations.push({"userID": userID, "password": password});
                        }
                        // login unsuccessful
                        else {
                            // store nothing; when user queries, they will get a null reply
                        }
                    });

            } else {
                // user was not found in database; login unsuccessful
            }
        });
    } else {
        // user-credential packet contained no content; do nothing
    }
}

/**
 * Method handles register user-credential Data packet sent by client. If
 * credentials match user in database, then store success so that it can be queried.
 *
 * If no user exists in database (a successful register), create entry for new user.
 *
 * @param dataContents
 */
function handleRegisterData(dataContents) {

    if (dataContents) {

        // syntax is "userid,password,email"
        var registerCredentials = dataContents.split(",");

        var userID = registerCredentials[0].trim();
        var password = registerCredentials[1].trim();
        var email = registerCredentials[2].trim();

        // TODO - handle email

        USER_CREDENTIALS.getUser(userID, function(rowCount, queryResult) {

            // register unsuccessful
            if (rowCount === 1 && queryResult) {
                // this username already exists
            }
            // register successful
            else {

                var entityType = StringConst.PATIENT_ENTITY; // TODO - fix this default value

                // place user in database
                USER_CREDENTIALS.insertNewUser(userID, password, email, entityType,
                    function(rowsTouched) {

                        if (rowsTouched === 1) {
                            // insertion successful; store result now so that user can query it
                            recentRegisterValidations.push({"userID": userID, "password": password, "email": email});
                        } else {
                            // insert unsuccessful

                            // TODO - notify user; handle this error
                        }
                });
            }
        });
    } else {
        // user-credential packet contained no content; do nothing
    }
}

/**
 *
 * Syntax: Sensor1--data1,time1;; ... ;;dataN,timeN:: ... ::SensorN--data1,time1;; ... ;;dataN,timeN
 *
 * TODO - doc
 *
 * @param userID
 * @param timeString
 * @param dataContents
 */
function handleSynchRequestData(userID, timeString, dataContents) {

    console.log("handling synch data");

    var parsedSynchData = utils.parseSynchData(userID, dataContents);

    if (parsedSynchData) {

        console.log("storing synch data");

        for (var i = 0; i < parsedSynchData.length; i++) {
            console.log(i + " : " + CS.insertCSData(parsedSynchData[i], function(){}));
        }
    } else {
        console.log("parse synch data returned null: FAILURE")
    }
}

/**
 * Returns entire FIB to user who requested it
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

    console.log("inside interest cache data");

    // first, check CONTENT STORE (cache) for requested information
    CS.getGeneralCSData(packetUserID, function(rowsTouched, csQueryResults) {

        // data was in cache; send to requester
        if (csQueryResults) {
            console.log("query results found");

            for (var i = 0; i < csQueryResults.length; i++) {

                // TODO - again, rework with specific date once TIME_STRING valid;
                // TODO - remove loop and send as single unit

                // create and send packet with ndn-js module
                var packetName = ndnjs_utils.createName(csQueryResults[i].getUserID(), csQueryResults[i].getSensorID(),

                    // TODO - fix this

                    csQueryResults[i].getTimeString(), "DATA_CACHE");//csQueryResults[i].getProcessID());

                var data = ndnjs_utils.createDataPacket(csQueryResults[i].getDataFloat(), packetName);
                var encodedPacket = data.wireEncode();

                sendMessage(encodedPacket, packetIP, packetPort); // reply to interest with DATA from cache
            }

        }
        // data wasn't in cache; check PIT to see if an interest has already been sent for data
        else {

            console.log("no data found, adding to pit");
            // TODO - again, rework with specific date once TIME variable valid

            var newPITEntry;

            PIT.getGeneralPITData(packetUserID, packetIP, function(rowsTouched, queryResults) {

                // query returned NULL: no INTEREST sent yet; do so now
                if (!rowsTouched || !queryResults) {

                    // add new request to PIT, then look into FIB before sending request
                    newPITEntry = DBData.DATA();
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
                    newPITEntry = DBData.DATA();
                    newPITEntry.pitData(packetUserID, packetSensorID, packetProcessID, packetTimeString, packetIP);

                    PIT.insertPITData(newPITEntry, function(){});
                }
            });
        }
    });
}

/**
 * Begins client-login procedure.
 *
 * @param clientID of client who requested login
 * @param hostIP of client who requested login
 * @param hostPort of client who requested login
 */
function handleInterestLoginRequest(clientID, hostIP, hostPort) {

    /**
     * Due to the nature of NDN, the client must first send login Interest to
     * server (because the server is the only one who can validate such requests). The
     * server will then reply with a blank Data and, shortly, an Interest requesting
     * login credentials, and the client will then reply with a Data packet containing
     * them. The client then sends an Interest to the server querying for the result,
     * to which the server replies with a Data packet. If the results are positive,
     * the client has logged in; otherwise, login failed.
     */

    var time = utils.getCurrentTime();

    var packetName = ndnjs_utils.createName(clientID, StringConst.NULL_FIELD,
        time, StringConst.CREDENTIAL_REQUEST);
    var interest = ndnjs_utils.createInterestPacket(packetName);

    var encodedPacket = interest.wireEncode();

    sendMessage(encodedPacket, hostIP, hostPort);

    // TODO - improve the way this entry is created

    // add request to PIT
    var newPITEntry = DBData.DATA();

    newPITEntry.pitData(clientID, StringConst.NULL_FIELD, StringConst.CREDENTIAL_REQUEST, time, hostIP);
    PIT.insertPITData(newPITEntry, function(){});
}

/**
 * Begins client-register procedure.
 *
 * @param clientID of client who requested register
 * @param hostIP of client who requested register
 * @param hostPort of client who requested register
 */
function handleInterestRegisterRequest(clientID, hostIP, hostPort) {

    /**
     * Due to the nature of NDN, the client must first send signup Interest to
     * server (because the server is the only one who can process such requests). The
     * server will then reply with a blank Data and, shortly, an Interest requesting
     * user credentials, and the client will then reply with a Data packet containing
     * them. The client then sends an Interest to the server querying for the result,
     * to which the server replies with a Data packet. If the results are positive,
     * the client has registered; otherwise, register failed.
     */

    handleInterestLoginRequest(clientID, hostIP, hostPort); // reuse function
}

/**
 * Handles user's query as to whether login was success; if found,
 * replies with previously-stored login result then deletes it from array.
 *
 * @param packetUserID of querying user
 * @param hostIP of querying user
 * @param hostPort of querying user
 */
function handleLoginResultRequest(packetUserID, hostIP, hostPort) {
    handleResultRequest(packetUserID, hostIP, hostPort, StringConst.LOGIN_REQUEST);
}

/**
 * Handles user's query as to whether register was success; if found,
 * replies with previously-stored register result then deletes it from array.
 *
 * @param packetUserID of querying user
 * @param hostIP of querying user
 * @param hostPort of querying user
 */
function handleRegisterResultRequest(packetUserID, hostIP, hostPort) {
    handleResultRequest(packetUserID, hostIP, hostPort, StringConst.REGISTER_REQUEST);
}

/**
 * Handles user's query as to whether register/login was success; if found,
 * replies with previously-stored register result then deletes it from array.
 *
 * @param packetUserID of querying user
 * @param hostIP of querying user
 * @param hostPort of querying user
 * @param requestType (i.e., either LOGIN_REQUEST or REGISTER_REQUEST)
 */
function handleResultRequest(packetUserID, hostIP, hostPort, requestType) {
    var userValidationFound = false;
    var recentValidations;

    if (requestType === StringConst.REGISTER_REQUEST) {
        recentValidations = recentRegisterValidations;
    } else {
        // only requestType left is login
        recentValidations = recentLoginValidations;
    }

    for (var i = 0; i < recentValidations.length; i++) {
        if (recentValidations[i].userID === packetUserID) {
            recentValidations.splice(i, 1); // remove element
            userValidationFound = true;

            break;
        }
    }

    var packetName;

    // client's login/register has been validated; reply with portion if FIB (refer to paper for more information)
    if (userValidationFound) {

        /**
         * TODO - doc use of packetUserID in place of (otherwise null) sensorID
         */

        if (requestType === StringConst.REGISTER_REQUEST) {
            // set process id to DATA_REGISTER_RESULT
            packetName = ndnjs_utils.createName(StringConst.SERVER_ID, packetUserID,
                utils.getCurrentTime(), StringConst.DATA_REGISTER_RESULT);
        } else {
            // only requestType left is login; set process id to DATA_LOGIN_RESULT
            packetName = ndnjs_utils.createName(StringConst.SERVER_ID, packetUserID,
                utils.getCurrentTime(), StringConst.DATA_LOGIN_RESULT);
        }

        FIB.getAllFIBData(function(rowsTouched, queryResult) {

            // TODO - perform appropriate processing here (choose only certain results)
            // TODO - add this user (themselves) to the FIB
            var fibCAP = 10; // TODO - set to appropriate cap
            var packetContent = "  "; // TODO - revise

            if (rowsTouched > 0 && queryResult) {
                for (var i = 0; i < queryResult.length; i++) {

                    if (i > fibCAP) {
                        break;
                    }

                    // syntax is "userid,ipaddr" and "||" separates each entry
                    packetContent += queryResult[i].getUserID() + "," + queryResult[i].getIpAddr() + "||";
                }
            } else {
                // TODO - handle this case
            }

            var data = ndnjs_utils.createDataPacket(packetContent, packetName);
            var encodedPacket = data.wireEncode();

            sendMessage(encodedPacket, hostIP, hostPort);
        });
    }
    // client's login/register has not been validated; reply with an empty data packet
    else {

        var data;
        if (requestType === StringConst.REGISTER_REQUEST) {
            // set process id to DATA_REGISTER_RESULT
            packetName = ndnjs_utils.createName(StringConst.SERVER_ID, StringConst.NULL_FIELD,
                utils.getCurrentTime(), StringConst.DATA_REGISTER_RESULT);

            data = ndnjs_utils.createDataPacket(StringConst.REGISTER_FAILED, packetName);
        } else {
            // only requestType left is login; set process id to DATA_LOGIN_RESULT
            packetName = ndnjs_utils.createName(StringConst.SERVER_ID, StringConst.NULL_FIELD,
                utils.getCurrentTime(), StringConst.DATA_LOGIN_RESULT);

            data = ndnjs_utils.createDataPacket(StringConst.LOGIN_FAILED, packetName);
        }

        var encodedPacket = data.wireEncode();
        sendMessage(encodedPacket, hostIP, hostPort);
    }
}

/**
 * Performs requested mode analytic task and sends result to requestor.
 *
 * @param userID of data set on which to perform mode
 * @param sensorID of data set on which to perform mode
 * @param timeString of data set on which to perform mode
 * @param hostIP of user sending request
 * @param hostPort of user sending request
 */
function handleModeAnalyticTask(userID, sensorID, timeString, hostIP, hostPort) {

    getRequestedData(userID, sensorID, timeString, function(requestedData) {

        // data exists, perform mean
        if (requestedData && requestedData.length > 0) {

            var packetName = ndnjs_utils.createName(userID, sensorID,
            timeString, StringConst.MODE_ANALYTIC);

            var modeValue = analytics.mode(requestedData).toString();

            var data = ndnjs_utils.createDataPacket(modeValue, packetName);
            var encodedPacket = data.wireEncode();

            sendMessage(encodedPacket, hostIP, hostPort);
        }
    });
}

/**
 * Performs requested median analytic task and sends result to requestor.
 *
 * @param userID of data set on which to perform median
 * @param sensorID of data set on which to perform median
 * @param timeString of data set on which to perform media
 * @param hostIP of user sending request
 * @param hostPort of user sending request
 */
function handleMedianAnalyticTask(userID, sensorID, timeString, hostIP, hostPort) {

    getRequestedData(userID, sensorID, timeString, function(requestedData) {

        // data exists, perform mean
        if (requestedData && requestedData.length > 0) {

            var packetName = ndnjs_utils.createName(userID, sensorID,
            timeString, StringConst.MEDIAN_ANALYTIC);

            var medianValue = analytics.median(requestedData).toString();

            var data = ndnjs_utils.createDataPacket(medianValue, packetName);
            var encodedPacket = data.wireEncode();

            sendMessage(encodedPacket, hostIP, hostPort);
        }
    });
}

/**
 * Performs requested mean analytic task and sends result to requestor.
 *
 * @param userID of data set on which to perform mean
 * @param sensorID of data set on which to perform mean
 * @param timeString of data set on which to perform mean
 * @param hostIP of user sending request
 * @param hostPort of user sending request
 */
function handleMeanAnalyticTask(userID, sensorID, timeString, hostIP, hostPort) {

    getRequestedData(userID, sensorID, timeString, function(requestedData) {

        // data exists, perform mean
        if (requestedData && requestedData.length > 0) {
            var packetName = ndnjs_utils.createName(userID, sensorID,
                timeString, StringConst.MEAN_ANALYTIC);

            var meanValue = analytics.mean(requestedData).toString();

            var data = ndnjs_utils.createDataPacket(meanValue, packetName);
            var encodedPacket = data.wireEncode();

            sendMessage(encodedPacket, hostIP, hostPort);
        }
    });
}

/**
 * Returns any data found given input params.
 *
 * @param userID of requested data
 * @param sensorID of requested data
 * @param timeString of requested data
 * @param callback used to pass back requested data
 */
function getRequestedData(userID, sensorID, timeString, callback) {

    CS.getGeneralCSData(userID, function(rowsTouched, queryResults) {

        if (rowsTouched > 0 && queryResults) {
            // TODO - improve upon this naive data-matching
            var matchingData = [];

            console.log("looping for requested data");
            for (var i = 0; i < queryResults.length; i++) {

                if (queryResults[i].getSensorID() === sensorID
                    && utils.isValidForTimeInterval(timeString, queryResults[i].getTimeString())) {

                    // match found, place in array to be returned to caller function
                    matchingData.push(parseInt(queryResults[i].getDataFloat()));

                    console.log("adding data");
                }
            }

            callback(matchingData);
        } else {

            callback(null); // no data found; drop
        }
    });
}

/**
 * Initiates synchronization request by sending Interest
 * to client requesting data within timeString interval.
 *
 * Here the userID was placed in sensorID position; the sensorID is null
 * (because no sensorID is used when we send a synch) and we need to include
 * the userID - so we place the userID as a sensorID.
 *
 * @param userID of client requesting synch request
 * @param sensorID here is actually the userID
 * @param timeString of data that should be synched
 * @param hostIP of client requesting synch request
 * @param hostPort of client requesting synch request
 */
function handleSynchRequest(userID, sensorID, timeString, hostIP, hostPort) {

    console.log("sending interest for synch request, timestring: " + timeString);

    // create and send packet with ndn-js module
    var packetName = ndnjs_utils.createName(sensorID, StringConst.NULL_FIELD, timeString, StringConst.SYNCH_DATA_REQUEST);
    var interest = ndnjs_utils.createInterestPacket(packetName);

    var encodedPacket = interest.wireEncode();

    // ask for synch data from requestor via INTEREST packet
    sendMessage(encodedPacket, hostIP, hostPort);

    // add request to PIT
    var newPITEntry = DBData.DATA();

    newPITEntry.pitData(sensorID, StringConst.NULL_FIELD, StringConst.SYNCH_DATA_REQUEST, timeString, hostIP);
    PIT.insertPITData(newPITEntry, function(){});
}