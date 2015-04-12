/**
 * File provides test cases for Content Store implemented in cs.js.
 */

var expect = require("chai").expect;

var ContentStore = require('../cs.js').CS();
var DBDataClass = require('../data');
var StringConst = require('../string_const').StringConst;

// --- test entries ---

var entry1 = DBDataClass.DATA();
entry1.csData("serverTestUser1", "serverTestSensor1", 
		StringConst.DATA_CACHE, StringConst.CURRENT_TIME, "10,11,12,13,14,15");

var entry2 = DBDataClass.DATA();
entry2.csData("serverTestUser2", "serverTestSensor2", 
		StringConst.DATA_CACHE, StringConst.CURRENT_TIME, "10,11,12,13,14,15");

// --- test entries ---

/**
 * Tests ContentStore.insertCSData() functionality.
 */
describe('ContentStore', function(){
    describe('#insertCSData()', function(){
        it('should return true if entry has been made', function(done) {

            // first delete entries before testing addition
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(), function (rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });
            ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            // test clearly bad input; should be rejected at start
            expect(ContentStore.insertCSData(null)).to.equal(false);
            expect(ContentStore.insertCSData(undefined)).to.equal(false);
            expect(ContentStore.insertCSData(DBDataClass.DATA())).to.equal(false); // "empty" object should fail

            // test good input
            ContentStore.insertCSData(entry1, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            ContentStore.insertCSData(entry2, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            // test redundant input
            ContentStore.insertCSData(entry1, function(rowsTouched) {
                 expect(rowsTouched === 0).to.equal(true);
             });

            // finally, delete entries after testing addition
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true);
            });

            ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true);

                done(); // the invocation of done() tells testing framework that all tests are complete
            });
        })
    })
});

/**
 * Tests ContentStore.updateCSData() functionality.
 */
describe('ContentStore', function(){
    describe('#updateCSData()', function(){
        it('should return true if update has been made', function(done){

            // first delete (potentially stored) entries before testing update
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(), function (rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });
            ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that 1 or fewer rows were deleted
            });

            // now, place data into DB
            ContentStore.insertCSData(entry1, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            ContentStore.insertCSData(entry2, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was modified
            });

            // test rejection given bad input
            expect(ContentStore.updateCSData(null)).to.equal(false);
            expect(ContentStore.updateCSData(undefined)).to.equal(false);
            expect(ContentStore.updateCSData(DBDataClass.DATA())).to.equal(false); // an "empty" object

            // modify data
            entry1.setDataFloat("100,101,102");

            // test valid update given good input
            ContentStore.updateCSData(entry1, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was updated
            });

            ContentStore.updateCSData(entry2, function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that 1 row was updated

                done(); // the invocation of done() tells testing framework that all tests are complete
            });
        })
    })
});

/**
 * Tests ContentStore.deleteCSData() functionality.
 */
describe('ContentStore', function(){
    describe('#deleteCSData()', function(){
        it('should return true if deletion has been made', function(done){

            // before deletion test, attempt to input data into CS
            ContentStore.insertCSData(entry1, function(rowsTouched){});
            ContentStore.insertCSData(entry2, function(rowsTouched){});

            // test deletion given good input
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(),
                function(rowsTouched) {
                    expect(rowsTouched === 1).to.equal(true); // verify deletion of 1 row
                });

            ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString(),
                function(rowsTouched) {
                    expect(rowsTouched === 1).to.equal(true); // verify deletion of 1 row
                });

            // test deletion given bad input
            expect(ContentStore.updateCSData(undefined, undefined)).to.equal(false);

            // object no longer exists in DB, verify that no rows were modified
            ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString(),
                function(rowsTouched) {
                    expect(rowsTouched === 0).to.equal(true);

                    done(); // the invocation of done() tells testing framework that all tests are complete
                });
        })
    })
});

/**
 * Tests ContentStore.getGeneralCSData() functionality.
 */
describe('ContentStore', function(){
    describe('#getGeneralCSData()', function(){
        it('should return array of data if userid valid, otherwise false', function(){

            // TODO - tests

        })
    })
});

/**
 * Tests ContentStore.getSpecificCSData() functionality.
 */
describe('ContentStore', function(){
    describe('#getSpecificCSData()', function(){
        it('should return a specific entry, otherwise false', function(){

            // TODO - tests

        })
    })
});