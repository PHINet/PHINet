/** 
 * File contains code for that functions as "main"
 * segment of execution for this web application
 **/

var StringConst = require('./string_const').StringConst;
var express = require('express')
var udp_comm = require('./udp_comm').UDPComm();
var http = require('http');

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
  res.sendFile('/public/templates/main.html', { root: __dirname })
})

app.get('/login', function (req, res) {
  res.sendFile('/public/templates/loginForm.html', { root: __dirname })
})

app.get('/signup', function (req, res) {
  res.sendFile('/public/templates/signupForm.html', { root: __dirname })
})

// ---- Code Tests UDP Functionality ---
var DataPacketClass = require('./datapacket');
var InterestPacketClass = require('./interestpacket');
app.get('/test', function (req, res) {
  res.sendFile('/public/templates/test.html', { root: __dirname })
})

/** method allows user to test networking functionality **/
app.post('/submitIP', function(req, res) {

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

});
// ---- Code Tests UDP Functionality ---

http.createServer(app).listen(app.get('port'), function() {
	console.log('Express server listening on port ' + app.get('port'));
});