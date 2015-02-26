/** 
 * File contains code for the Content Store
 * specified in the NDN documentation
 **/



exports.cs = function() {
	// NOTE: contents will be returned when 
	// other modules "require" this 
}
/*



// NOTE: this is the original server code 
/*
Handle UDP Socket
*/
/*
var PORT = 1635;
var HOST = '10.0.2.15';

//Create Socket & Initialize Buffer
var dgram = require('dgram');
var server = dgram.createSocket('udp4');
var buffer = new Buffer(1024); //buffersize

//Error handler for socket creation
server.on("error", function (err) {
	console.log("server error:\n" + err.stack);
	server.close();
});

//Listening event fired when server ready to recieve UDP packets
server.on('listening', function() {
	var address = server.address();
	server.listen(PORT, HOST);
	console.log('UDP server listening '+ PORT);
});

//Message event fired when udp packet arrives
server.on('message', function(message, remote) {
	console.log(remote.address + ':' + remote.port +' - ' + message);
	buffer.write(message);
	console.log(message);	
});

server.bind(PORT);

console.log(buffer.toString('utf8'));


//Generate a new instance of express server
var http = require('http'),
	express = require('express'),
	app = express();
	sqlite3 = require('sqlite3').verbose(),
	db = new sqlite3.Database('cozy');


/*Configure directive to tell express to use jade for rendering templates*/
	/*app.set('views', __dirname + '/public');
	app.engine('.html', require('jade').__express);

	//Allows express to get data from POST requests
	app.use(express.bodyParser());

//Database Initialization/*
db.get("SELECT name FROM sqlite_master WHERE type='table' AND name='data'", function(err, row) {
	if(err !== null) {
		console.log(err);
	}
	else if(row == null) {
		db.run('CREATE TABLE "data" ("id" INTEGER PRIMARY KEY AUTOINCREMENT, "name" VARCHAR(255), content VARCHAR(255))', function(err) {
			if(err !== null) {
				console.log(err);
			}
			else {
				console.log("SQL Table 'data' initialized.");
			}
		});
	}
	else {
		console.log("SQL Table 'data' already initialized.");
	}
});

//We render the templates with the data
app.get('/', function(req, res) {
	
	db.all('SELECT * FROM data ORDER BY name', function(err, row) {
		if(err !== null) {
			res.send(500, "An error has occurred -- " + err);
		}
		else {
			res.render('index.jade', {bookmarks: row}, function(err, html) {
				res.send(200, html);
			});
		}
	});
});

//Route to handle bookmark creation
app.post('/add', function(req, res) {
	name = req.body.name;
	content = req.body.content;
	sqlRequest = "INSERT INTO 'name' (name, content) VALUES('" + name + "', '" + content + "')"
	db.run(sqlRequest, function(err) {
		if(err !==null) {
			res.send(500, "An error has occurred -- " + err);
		}
		else {
			res.redirect('back');
		}
	});
});

//Route to handle bookmark deletion
app.get('/delete/:id', function(req, res) {
	db.run("DELETE FROM name WHERE id='" + req.params.id + "'", function(err) {
		if(err!==null) {
			res.send(500, "An error has occurred -- " + err);
		}
		else {
			res.redirect('back');
		}
	});
});
*/


//At the root of your website, we show the index.html page
/*
app.get('/', function(req, res) {
	res.sendFile(path.join(__dirname, './public/', 'index.html'));
});
*/

/*
//Define Bookmarks
var bookmarks = []
bookmarks.push({name: "Cozycloud", content:
	"http://cozycloud.cc"});
bookmarks.push({name: "Cozy.io", content:
	"http://cozy.io"});
bookmarks.push({name: "My Cozy", content:
	"http://localhost:9104/"});

//Render Templates with data
app.get('/', function(req, res) {
	params = {
		"bookmarks": bookmarks
	}
	res.render('public/index.jade', params, function(err, html) {
		res.send(200, html);
	});
});
*/



/* This will allow Cozy to run your app smoothly but it won't break other execution environment */
/*var port = process.env.PORT || 9250;
var host = process.env.HOST || "127.0.0.1";

//Starts the server itself
var server = http.createServer(app).listen(port, host, function() {
	console.log("Server listening to %s:%d within %s environment",
		host, port, app.get('env'));
});

*/