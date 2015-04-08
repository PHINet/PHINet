/**
 *
 */

var expect = require("chai").expect;
var FIB = require('../fib.js').FIB();
var DBDataClass = require('../data');
var StringConst = require('../string_const').StringConst;

// --- test entries ---

var entry1 = DBDataClass.DATA();
entry1.fibData("serverTestUser1", StringConst.CURRENT_TIME, "10,11,12,13,14,15");

var entry2 = DBDataClass.DATA();
entry2.fibData("serverTestUser2", StringConst.CURRENT_TIME, "10,11,12,13,14,15");

// --- test entries ---

/**
 *
 */
describe('FIB', function(){
    describe('#addFIBData()', function(){
        it('should return true if entry has been made', function(){
                // TODO - tests
        })
    })
})

/**
 *
 */
describe('FIB', function(){
    describe('#updateFIBData()', function(){
        it('should return true if update has been made', function(){
            // TODO - tests
        })
    })
})

/**
 *
 */
describe('FIB', function(){
    describe('#deleteFIBData()', function(){
        it('should return true if deletion has been made', function(){

            // TODO - tests
        })
    })
})

/**
 *
 */
describe('FIB', function(){
    describe('#getGeneralFIBData()', function(){
        it('should return array of data if userid valid, otherwise false', function(){

        	// TODO - tests

        })
    })
})

/**
 *
 */
describe('FIB', function(){
    describe('#getSpecificFIBData()', function(){
        it('should return a specific entry, otherwise false', function(){

        	// TODO - tests

        })
    })
})
