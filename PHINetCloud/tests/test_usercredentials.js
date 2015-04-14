/**
 * File provides test cases for the Pending Interest Table implemented in pit.js.
 */

var expect = require("chai").expect;
var LoginDB = require('../usercredentials.js').LoginCredentials();
var StringConst = require('../string_const').StringConst;
var UserClass = require('./user');

// --- test entries ---

var user1 = UserClass.User();
user1.user("uid1", "pw1", "email1",StringConst.DOCTOR_ENTITY);

var user2 = UserClass.User();
user2.user("uid2", "pw2", "email2",StringConst.DOCTOR_ENTITY);

var user3 = UserClass.User();
user3.user("uid1", "pw1", "email1", "INVALID ENTITY TYPE: ERROR!!");

// --- test entries ---

/**
 * Tests LoginCredentials.insertNewUser() functionality.
 */
describe('LoginCredentials', function(){
    describe('#insertNewUser()', function(){
        it('returns true of insertion was successful, otherwise returns false', function(done) {

            // delete first

            // test insert on bad data

            // test insert on good data

            // delete before exiting tests



            // TODO - test bad type

                // TODO -
        })
    })
});

/**
 * Tests LoginCredentials.getUser() functionality.
 */
describe('LoginCredentials', function(){
    describe('#getUser()', function(){
        it('returns true if get was valid, otherwise false', function(done) {
            // TODO -

            // delete first

            // insert data before testing get

            // test get on bad data

            // test get on good data

            // delete before exiting tests

        })
    })
});

/**
 * Tests LoginCredentials.updateUser() functionality.
 */
describe('LoginCredentials', function(){
    describe('#updateUser()', function(){
        it('returns true if update was successful, otherwise false', function(done) {
            // TODO -

            // delete first

            // insert data before testing update

            // update data locally

            // update data in db

            // test update on bad data

            // delete before exiting tests

        })
    })
});

/**
 * Tests LoginCredentials.deleteUser() functionality.
 */
describe('LoginCredentials', function(){
    describe('#deleteUser()', function(){
        it('returns true if deletion was successful, otherwise false', function(done) {
            // TODO -

            // delete first

            // insert data before testing delete

            // test delete on bad data

            // test delete on good data

        })
    })
});

