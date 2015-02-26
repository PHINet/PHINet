/** 
 * File contains code for that functions as "main"
 * segment of execution for this web application
 **/

var express = require('express')
var app = express()

app.use(express.static(__dirname));

app.get('/', function (req, res) {
  res.sendFile('/public/main.html', { root: __dirname })
})

app.get('/login', function (req, res) {
  res.sendFile('/public/loginForm.html', { root: __dirname })
})

app.get('/signup', function (req, res) {
  res.sendFile('/public/signupForm.html', { root: __dirname })
})

var server = app.listen(3000, function () {

  var host = server.address().address
  var port = server.address().port

  console.log('Example app listening at http://%s:%s', host, port)

})