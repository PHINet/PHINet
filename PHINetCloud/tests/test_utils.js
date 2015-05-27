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