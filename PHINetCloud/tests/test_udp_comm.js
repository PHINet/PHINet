/**
 * File provides test cases for the UDP Communication module implemented in udp_comm.js
 */

var expect = require("chai").expect;
var StringConst = require('../string_const').StringConst;
var udp_comm = require('../udp_comm').UDPComm();

// the udp_comm module has several branches (i.e.,
// we send the packet along as more specific things are detected)
// here, we will only test the final branches


/**
 *
 */
describe('UDPComm', function(){
    describe('#handleInterestFIBRequest', function(){
        it('X', function(done){

            // 1. test if empty fib

            // 2. test if non-empty fib

            // 3. test bad input


            done();
        })
    })
});

/**
 *
 */
describe('UDPComm', function(){
    describe('#handleInterestCacheRequest', function(){
        it('X', function(done){

            // 1. handle with empty cache

            // 2. handle with non empty cache

            // 3. handle with non-empty pit

            // 4. handle with empty pit

            // 5. test bad data

            done();

        })
    })
});

/**
 *
 */
describe('UDPComm', function(){
    describe('#handleCacheData', function(){
        it('X', function(done){

            // 1. test bad input

            // 2. handle with empty cache for this data

            // 3. handle with non-empty cache for this data (update)

            // 4. handle with pit requests

            // 4. handle without pit requests

            done();
        })
    })
});

/**
 *
 */
describe('UDPComm', function() {
    describe('#handleFIBData', function () {
        it('X', function (done) {

            /* var IP1 = "11.11.11.11";
             var IP2 = "12.12.12.12";
             var userID1 = "user1";
             var deviceID = "deviceID";

             // it's necessary to have a userID entered for device so that a FIB entry isn't added for self
             Utils.saveToPrefs(this.context, StringConst.PREFS_LOGIN_USER_ID_KEY, deviceID);

             var goodFIBEntry = userID1 + "," + IP1;
             var updateToGoodFIBEntry = userID1 + "," + IP2;
             var deviceFIBEntry = deviceID + "," + IP1; // should be rejected on basis of ID
             var badFIBEntry1 = "a,a";
             var badFIBEntry2 = "apsidfasdf";

             datasource.deleteEntireFIB(); // clear FIB before testing functionality

             // test on bad input
             expect(udp_comm.handleFIBData(null)).to.equal(false);
             expect(udp_comm.handleFIBData("")).to.equal(false);
             expect(udp_comm.handleFIBData(badFIBEntry1)).to.equal(false);
             expect(udp_comm.handleFIBData(badFIBEntry2)).to.equal(false);

             // handle handle with self-FIB entry; should be rejected because it's the device's own ID
             expect(udp_comm.handleFIBData(deviceFIBEntry)).to.equal(false);

             // --- handle with FIB entry of other entity ---

             udp_comm.handleFIBData(goodFIBEntry);

             expect(udp_comm.handleFIBData(goodFIBEntry)).to.equal(true);

             DBData fibEntry = datasource.getFIBData(userID1);

             expect(fibEntry.getIpAddr()).to.equal(IP1); // test entry was added to FIB

             expect(udp_comm.handleFIBData(updateToGoodFIBEntry)).to.equal(true);

             DBData updatedFIBEntry = datasource.getFIBData(userID1);

             expect(updatedFIBEntry.getIpAddr()).to.equal(IP2); // test entry was updated in FIB

             // --- handle with FIB entry of other entity ---*/

            done();

        })
    })
});

/**
 *
 */
describe('UDPComm', function(){
    describe('#isValidForTimeInterval', function(){
        it('X', function(done){
            var goodRequestInterval = "2012-05-04T08:08:08.888||2014-05-04T08:08:08.888";
            var badRequestInterval = "2012-ERROR:08.888||2014-ERROR8:08.888";
            
            var goodDataInterval1 = "2012-07-04T08:08:08.888"; // date is within goodRequestInterval
            var goodDataInterval2 = "2012-01-04T08:08:08.888"; // date is before goodRequestInterval
            var goodDataInterval3 = "2014-07-04T08:08:08.888"; // date is after goodRequestInterval
            
            var testInterval1 = "2015-02-22T00:00:00.000||2015-04-22T00:00:00.000";
            var dataTime1 = "2015-03-22T22:58:10.878";
            
            // --- test bad input ---
            expect(udp_comm.isValidForTimeInterval(null, null)).to.equal(false); // null entries
            
            // syntax error in request interval
            expect(udp_comm.isValidForTimeInterval(badRequestInterval, goodDataInterval1)).to.equal(false);
            
            // two data intervals and no request interval
            expect(udp_comm.isValidForTimeInterval(goodDataInterval1, goodDataInterval1)).to.equal(false);
            
            // --- test bad input ---
            
            // test input rejection if before interval
            expect(udp_comm.isValidForTimeInterval(goodRequestInterval, goodDataInterval2)).to.equal(false);
            
            // test input rejection if after interval
            expect(udp_comm.isValidForTimeInterval(goodRequestInterval, goodDataInterval3)).to.equal(false);
            
            // test input acceptance if during interval
            expect(udp_comm.isValidForTimeInterval(goodRequestInterval, goodDataInterval1)).to.equal(true);
            expect(udp_comm.isValidForTimeInterval(testInterval1, dataTime1)).to.equal(true);

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});
