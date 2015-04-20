/** 
 * File contains code for that functions as "main"
 * segment of execution for this web application
 **/

var StringConst = require('./string_const').StringConst;
var cookieParser = require('cookie-parser');
var express = require('express');
var udp_comm = require('./udp_comm').UDPComm();
var http = require('http');
var ejs = require('ejs');
var fs = require('fs');
var LoginDB = require('./usercredentials.js').LoginCredentials(StringConst.LOGIN_DB);

var bodyParser = require('body-parser'); // allows easy form submissions

udp_comm.initializeListener();

var app = express()
app.use(bodyParser.json());
app.use(cookieParser());
app.use(bodyParser.urlencoded({
  extended: true
}));

app.set('port',  process.env.PORT || 3000);
app.use(express.static(__dirname));

app.get('/', function (req, res) {

    fs.readFile(__dirname + '/public/templates/index.html', 'utf-8', function(err, content) {
        if (err) {
            console.log("Error serving index.html: " + err);
        } else {
            var renderedHtml;

            if (req.cookies && req.cookies.user) {
                renderedHtml = ejs.render(content, {user: req.cookies.user});
            } else {
                renderedHtml = ejs.render(content, {user: ""});
            }

            res.send(renderedHtml);
        }
    });
});

app.get('/login', function (req, res) {

    fs.readFile(__dirname + '/public/templates/login.html', 'utf-8', function(err, content) {
        if (err) {
            console.log("Error serving login.html: " + err);
        } else {
            var renderedHtml;

            if (req.cookies && req.cookies.user) {
                renderedHtml = ejs.render(content, {user: req.cookies.user, error:""});
            } else {
                renderedHtml = ejs.render(content, {user: "", error:""});
            }

            res.send(renderedHtml);
        }
    });
});

app.get('/signup', function (req, res) {
    fs.readFile(__dirname + '/public/templates/signup.html', 'utf-8', function(err, content) {
        if (err) {
            console.log("Error serving signup.html: " + err);
        } else {
            var renderedHtml;

            if (req.cookies && req.cookies.user) {
                renderedHtml = ejs.render(content, {user: req.cookies.user, error:""});
            } else {
                renderedHtml = ejs.render(content, {user: "", error:""});
            }

            res.send(renderedHtml);
        }
    });
});

app.get('/logout', function(req, res) {
    fs.readFile(__dirname + '/public/templates/index.html', 'utf-8', function(err, content) {
        if (err) {
            console.log("Error serving index.html: " + err);
        } else {

            res.clearCookie('user');

            var renderedHtml = ejs.render(content, {user: "", error:""});

            res.send(renderedHtml);
        }
    });
});

app.get('/document', function (req, res) {
    fs.readFile(__dirname + '/public/templates/document.html', 'utf-8', function(err, content) {
        if (err) {
            console.log("Error serving document.html: " + err);
        } else {
            var renderedHtml;

            if (req.cookies && req.cookies.user) {
                renderedHtml = ejs.render(content, {user: req.cookies.user});
            } else {
                renderedHtml = ejs.render(content, {user: ""});
            }

            res.send(renderedHtml);
        }
    });
});

app.get('/contact', function (req, res) {
    fs.readFile(__dirname + '/public/templates/contact.html', 'utf-8', function(err, content) {
        if (err) {
            console.log("Error serving contact.html: " + err);
        } else {
            var renderedHtml;

            if (req.cookies && req.cookies.user) {
                renderedHtml = ejs.render(content, {user: req.cookies.user});
            } else {
                renderedHtml = ejs.render(content, {user: ""});
            }

            res.send(renderedHtml);
        }
    });
});

app.get('/profile', function (req, res) {
    fs.readFile(__dirname + '/public/templates/profile.html', 'utf-8', function(err, content) {
        if (err) {
            console.log("Error serving profile.html: " + err);
        } else {

            // verify that user exists before displaying profile page
            if (req.cookies && req.cookies.user) {
                LoginDB.getUser(req.cookies.user, function(rowsTouched, queryResult) {

                    var renderedHtml = ejs.render(content, {user: req.cookies.user, email: queryResult.getEmail(),
                                                                type: queryResult.getEntityType()});

                    res.send(renderedHtml);

                });

            } else {

                // user doesn't exist, direct to main page
                fs.readFile(__dirname + '/public/templates/index.html', 'utf-8', function(err, content) {
                    if (err) {
                        console.log("Error serving profile.html: " + err);
                    } else {

                        res.send( ejs.render(content, {user: "", email:"", type:""}));
                    }
                });
            }
        }
    });
});

app.get('/test', function (req, res) {
    fs.readFile(__dirname + '/public/templates/test.html', 'utf-8', function(err, content) {
        if (err) {
            console.log("Error serving test.html: " + err);
        } else {
            var renderedHtml;

            if (req.cookies && req.cookies.user) {
                renderedHtml = ejs.render(content, {user: req.cookies.user});
            } else {
                renderedHtml = ejs.render(content, {user: ""});
            }

            res.send(renderedHtml);
        }
    });
});

app.get('*', function(req, res){

    fs.readFile(__dirname + '/public/templates/404.html', 'utf-8', function(err, content) {
        if (err) {
            console.log("Error serving 404.html: " + err);
        } else {

            var renderedHtml;

            if (req.cookies && req.cookies.user) {
                renderedHtml = ejs.render(content, {user: req.cookies.user});
            } else {
                renderedHtml = ejs.render(content, {user: ""});
            }

            res.send(renderedHtml);
        }
    });
});

app.post('/loginAction', function(req, res) {

     LoginDB.getUser(req.body.user_name, function(rowsTouched, queryResults){

         var userAlreadyExists = rowsTouched === 1;
         var passwordMatched;

         if (queryResults === null) {
             passwordMatched = false; // user doesn't exist; passwords cannot match
         } else {
             passwordMatched = queryResults.getPassword() === req.body.user_password;
         }

        // checks to see if user already exists and password matches
        if (userAlreadyExists && passwordMatched) {

            // notify user of successful login
            fs.readFile(__dirname + '/public/templates/index.html', 'utf-8', function(err, content) {
                if (err) {
                    console.log("Error serving index.html: " + err);
                } else {

                    // TODO - improve on cookie use
                    res.cookie('user', req.body.user_name, {maxAge: 90000, httpOnly:true});

                    var renderedHtml = ejs.render(content, {user: req.body.user_name});
                    res.send(renderedHtml);
                }
            });

        } else {

            // notify user of unsuccessful login
            fs.readFile(__dirname + '/public/templates/login.html', 'utf-8', function(err, content) {
                if (err) {
                    console.log("Error serving login.html: " + err);
                } else {

                    var renderedHtml;
                    if (userAlreadyExists && !passwordMatched) {
                         renderedHtml = ejs.render(content, {error: "Login unsuccessful: incorrect password.", user:""});
                    } else {

                        // only remaining option: user does not exist
                        renderedHtml = ejs.render(content, {error: "Login unsuccessful: user does not exist.", user:""});
                    }

                    res.send(renderedHtml);
                }
            });
        }
    });
});

app.post('/registerAction', function(req, res) {

    // check that passwords match
    if (req.body.user_password[0] === req.body.user_password[1]) {

        // TODO - perform input validation on email, password, name, and entity

        var userType = "";
        if (req.body.user_type === 'p') {
            userType = StringConst.PATIENT_ENTITY;
        } else {

            // only valid type remaining is DOCTOR
            userType = StringConst.DOCTOR_ENTITY;
        }

        LoginDB.insertNewUser(req.body.user_name, req.body.user_password[0], req.body.user_email, userType,

            function(rowsTouched) {

                if (rowsTouched === 1) {

                    // notify user of successful register
                    fs.readFile(__dirname + '/public/templates/index.html', 'utf-8', function(err, content) {
                        if (err) {
                            console.log("Error serving index.html: " + err);
                        } else {

                            // TODO - improve on cookie use
                            res.cookie('user', req.body.user_name, {maxAge: 90000, httpOnly:true});

                            var renderedHtml = ejs.render(content, {user: req.body.user_name});
                            res.send(renderedHtml);
                        }
                    })
                } else {

                    // notify user of bad input
                    fs.readFile(__dirname + '/public/templates/signup.html', 'utf-8', function(err, content) {
                        if (err) {
                            console.log("Error serving signup.html: " + err);
                        } else {
                            var renderedHtml = ejs.render(content, {user: "", error: "Register unsuccessful. Bad input"});
                            res.send(renderedHtml);
                        }
                    })
                }
        });

    } else {

        // notify user of password mismatch
        fs.readFile(__dirname + '/public/templates/signup.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving signup.html: " + err);
            } else {
                var renderedHtml = ejs.render(content, {user: "", error: "Passwords don't match."});
                res.send(renderedHtml);
            }
        })
    }
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

    ifNonexistentCreateDB(StringConst.PIT_DB, StringConst.createPITQuery(StringConst.PIT_DB));
    ifNonexistentCreateDB(StringConst.PIT_TEST_DB, StringConst.createPITQuery(StringConst.PIT_TEST_DB));
}

function createCS() {
  
    ifNonexistentCreateDB(StringConst.CS_DB, StringConst.createCSQuery(StringConst.CS_DB));
    ifNonexistentCreateDB(StringConst.CS_TEST_DB, StringConst.createCSQuery(StringConst.CS_TEST_DB));
}

function createFIB() {

    ifNonexistentCreateDB(StringConst.FIB_DB, StringConst.createFIBQuery(StringConst.FIB_DB));
    ifNonexistentCreateDB(StringConst.FIB_TEST_DB, StringConst.createFIBQuery(StringConst.FIB_TEST_DB));
}

function createLoginDB() {

    ifNonexistentCreateDB(StringConst.LOGIN_DB, StringConst.createLoginDBQuery(StringConst.LOGIN_DB));
    ifNonexistentCreateDB(StringConst.LOGIN_TEST_DB, StringConst.createLoginDBQuery(StringConst.LOGIN_TEST_DB));
}

createFIB();
createCS();
createPIT();
createLoginDB();

// --- Code Handles DB Creation ---

http.createServer(app).listen(app.get('port'), function() {
	console.log('Express server listening on port ' + app.get('port'));
});
