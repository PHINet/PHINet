/**
 *
 */

var expect = require("chai").expect;

var PIT = require('../PIT.js').PIT();
var DBDataClass = require('../data');
var StringConst = require('../string_const').StringConst;

// --- test entries ---

var entry1 = DBDataClass.DATA();
entry1.pitData("serverTestUser1", "serverTestSensor1",
		StringConst.DATA_CACHE, StringConst.CURRENT_TIME, "10.12.13.14");

var entry2 = DBDataClass.DATA();
entry2.pitData("serverTestUser2", "serverTestSensor2",
		StringConst.DATA_CACHE, StringConst.CURRENT_TIME, "10.88.11.22");

// --- test entries ---

describe('PIT', function(){
    describe('#addPITData()', function(){
        it('should return true if entry has been made', function(){
            // TODO - tests
        })
    })
})

describe('PIT', function(){
    describe('#updatePITData()', function(){
        it('should return true if update has been made', function(){
            // TODO - tests
        })
    })
})

describe('PIT', function(){
    describe('#deletePITData()', function(){
        it('should return true if deletion has been made', function(){

            // TODO - tests
        })
    })
})

describe('PIT', function(){
    describe('#getGeneralPITData()', function(){
        it('should return array of data if userid valid, otherwise false', function(){

        	// TODO - tests

        })
    })
})

describe('PIT', function(){
    describe('#getSpecificPITData()', function(){
        it('should return a specific entry, otherwise false', function(){

        	// TODO - tests

        })
    })
})
