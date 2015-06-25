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

var udp_comm = require('./udp_comm').UDPComm(PIT, FIB, CS, LoginDB); // UDP communication module; requires reference to databases
var http = require('http'); // used to create the server
var ejs = require('ejs'); // enables use of embedded javascript
var fs = require('fs'); // enables easy file reading

var favicon = require('serve-favicon'); // allows use of a custom favicon
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
app.use(favicon(__dirname + '/public/images/favicon.ico'));


var RATE_LIMIT_CODE = 429; // HTTP Code 429 is for rate-limits
var rateLimitDictionary = {};

// chosen somewhat arbitrarily, is hopefully large enough to server many legitimate users with private IP
var MAX_HITS_PER_SECOND = 50; 
var CLEAN_RATE_DICT_INTERVAL = 1000 * 60 * 60; // chosen somewhat arbitrarily

// TODO - improve upon this naive rate-limiting

/**
 * Enforces that each IP recieves a maximum of MAX_HITS_PER_SECOND pages each second.
 * 
 * @param userIP of user requesting page
 * @return boolean regarding isRateLimit status of user's IP
 */
var isRateLimited = function (userIP) {

    var currentSecond = parseInt(new Date().getTime() / 1000);

    if (!rateLimitDictionary[userIP]) {
        rateLimitDictionary[userIP] = [currentSecond, 1];

        return false;
    } else {

        if (rateLimitDictionary[userIP][0] === currentSecond) {

            if(rateLimitDictionary[userIP][1] > MAX_HITS_PER_SECOND) {
                return true;
            } else {
                rateLimitDictionary[userIP][1] += 1;
                return false;
            }
        } else {
            rateLimitDictionary[userIP] = [currentSecond, 1];
            return false;
        }
    }

}

/**
 * Each CLEAN_RATE_DICT_INTERVAL we remove all entries that contain
 * invalid times (ie past times that could not cause a rate limit).
 */
setInterval(
    function() {
        var currentSecond = parseInt(new Date().getTime() / 1000);

        for (var key in rateLimitDictionary) {
            if (rateLimitDictionary[key][0] !== currentSecond) {
                delete rateLimitDictionary[key];
            }
        }

    }, CLEAN_RATE_DICT_INTERVAL
); // invoke every interval

/**
 * Handles main web page
 */
app.get('/', function (req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
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
    } else {
        fs.readFile(__dirname + '/public/templates/rate_limit.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving rate_limit.html: " + err);
            } else {
                var renderedHtml = ejs.render(content);

                res.status(RATE_LIMIT_CODE).send(renderedHtml);
            }
        });
    }
});

/**
 * Handles login page
 */
app.get('/login', function (req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
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
    } else {
        fs.readFile(__dirname + '/public/templates/rate_limit.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving rate_limit.html: " + err);
            } else {
                var renderedHtml = ejs.render(content);

                res.status(RATE_LIMIT_CODE).send(renderedHtml);
            }
        });
    }
});

/**
 * Handles signup page
 */
app.get('/signup', function (req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
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
    } else {
        fs.readFile(__dirname + '/public/templates/rate_limit.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving rate_limit.html: " + err);
            } else {
                var renderedHtml = ejs.render(content);

                res.status(RATE_LIMIT_CODE).send(renderedHtml);
            }
        });
    }
});

/**
 * Handles logout request by clearing the login cookie.
 */
app.get('/logout', function(req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
        fs.readFile(__dirname + '/public/templates/index.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving index.html: " + err);
            } else {

                var renderedHtml = ejs.render(content, {user: "", error:""});

                res.clearCookie('user'); // user has logged out; clear the login cookie
                res.send(renderedHtml);
            }
        });
    } else {
        fs.readFile(__dirname + '/public/templates/rate_limit.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving rate_limit.html: " + err);
            } else {
                var renderedHtml = ejs.render(content);

                res.status(RATE_LIMIT_CODE).send(renderedHtml);
            }
        });
    }
});

/**
 * Handles FAQ page
 */
app.get('/faq', function (req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
        fs.readFile(__dirname + '/public/templates/faq.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving faq.html: " + err);
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
    } else {
        fs.readFile(__dirname + '/public/templates/rate_limit.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving rate_limit.html: " + err);
            } else {
                var renderedHtml = ejs.render(content);

                res.status(RATE_LIMIT_CODE).send(renderedHtml);
            }
        });
    }
});

/**
 * Handles profile page
 */
app.get('/profile', function (req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
        fs.readFile(__dirname + '/public/templates/profile.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving profile.html: " + err);
            } else {

                // verify that user-login cookie exists before displaying profile page
                if (req.cookies && req.cookies.user) {

                    // now, query database as second level of validation
                    LoginDB.getUserByID(req.cookies.user, function(rowsTouched, queryResult) {

                        // TODO - perform more sophisticated validation

                        var displayedEmail;
                        if (queryResult.getEmail() === StringConst.NULL_FIELD) {
                            displayedEmail = "you didn't enter one"
                        } else {
                            displayedEmail = queryResult.getEmail();
                        }

                        // capitalize the first letter
                        var displayedPatientType = queryResult.getEntityType().charAt(0) + queryResult.getEntityType().slice(1).toLocaleLowerCase();

                        var renderedHtml = ejs.render(content, {user: req.cookies.user, email: displayedEmail,
                                                                    type: displayedPatientType});

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
    } else {
        fs.readFile(__dirname + '/public/templates/rate_limit.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving rate_limit.html: " + err);
            } else {
                var renderedHtml = ejs.render(content);

                res.status(RATE_LIMIT_CODE).send(renderedHtml);
            }
        });
    }
});

/**
 * Handles viewdata page
 */
app.get('/viewdata', function (req, res) {

    var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
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
    } else {
        fs.readFile(__dirname + '/public/templates/rate_limit.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving rate_limit.html: " + err);
            } else {
                var renderedHtml = ejs.render(content);

                res.status(RATE_LIMIT_CODE).send(renderedHtml);
            }
        });
    }
});

/**
 * Handles test page
 */
app.get('/test', function (req, res) {

     var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
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
    } else {
        fs.readFile(__dirname + '/public/templates/rate_limit.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving rate_limit.html: " + err);
            } else {
                var renderedHtml = ejs.render(content);

                res.status(RATE_LIMIT_CODE).send(renderedHtml);
            }
        });
    }
});

/**
 * Handles all other queries; responds with 404 page
 */
app.get('*', function(req, res) {

     var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
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

                res.status(404).send(renderedHtml);
            }
        });
    } else {
        fs.readFile(__dirname + '/public/templates/rate_limit.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving rate_limit.html: " + err);
            } else {
                var renderedHtml = ejs.render(content);

                res.status(RATE_LIMIT_CODE).send(renderedHtml);
            }
        });
    }
});

/**
 * Handles user login-attempt
 */
app.post('/loginAction', function(req, res) {

     var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
        
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

            var pw = req.body.user_password.trim();
            var userName = req.body.user_name.trim();

            // user entered both required params, now query databse to verify password
            LoginDB.getUserByID(userName, function(rowsTouched, queryResults){

                // only attempt to compare passwords if query was successful
                if (queryResults != null && rowsTouched == 1) {

                    utils.comparePassword(pw, queryResults.getPassword(),
                        function(err, isPasswordMatch) {

                            if (isPasswordMatch) {
                                // notify user of successful login
                                fs.readFile(__dirname + '/public/templates/index.html', 'utf-8', function(err, content) {
                                    if (err) {
                                        console.log("Error serving index.html: " + err);
                                    } else {

                                        // TODO - improve on cookie use
                                        res.cookie('user', userName, {maxAge: 90000, httpOnly:true});

                                        var renderedHtml = ejs.render(content, {user: userName});
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
    } else {
        fs.readFile(__dirname + '/public/templates/rate_limit.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving rate_limit.html: " + err);
            } else {
                var renderedHtml = ejs.render(content);

                res.status(RATE_LIMIT_CODE).send(renderedHtml);
            }
        });
    }
});

/**
 * Handles user register-attempt
 */
app.post('/registerAction', function(req, res) {

     var ip = req.headers['x-forwarded-for'] || req.connection.remoteAddress || 
                req.socket.remoteAddress || req.connection.socket.remoteAddress;

    if (!isRateLimited(ip)) {
    
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

            var pw = req.body.user_password[1].trim();
            var verifyPW = req.body.user_password[1].trim();
            var userName = req.body.user_name.trim();
            var email = req.body.user_email.trim();
            
            // check that passwords match, enforce PW/Username syntax, and verify validity of email address
            if (pw === verifyPW
                && utils.isValidPassword(pw) && utils.isValidUserName(userName)
                && (!email || utils.isValidEmail(email))) {

                var userType = "";
                if (req.body.user_type === 'p') {

                    // user type of 'p' denotes patient
                    userType = StringConst.PATIENT_ENTITY;
                } else {

                    // only valid type remaining is DOCTOR
                    userType = StringConst.DOCTOR_ENTITY;
                }

                if (!email) {
                    email = StringConst.NULL_FIELD; // email not provided; list as null
                }

                // hash user's password then insert user in database
                utils.hashPassword(pw, function(err, hashedPW) {

                    // store hashed pw into DB
                    LoginDB.insertNewUser(userName, hashedPW, email, userType,

                        function(rowsTouched) {

                            // one row touched corresponds to successful insertion
                            if (rowsTouched === 1) {

                                // notify user of successful register
                                fs.readFile(__dirname + '/public/templates/index.html', 'utf-8', function(err, content) {
                                    if (err) {
                                        console.log("Error serving index.html: " + err);
                                    } else {

                                        // TODO - improve on cookie use
                                        res.cookie('user', userName, {maxAge: 90000, httpOnly:true});

                                        var renderedHtml = ejs.render(content, {user: userName});
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
                                        var renderedHtml = ejs.render(content, {user: "", error: "Register unsuccessful."
                                                        + "\nEither username or email already exists."});
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
                    } else if (pw !== verifyPW) {

                        renderedHtml = ejs.render(content, {user: "", error: "Passwords don't match."});
                        res.send(renderedHtml);
                    }
                    // since email is optional, validity only applies if it was entered
                    else if (!utils.isValidEmail(email) && email) {

                        renderedHtml = ejs.render(content, {user: "", error: "Invalid email."});
                        res.send(renderedHtml);
                    } else if (!utils.isValidPassword(pw)) {

                        renderedHtml = ejs.render(content, {user: "", error: "Invalid password. Must use 3-15 alpha-numerics."});
                        res.send(renderedHtml);
                    } else if (!utils.isValidUserName(userName)) {

                        renderedHtml = ejs.render(content, {user: "", error: "Invalid username. Must use 3-15 alpha-numerics."});
                        res.send(renderedHtml);
                    }
                })
            }
        }
    } else {
        fs.readFile(__dirname + '/public/templates/rate_limit.html', 'utf-8', function(err, content) {
            if (err) {
                console.log("Error serving rate_limit.html: " + err);
            } else {
                var renderedHtml = ejs.render(content);

                res.status(RATE_LIMIT_CODE).send(renderedHtml);
            }
        });
    }
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
