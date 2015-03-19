
----------- General Information -------------------------------------

This is a testbed for an IoT-NDN-WBAN project; currently a work in progress.

Current URL: http://ndn-healthnet.elasticbeanstalk.com/

In regards to update the server-side code, it's important that all files 
are zipped together. Don't just zip the parent directory.

----------------------------------------------------------------------

------------------ Running Server-Side Code Locally ------------------

in the server-code main directory, enter the two commands into terminal
npm install
npm install body-parser --save

-----------------------------------------------------------------------

--------------------------- Paper TODO --------------------------------

- write paper from perspective of system design 

-----------------------------------------------------------------------


--------------------------- Android TODO ------------------------------

- General: Test Cases, Documentation, Code-clarity, optimization, handle TODOs
- General: use WiFi-Direct
- General: Consider NDN Libraries
- General: Define specific process IDs & sensorIDs

- DataPacket: allow optional fields to be excluded/included
- DataPacket: create signature
- Datapacket: create key locator

- InterestPacket: allow optional fields to be excluded/included
- InterestPacket: create min/max suffix
- InterestPacket: create publisher public key locator
- InterestPacket: create scope/lifetime interest

- NameField: real sha component

- ConfigNetLinks: improve functionality (FIB analysis, etc.)

- GetCliBeatActivity: selecting FIB results (only patients should be displayed)

- DatabaseHandler: improve content storage (especially for NDN-HealthNet data)
- DatabaseHandler: use timestring for all queries, where valid
- DatabaseHandler: utilize longest prefix matching regarding
- DatabaseHandler: set limits on cache/pit/fib size (and clean at certain intervals)
- DatabaseHandler: prevent sql injection via naming, etc
- DatabaseHandler: longestprefix match

- UDPListener: drastic communication improvements (validate entire packet, rather than only looking at name/content)

- UserCredentialActivity: rework with server to enforce names (login/logout type structure)
- UserCredentialActivity: improve input validation

- Utils: improve on processing cache data
- Utils: encrypt user information

- ViewMyDataActivity: extensive improvements regarding interval selection and data presentation

- PatientDataActivity: extensive improvements regarding interval selection and data presentation
- PatientDataActivity: rework to handle data on a per-sensor basis

- UDPSocket: create&send synch. requests on a regular basis

-----------------------------------------------------------------------

------------------------ Web TODO -------------------------------------

- General: Test Cases, Documentation, Code-clarity, optimization, handle TODOs
- General: Consider NDN Libraries

- Define specific process IDs & sensorIDs
- Account Management: web profile page 
- longestprefix match Interests
- Documentation Page
- NDN reception (etc.) Test Page
- set limits on cache/pit/fib size (and clean at certain intervals)
- Functional DB
- Provide useful metrics
- Full NDN (allow exclusion/inclusion of any part of packet & analyze the whole)
- prevent sql injection via naming, etc

-----------------------------------------------------------------------



