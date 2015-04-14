/** 
 * File contains code for that functions as "main"
 * segment of execution for this web application
 **/

var request = require('request'); // handles POST
var StringConst = require('./string_const').StringConst;
var express = require('express')
var udp_comm = require('./udp_comm').UDPComm();
var http = require('http');
var LoginDB = require('./usercredentials.js').LoginCredentials();

var bodyParser = require('body-parser'); // allows easy form submissions

udp_comm.initializeListener();

var app = express()
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: true
}));

app.set('port',  process.env.PORT || 3000);
app.use(express.static(__dirname));

app.get('/', function (req, res) {
  res.sendFile('/public/templates/index.html', { root: __dirname })
});

app.get('/login', function (req, res) {
  res.sendFile('/public/templates/login.html', { root: __dirname })
});

app.get('/signup', function (req, res) {
  res.sendFile('/public/templates/signup.html', { root: __dirname })
});

app.get('/document', function (req, res) {
  res.sendFile('/public/templates/document.html', { root: __dirname })
});

app.get('/contact', function (req, res) {
  res.sendFile('/public/templates/contact.html', { root: __dirname })
});

app.get('/profile', function (req, res) {
  res.sendFile('/public/templates/profile.html', { root: __dirname })
});

app.get('/test', function (req, res) {
    res.sendFile('/public/templates/test.html', { root: __dirname })
});

app.get('*', function(req, res){
    res.status(404).sendFile('/public/templates/404.html', { root: __dirname });
});

app.post('/loginAction', function(req, res) {

    // NOTE: temporary redirect to invalid page
    res.status(404).sendFile('/public/templates/404.html', { root: __dirname });

   /* LoginDB.getUser(req.body.user_name, function(rowsTouched, queryResults){

        // checks to see if user already exists and password matches
        if (rowsTouched === 1 && queryResults.getPassword() === req.body.user_password) {

            // notify user of successful login
            request.post(
                'http://ndn-healthnet.elasticbeanstalk.com/login',
                { form: { key: 'value' } },
                function (error, response, body) {
                    if (!error && response.statusCode == 200) {
                        console.log(body)
                    }
                }
            );

            // TODO - notify user of successful login
        } else {
            // TODO - notify user of unsuccessful login

            // notify user of unsuccessful login
            request.post(
                'http://ndn-healthnet.elasticbeanstalk.com/login',
                { form: { key: 'value' } },
                function (error, response, body) {
                    if (!error && response.statusCode == 200) {
                        console.log(body)
                    }
                }
            );
        }

    });
    */
    // TODO - at end, redirect user

});

app.post('/registerAction', function(req, res) {

    // NOTE: temporary redirect to invalid page
    res.status(404).sendFile('/public/templates/404.html', { root: __dirname });

    // check that passwords match
   /* if (req.body.user_password[0] === req.body.user_password[1]) {

        // attempt to submit user

        // TODO - perform input validation on email, password, name, and entity

        var userType = "";
        if (req.body.user_type === 'p') {
            userType = StringConst.PATIENT_ENTITY;
        } else {

            // only valid type remaining is DOCTOR
            userType = StringConst.DOCTOR_ENTITY;
        }

        LoginDB.insertNewUser(req.body.user_name, req.body.user_password, req.body.user_email, userType,

            function(rowsTouched) {

                if (rowsTouched === 1) {
                    // TODO - notify user of successful register

                    // notify user of successful register
                    request.post(
                        'http://ndn-healthnet.elasticbeanstalk.com/signup',
                        { form: { key: 'value' } },
                        function (error, response, body) {
                            if (!error && response.statusCode == 200) {
                                console.log(body)
                            }
                        }
                    );
                } else {
                    // TODO - notify user of bad input

                    // notify user of bad input
                    request.post(
                        'http://ndn-healthnet.elasticbeanstalk.com/signup',
                        { form: { key: 'value' } },
                        function (error, response, body) {
                            if (!error && response.statusCode == 200) {
                                console.log(body)
                            }
                        }
                    );
                }
        });

    } else {
        // TODO - notify user of password mismatch

        // notify user of password mismatch
        request.post(
            'http://ndn-healthnet.elasticbeanstalk.com/signup',
            { form: { key: 'value' } },
            function (error, response, body) {
                if (!error && response.statusCode == 200) {
                    console.log(body)
                }
            }
        );
    }*/

    // TODO - at end, redirect user

});

app.post('/contactAction', function(req, res) {
    console.dir(req.body);
    // TODO - handle contact

    // TODO - at end, redirect user

    // NOTE: temporary redirect to invalid page
    res.status(404).sendFile('/public/templates/404.html', { root: __dirname });
});

// ---- Code Tests UDP Functionality ---

// TODO - implement the testing portion of site

//var DataPacketClass = require('./datapacket');
//var InterestPacketClass = require('./interestpacket');

// method allows user to test networking functionality
/*app.post('/submitIP', function(req, res) {

  if (req.body.user.ipAddrPing !== undefined) {
    // user requested ping
  
    var sys = require('sys')
    var exec = require('child_process').exec;

    function puts(error, stdout, stderr) { 
      console.log(stdout);
    }

    exec("ping -c 3 " + req.body.user.ipAddrPing, puts);

  } else if (req.body.user.ipAddrTrace !== undefined) {
    // user requested traceroute

    var traceroute = require('traceroute');

    traceroute.trace(req.body.user.ipAddrTrace, 
      function (err,hops) {
          if (!err) { 
            console.log(hops); 
          } else {
            console.log("error: " + err);
          }
    });

  } else {
    // user requested fake packets sent to them

    var dataPacket = new DataPacketClass.DataPacket();
    dataPacket.DataPacket("CLOUD-SERVER", StringConst.NULL_FIELD, StringConst.CURRENT_TIME, 
        StringConst.DATA_CACHE, "0,99,100,101,102");

    var interestPacket = new InterestPacketClass.InterestPacket();
    interestPacket.InterestPacket("CLOUD-SERVER", StringConst.NULL_FIELD, 
      StringConst.CURRENT_TIME, StringConst.INTEREST_CACHE_DATA, "0,99,100,101,102");

    udp_comm.sendMessage(dataPacket.createDATA(), req.body.user.ipAddr);
    udp_comm.sendMessage(interestPacket.createINTEREST(), req.body.user.ipAddr);
  }

});*/
// ---- Code Tests UDP Functionality ---

// --- Code Handles DB Creation ---

var pg = require('pg');

var client = new pg.Client(StringConst.DB_CONNECTION_STRING);
client.connect(function(err) {
  if(err) {
    return console.error('could not connect to postgres', err);
  }
});

/**
 * Function creates DB table if it currently don't exist.
 *
 * @param dbName suspect table name
 * @param dbCreationQuery creation query to be invoked if table doesn't exist
 */
function ifNonexistentCreateDB(dbName, dbCreationQuery) {
  client.query( "SELECT COUNT(*) FROM " + dbName, function(err, result) {

    if (err) {

      var errWords = toString(err).split(" ");
      var naiveCheckPasses = true;
      // create table if naive check passes
        
      naiveCheckPasses &= errWords.indexOf("does") === -1;
      naiveCheckPasses &= errWords.indexOf("not") === -1;
      naiveCheckPasses &= errWords.indexOf("exist") === -1;

      if (naiveCheckPasses) {
        
        client.query(dbCreationQuery);
      }
    } 
  });
}

function createPIT() {

  ifNonexistentCreateDB(StringConst.PIT_DB, StringConst.createPITQuery());
}

function createCS() {
  
  ifNonexistentCreateDB(StringConst.CS_DB, StringConst.createCSQuery());
}

function createFIB() {

  ifNonexistentCreateDB(StringConst.FIB_DB, StringConst.createFIBQuery());
}

function createLoginDB() {

    ifNonexistentCreateDB(StringConst.LOGIN_DB, StringConst.createLoginDBQuery());
}

createFIB();
createCS();
createPIT();
createLoginDB();

// --- Code Handles DB Creation ---

http.createServer(app).listen(app.get('port'), function() {
	console.log('Express server listening on port ' + app.get('port'));
});
