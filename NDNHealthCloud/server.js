/** 
 * File contains code for that functions as "main"
 * segment of execution for this web application
 **/

var express = require('express')
var udp_comm = require('./udp_comm').UDPComm();
var http = require('http');

udp_comm.initializeListener();

var app = express()

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

http.createServer(app).listen(app.get('port'), function() {
	console.log('Express server listening on port ' + app.get('port'));
});