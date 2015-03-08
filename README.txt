This is a testbed for an IoT-NDN-WBAN project; currently a work in progress.

Current URL: http://ndn-healthnet.elasticbeanstalk.com/

In regards to the server, it's important that all files are zipped together. Don't just zip the parent directory.

Email Contact: rseales@troy.edu

--- Running Server-Side Code Locally ---
in the server-code main directory, enter the two commands into terminal
npm install
npm install body-parser --save


Android TODO 

- General: Test Cases, Documentation, Code-clarity, optimization

- DataPacket: allow optional fields to be excluded
- DataPacket: create signature
- Datapacket: create key locator

- InterestPacket: allow optional fields to be excluded
- InterestPacket: create min/max suffix
- InterestPacket: create publisher public key locator
- InterestPacket: create scope/lifetime interest

- NameField: real sha component

- ConfigNetLinks: improve functionality (FIB analysis, etc.)

- GetCliBeatActivity: selecting FIB results (only patients should be displayed)

- DatabaseHandler: generalize tables to various NDN formats (allow for packets outside of NDN-HealthNet)
- DatabaseHandler: improve upon content storage (especially for NDN-HealthNet data)
- DatabaseHandler: use timestring for all queries, where valid
- DatabaseHandler: utilize longest prefix matching regarding

- UDPListener: drastic communication improvements (validate entire packet, rather than only looking at name/content)

- UserCredentialActivity: rework with server to enforce names (login/logout type structure)
- UserCredentialActivity: improve input validation

- Utils: improve on processing cache data
- Utils: encrypt user information

- ViewMyDataActivity: extensive improvements regarding interval selection and data presentation

- PatientDataActivity: extensive improvements regarding interval selection and data presentation
- PatientDataActivity: rework to handle data on a per-sensor basis

Web TODO

- General: Test Cases, Documentation, Code-clarity, optimization

- 	Synch. Request
-	Full NDN
-	Profiles
-	DB
-	Test caes
-	Test page
-	Metrics
-	Documentation
