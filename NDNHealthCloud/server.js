/** 
 * File contains code for that functions as "main"
 * segment of execution for this web application
 **/

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

app.set('port', process.env.PORT || 3000);
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
app.get('/test', function (req, res) {
  res.sendFile('/public/templates/test.html', { root: __dirname })
})

app.post('/submitIP', function(req, res) {
   console.log(req.body.user.ipAddr);

   var dataPacket = new DataPacketClass.DataPacket();
			    dataPacket.DataPacket("SERVER", "null",
			            "NOW", "CACHE_DATA", "99,100,101,102");

   udp_comm.sendMessage(dataPacket.createDATA(), req.body.user.ipAddr);

});
// ---- Code Tests UDP Functionality ---

http.createServer(app).listen(app.get('port'), function() {
	console.log('Express server listening on port ' + app.get('port'));
});