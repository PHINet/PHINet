/** 
 * File contains code for that functions as "main"
 * segment of execution for this web application
 **/

var StringConst = require('./string_const').StringConst; // important Strings
var cookieParser = require('cookie-parser'); // allows integration of cookies
var express = require('express'); // Node.js web framework

var PIT = require('./pit').PIT(StringConst.PIT_DB); // PendingInterestTable database module
var FIB = require('./fib').FIB(StringConst.FIB_DB); // ForwardingInformationBase database module
var CS = require('./cs').CS(StringConst.CS_DB); // ContentStore database module
var LoginDB = require('./usercredentials.js').LoginCredentials(StringConst.LOGIN_DB); // user credential database
var utils = require('./utils').Utils; // useful utility methods

var udp_comm = require('./udp_comm').UDPComm(PIT, FIB, CS); // UDP communication module; requires reference to databases
var http = require('http'); // used to create the server
var ejs = require('ejs'); // enables use of embedded javascript
var fs = require('fs'); // enables easy file reading

var bodyParser = require('body-parser'); // allows easy form submissions

udp_comm.initializeListener(); // begin listening for udp packets

var app = express();
app.use(bodyParser.json());
app.use(cookieParser());
app.use(bodyParser.urlencoded({
  extended: true
}));

app.set('port',  process.env.PORT || 3000);
app.use(express.static(__dirname));

/**
 * Handles main web page
 */
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

/**
 * Handles login page
 */
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

/**
 * Handles signup page
 */
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

/**
 * Handles logout request by clearing the login cookie.
 */
app.get('/logout', function(req, res) {
    fs.readFile(__dirname + '/public/templates/index.html', 'utf-8', function(err, content) {
        if (err) {
            console.log("Error serving index.html: " + err);
        } else {

            var renderedHtml = ejs.render(content, {user: "", error:""});

            res.clearCookie('user'); // user has logged out; clear the login cookie
            res.send(renderedHtml);
        }
    });
});

/**
 * Handles documentation page
 */
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

/**
 * Handles contact page
 */
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

/**
 * Handles profile page
 */
app.get('/profile', function (req, res) {
    fs.readFile(__dirname + '/public/templates/profile.html', 'utf-8', function(err, content) {
        if (err) {
            console.log("Error serving profile.html: " + err);
        } else {

            // verify that user-login cookie exists before displaying profile page
            if (req.cookies && req.cookies.user) {

                // now, query database as second level of validation
                LoginDB.getUser(req.cookies.user, function(rowsTouched, queryResult) {

                    // TODO - perform more sophisticated validation

                    var renderedHtml = ejs.render(content, {user: req.cookies.user, email: queryResult.getEmail(),
                                                                type: queryResult.getEntityType()});

                    res.send(renderedHtml);
                });

            }
            // user doesn't exist, direct to main page
            else {

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

/**
 * Handles viewdata page
 */
app.get('/viewdata', function (req, res) {
    fs.readFile(__dirname + '/public/templates/viewdata.html', 'utf-8', function(err, content) {
        if (err) {
            console.log("Error serving viewdata.html: " + err);
        } else {

            // verify that user exists before displaying page
            if (req.cookies && req.cookies.user) {

                // query the database for the user's data
                CS.getGeneralCSData(req.cookies.user, function(rowsTouched, queryResults) {

                    // TODO - improve upon display (give patients or own data, etc)

                    var renderedHtml = ejs.render(content, {user: req.cookies.user, data:JSON.stringify(queryResults)});

                    res.send(renderedHtml);
                });

            }
            // user doesn't exist, direct to main page
            else {

                fs.readFile(__dirname + '/public/templates/index.html', 'utf-8', function(err, content) {
                    if (err) {
                        console.log("Error serving viewdata.html: " + err);
                    } else {

                        res.send( ejs.render(content, {user: "", email:"", type:""}));
                    }
                });
            }
        }
    });
});

/**
 * Handles test page
 */
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

/**
 * Handles all other queries; responds with 404 page
 */
app.get('*', function(req, res){

    fs.readFile(__dirname + '/public/templates/404.html', 'utf-8', function(err, content) {
        if (err) {
            console.log("Error serving 404.html: " + err);
        } else {

            var renderedHtml ;

            if (req.cookies && req.cookies.user) {
                renderedHtml = ejs.render(content, {user: req.cookies.user});
            } else {
                renderedHtml = ejs.render(content, {user: ""});
            }

            res.send(renderedHtml);
        }
    });
});

/**
 * Handles user login-attempt
 */
app.post('/loginAction', function(req, res) {

    // check that user entered both required params
    if (!req.body.user_name || !req.body.user_password) {

        // notify user of unsuccessful login
        fs.readFile(__dirname + '/public/templates/login.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving login.html: " + err);
            } else {

                var renderedHtml = ejs.render(content, {error: "Login unsuccessful: provide all input.", user:""});

                res.send(renderedHtml);
            }
        });

    } else {
        // user entered both required params, now query databse to verify password
        LoginDB.getUser(req.body.user_name, function(rowsTouched, queryResults){

            // only attempt to compare passwords if query was successful
            if (queryResults != null && rowsTouched == 1) {

                utils.comparePassword(req.body.user_password, queryResults.getPassword(),
                    function(err, isPasswordMatch) {

                        if (isPasswordMatch) {
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

                                    var renderedHtml = ejs.render(content, {error: "Login unsuccessful: incorrect password.", user:""});

                                    res.send(renderedHtml);
                                }
                            });
                        }
                });
            } else {
                // notify user of unsuccessful login (no user found)
                fs.readFile(__dirname + '/public/templates/login.html', 'utf-8', function(err, content) {
                    if (err) {
                        console.log("Error serving login.html: " + err);
                    } else {

                        var renderedHtml;

                        // only remaining option: user does not exist
                        renderedHtml = ejs.render(content, {error: "Login unsuccessful: user does not exist.", user:""});

                        res.send(renderedHtml);
                    }
                });
            }
        });
    }
});

/**
 * Handles user register-attempt
 */
app.post('/registerAction', function(req, res) {

    //check that user entered all required params
    if (!req.body.user_password || !req.body.user_name || !req.body.user_type) {
        // notify user of unsuccessful login
        fs.readFile(__dirname + '/public/templates/signup.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving login.html: " + err);
            } else {

                var renderedHtml = ejs.render(content, {error: "Register unsuccessful: provide all input.", user:""});

                res.send(renderedHtml);
            }
        });
    } else {

        // TODO - perform more sophisticated password requirements

        // check that passwords match and enforce five character PW
        if (req.body.user_password[0] === req.body.user_password[1] && req.body.user_password[0].length > 5) {

            // TODO - perform input validation on email, password, name, and entity

            var userType = "";
            if (req.body.user_type === 'p') {

                // user type of 'p' denotes patient
                userType = StringConst.PATIENT_ENTITY;
            } else {

                // only valid type remaining is DOCTOR
                userType = StringConst.DOCTOR_ENTITY;
            }

            if (!req.body.user_email) {
                req.body.user_email = "null"; // email not provided; list as null
            }

            // hash user's password then insert user in database
            utils.hashPassword(req.body.user_password[0], function(err, hashedPW) {

                // store hashed pw into DB
                LoginDB.insertNewUser(req.body.user_name, hashedPW, req.body.user_email, userType,

                    function(rowsTouched) {

                        // one row touched corresponds to successful insertion
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
                        }
                        // an error occurred while inserting user into the database
                        else {

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
            });

        } else {

            // notify user of password mismatch
            fs.readFile(__dirname + '/public/templates/signup.html', 'utf-8', function(err, content) {
                var renderedHtml;

                if (err) {
                    console.log("Error serving signup.html: " + err);
                } else if (req.body.user_password[0] !== req.body.user_password[1]) {
                    renderedHtml = ejs.render(content, {user: "", error: "Passwords don't match."});
                    res.send(renderedHtml);
                } else {
                    renderedHtml = ejs.render(content, {user: "", error: "Minimum password length is 5 characters."});
                    res.send(renderedHtml);
                }
            })
        }
    }
});

// --- Below Code Handles DB Creation ---

var postgresDB = require('pg'); // the postgres Node.js module

var client = new postgresDB.Client(StringConst.DB_CONNECTION_STRING);
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

        // TODO - perform more sophisticated check

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

// --- Above Code Handles DB Creation ---

http.createServer(app).listen(app.get('port'), function() {
	console.log('Express server listening on port ' + app.get('port'));
});
