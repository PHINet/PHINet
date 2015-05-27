/**
 * File provides test cases for the UDP Communication module implemented in udp_comm.js
 */

var expect = require("chai").expect;
var StringConst = require('../string_const').StringConst;
var udp_comm = require('../udp_comm').UDPComm(StringConst.PIT_TEST_DB, StringConst.FIB_TEST_DB, StringConst.CS_TEST_DB);

var DBData = require('../data'); // used to create objects used by the database

// the udp_comm module has several branches (i.e.,
// we send the packet along as more specific things are detected)
// here, we will only test the final branches


var NDN_SENSOR_NET_PORT = 50056; // same across all applications; useful for testing

// --- test FIB entries ---

var entry1 = DBData.DATA();
entry1.fibData("serverTestUser1", StringConst.CURRENT_TIME, "10.10.10.10");

// --- test FIB entries ---

/**
 *  Tests UDPComm.handleInterestFIBRequest() functionality.
 */
describe('UDPComm', function(){
    describe('#handleInterestFIBRequest()', function(){
        it('TODO - document what this function should return', function(done){

            // test valid input on empty FIB
            expect(udp_comm.handleInterestFIBRequest(entry1.getUserID(), entry1.getSensorID(),
                    entry1.getIpAddr(), NDN_SENSOR_NET_PORT)).to.equal(false);

            // test bad input
            expect(udp_comm.handleInterestFIBRequest(null, entry1.getSensorID(),
                entry1.getIpAddr(), null)).to.equal(false);

            // TODO - more robust testing

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});

/**
 * Tests UDPComm.handleInterestCacheRequest() functionality.
 */
describe('UDPComm', function(){
    describe('#handleInterestCacheRequest()', function(){
        it('TODO - document what this function should return', function(done){

            // 1. handle with empty cache

            // 2. handle with non empty cache

            // 3. handle with non-empty pit

            // 4. handle with empty pit

            // 5. test bad data

            // TODO - more robust testing

            done(); // the invocation of done() tells testing framework that all tests are complete

        })
    })
});

/**
 * Tests UDPComm.handleCacheData() functionality.
 */
describe('UDPComm', function(){
    describe('#handleCacheData()', function(){
        it('TODO - document what this function should return', function(done){

            // 1. test bad input

            // 2. handle with empty cache for this data

            // 3. handle with non-empty cache for this data (update)

            // 4. handle with pit requests

            // 4. handle without pit requests

            // TODO - more robust testing

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});

/**
 * Tests UDPComm.handleFIBData() functionality.
 */
describe('UDPComm', function() {
    describe('#handleFIBData', function () {
        it('TODO - document what this function should return', function (done) {

            // TODO - more robust testing

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});

/**
 * Tests UDPComm.isValidForTimeInterval() functionality.
 */
describe('UDPComm', function(){
    describe('#isValidForTimeInterval', function(){
        it('returns true if valid time interval, otherwise false', function(done){
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
