/**
 * File provides test cases for the Pending Interest Table implemented in pit.js.
 */

var expect = require("chai").expect;
var LoginDB = require('../usercredentials.js').LoginCredentials();
var StringConst = require('../string_const').StringConst;
var UserClass = require('../user');

// --- test entries ---

var user1 = UserClass.User();
user1.user("uid111", "pw1", "email1",StringConst.DOCTOR_ENTITY);

var user2 = UserClass.User();
user2.user("uid222", "pw2", "email2",StringConst.PATIENT_ENTITY);


// --- test entries ---

/**
 * Tests LoginCredentials.insertNewUser() functionality.
 */
describe('LoginCredentials', function(){
    describe('#insertNewUser()', function(){
        it('returns true of insertion was successful, otherwise returns false', function(done) {

            // delete first, before tests
            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

            });

            LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

            });

            // test insert on bad data
            expect(LoginDB.insertNewUser(null, null, null, null)).to.equal(false); // rejected due to null params
            expect(LoginDB.insertNewUser(user2.getUserID(), user2.getPassword(), user2.getEmail(),
                                        "INVALID ENTITY TYPE: ERROR!", function(){})); // rejected due to invalid entity type

            // test insert on good data

            LoginDB.insertNewUser(user1.getUserID(), user1.getPassword(), user1.getEmail(),
                user1.getEntityType(), function(rowsTouched){

                    expect(rowsTouched === 1).to.equal(true);
                });

            LoginDB.insertNewUser(user2.getUserID(), user2.getPassword(), user2.getEmail(),
                user2.getEntityType(), function(rowsTouched){

                    expect(rowsTouched === 1).to.equal(true);
                });

            // test that redundant data is rejected
            LoginDB.insertNewUser(user2.getUserID(), user2.getPassword(), user2.getEmail(),
                user2.getEntityType(), function(rowsTouched){

                    expect(rowsTouched === 0).to.equal(true);
                });

            // delete before exiting tests

            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

            });

            LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                done();
            });
        })
    })
});

/**
 * Tests LoginCredentials.getUser() functionality.
 */
describe('LoginCredentials', function(){
    describe('#getUser()', function(){
        it('returns true if get was valid, otherwise false', function(done) {

            // delete first, before tests
            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

            });

            LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

            });

            // insert data before testing get
            LoginDB.insertNewUser(user1.getUserID(), user1.getPassword(), user1.getEmail(),
                user1.getEntityType(), function(rowsTouched){

                    expect(rowsTouched === 1).to.equal(true);
                });

            LoginDB.insertNewUser(user2.getUserID(), user2.getPassword(), user2.getEmail(),
                user2.getEntityType(), function(rowsTouched){

                    expect(rowsTouched === 1).to.equal(true);
                });


            // test get on bad data
            expect(LoginDB.getUser(null,null)).to.equal(false);

            // test get on good data
            LoginDB.getUser(user1.getUserID(), function(rowsTouched, queryResult) {

                expect(rowsTouched === 1).to.equal(true);

                expect(queryResult.getUserID() === user1.getUserID()).to.equal(true);
                expect(queryResult.getPassword() === user1.getPassword()).to.equal(true);

            });

            LoginDB.getUser(user2.getUserID(), function(rowsTouched, queryResult) {

                expect(rowsTouched === 1).to.equal(true);

                expect(queryResult.getUserID() === user2.getUserID()).to.equal(true);
                expect(queryResult.getPassword() === user2.getPassword()).to.equal(true);

            });

            // delete before exiting tests

            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

            });

            LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                done();
            });
        })
    })
});

/**
 * Tests LoginCredentials.updateUser() functionality.
 */
describe('LoginCredentials', function(){
    describe('#updateUser()', function(){
        it('returns true if update was successful, otherwise false', function(done) {

            /// delete first, before tests
            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

            });

            LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

            });

            // insert data before testing update
            LoginDB.insertNewUser(user1.getUserID(), user1.getPassword(), user1.getEmail(),
                user1.getEntityType(), function(rowsTouched){

                    expect(rowsTouched === 1).to.equal(true);
                });

            LoginDB.insertNewUser(user2.getUserID(), user2.getPassword(), user2.getEmail(),
                user2.getEntityType(), function(rowsTouched){

                    expect(rowsTouched === 1).to.equal(true);
                });

            // update data locally
            user1.setPassword("NEW_PASSWORD1");

            user2.setPassword("NEW_PASSWORD2");
            user2.setEntityType(StringConst.DOCTOR_ENTITY); // test valid entity update

            // test update on bad data
            expect(LoginDB.updateUser(null, null, null, null, null)).to.equal(false);

            // test that bad entity update is rejected
            expect(LoginDB.updateUser(user1.getUserID(), user1.getPassword(),
                                user1.getEmail(), "INVALID ENTITY TYPE: ERRROR!", function(){})).to.equal(false);

            // test that valid update is accepted
            LoginDB.updateUser(user2.getUserID(), user2.getPassword(), user2.getEmail(), user2.getEntityType(),

                function(rowsTouched) {
                    expect(rowsTouched === 1).to.equal(true); // verify that 1 row was updated
                });

            // get user to verify update took place
            LoginDB.getUser(user2.getUserID(), function(rowsTouched, queryResult) {

                expect(rowsTouched === 1).to.equal(true);

                expect(queryResult.getUserID() === user2.getUserID()).to.equal(true);
                expect(queryResult.getPassword() === user2.getPassword()).to.equal(true);
                expect(queryResult.getEntityType() === user2.getEntityType()).to.equal(true);
            });

            // delete before exiting tests
            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

            });

            LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                done();
            });
        })
    })
});

/**
 * Tests LoginCredentials.deleteUser() functionality.
 */
describe('LoginCredentials', function(){
    describe('#deleteUser()', function(){
        it('returns true if deletion was successful, otherwise false', function(done) {
            //// delete first, before tests
            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

            });

            LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {
                expect(rowsTouched <= 1).to.equal(true); // verify that one or fewer rows were touched

            });

            // insert data before testing deletion
            LoginDB.insertNewUser(user1.getUserID(), user1.getPassword(), user1.getEmail(),
                user1.getEntityType(), function(rowsTouched){

                    expect(rowsTouched === 1).to.equal(true);
                });

            LoginDB.insertNewUser(user2.getUserID(), user2.getPassword(), user2.getEmail(),
                user2.getEntityType(), function(rowsTouched){

                    expect(rowsTouched === 1).to.equal(true);
                });

            // test deletion on bad data
            expect(LoginDB.deleteUser(null, null)).to.equal(false);

            // test deletion on good data
            LoginDB.deleteUser(user1.getUserID(), function(rowsTouched) {

                expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted
            });

            LoginDB.deleteUser(user2.getUserID(), function(rowsTouched) {

                expect(rowsTouched === 1).to.equal(true); // verify that one row was deleted

                done();
            });
        })
    })
});

