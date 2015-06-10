/**
 * File provides test cases for Utility methods implemented in utils.js.
 */

var expect = require("chai").expect;
var utils = require('../utils').Utils;

/**
 * Tests Utils.comparePassword() functionality.
 */
describe('Utils', function() {
    describe('#comparePassword()', function () {
        it('method compares password against its encrypted version', function (done) {

            // --- test against bad input ---

            var exceptionThrown = false;

            try {
                utils.encryptPassword(null, null);
            } catch (e) {
                exceptionThrown = true;
            }

            expect(exceptionThrown).to.equal(true);
            exceptionThrown = false; // reset for future test

            try {
                utils.encryptPassword("one", null);
            } catch (e) {
                exceptionThrown = true;
            }

            expect(exceptionThrown).to.equal(true);
            exceptionThrown = false; // reset for future test

            try {
                utils.comparePassword(null);
            } catch (e) {
                exceptionThrown = true;
            }

            expect(exceptionThrown).to.equal(true);
            exceptionThrown = false; // reset for future test

            try {
                utils.comparePassword("null", "null", null);
            } catch (e) {
                exceptionThrown = true;
            }

            expect(exceptionThrown).to.equal(true);

            // --- test against bad input ---

            var password1 = "hunter2";

            utils.encryptPassword(password1, function(err, hash) {

                var hashedPW1 = hash;

                expect(hashedPW1 === password1).to.equal(false);

                utils.comparePassword(password1, hashedPW1, function(err, passwordsMatch) {

                    expect(passwordsMatch).to.equal(true);
                    done(); // the invocation of done() tells testing framework that all tests are complete
                })
            });
        })
    })
});

/**
 * Tests Utils.isValidForTimeInterval() functionality.
 */
describe('Utils', function(){
    describe('#isValidForTimeInterval()', function(){
        it('returns true if valid time interval, otherwise false', function(done){
            var goodRequestInterval = "2012-05-04T08.08.08.888||2014-05-04T08.08.08.888";
            var badRequestInterval = "2012-ERROR:08.888||2014-ERROR8.08.888";

            var goodDataInterval1 = "2012-07-04T08.08.08.888"; // date is within goodRequestInterval
            var goodDataInterval2 = "2012-01-04T08.08.08.888"; // date is before goodRequestInterval
            var goodDataInterval3 = "2014-07-04T08.08.08.888"; // date is after goodRequestInterval

            var testInterval1 = "2015-02-22T00.00.00.000||2015-04-22T00.00.00.000";
            var dataTime1 = "2015-03-22T22.58.10.878";

            // --- test bad input ---
            expect(utils.isValidForTimeInterval(null, null)).to.equal(false); // null entries

            // syntax error in request interval
            expect(utils.isValidForTimeInterval(badRequestInterval, goodDataInterval1)).to.equal(false);

            // two data intervals and no request interval
            expect(utils.isValidForTimeInterval(goodDataInterval1, goodDataInterval1)).to.equal(false);

            // --- test bad input ---

            // test input rejection if before interval
            expect(utils.isValidForTimeInterval(goodRequestInterval, goodDataInterval2)).to.equal(false);

            // test input rejection if after interval
            expect(utils.isValidForTimeInterval(goodRequestInterval, goodDataInterval3)).to.equal(false);

            // test input acceptance if during interval
            expect(utils.isValidForTimeInterval(goodRequestInterval, goodDataInterval1)).to.equal(true);
            expect(utils.isValidForTimeInterval(testInterval1, dataTime1)).to.equal(true);

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});

/**
 * Tests Utils.parseSynchData() functionality.
 */
describe('Utils', function(){
    describe('#parseSynchData()', function(){
        it('after parse, returns list of parsed data', function(done){

            // this input contains two data points from sensorID1 and one data point from sensorID2
            var goodInput = "sensorID2--100,1990-11-09T00.00.00.000::sensorID1--10,1990-11-06T00.00.00.000;;"
                                        + "15,1990-11-07T00.00.00.000;;99,1990-11-08T00.00.00.000";
            var badInput = "i1sdfpijajjsdf";

            var goodParsedInput = utils.parseSynchData("userID", goodInput);
            var badParsedOutput1 = utils.parseSynchData("userID", badInput);
            var badParsedOutput2 = utils.parseSynchData("", badInput);

            var foundSensorID1Count = 0;
            var foundSensorID2Count = 0;

            // check to see that both sensors were found
            for (var i = 0; i < goodParsedInput.length; i++) {
                if (goodParsedInput[i].getSensorID() === "sensorID2") {
                    foundSensorID2Count += 1;
                } else if (goodParsedInput[i].getSensorID() === "sensorID1") {
                    foundSensorID1Count += 1;
                }
            }

            // test good output found in right proportions
            expect(foundSensorID1 === 2 && foundSensorID2 === 1).to.equal(true);

            // test bad output
            expect(badParsedOutput1).to.equal([]);
            expect(badParsedOutput2).to.equal([]);

            done(); // the invocation of done() tells testing framework that all tests are complete
        })
    })
});

