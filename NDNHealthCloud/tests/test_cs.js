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

describe('ContentStore', function(){
    describe('#addCSData()', function(){
        it('should return true if entry has been made', function(){

        	// test good input
            expect(ContentStore.addCSData(entry1)).to.equal(true);
            expect(ContentStore.addCSData(entry2)).to.equal(true);

            // test clearly bad input
            expect(ContentStore.addCSData(null)).to.equal(false); 
            expect(ContentStore.addCSData(undefined)).to.equal(false);
            expect(ContentStore.addCSData(DBDataClass.DATA())).to.equal(false); // an "empty" object

            // TODO - run this test
            // test redundant input
            //expect(ContentStore.addCSData(entry1)).to.equal(false);
        })
    })
})

describe('ContentStore', function(){
    describe('#updateCSData()', function(){
        it('should return true if update has been made', function(){

        	entry1.setDataFloat("100,101,102");

        	// test good input
        	expect(ContentStore.updateCSData(entry1)).to.equal(true);
        	expect(ContentStore.updateCSData(entry2)).to.equal(true);

        	// test bad input
        	expect(ContentStore.updateCSData(null)).to.equal(false);
        	expect(ContentStore.updateCSData(undefined)).to.equal(false);
        	expect(ContentStore.updateCSData(DBDataClass.DATA())).to.equal(false); // an "empty" object
        })
    })
})

describe('ContentStore', function(){
    describe('#deleteCSData()', function(){
        it('should return true if deletion has been made', function(){

        	// NOTE: test assumes "entry1" and "entry2" have already been entered

        	// test good input
        	expect(ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString())).to.equal(true);
        	expect(ContentStore.deleteCSData(entry2.getUserID(), entry2.getTimeString())).to.equal(true);

        	// test bad input
        	expect(ContentStore.deleteCSData(entry1.getUserID(), entry1.getTimeString())).to.equal(false); // object doesn't exist
        	expect(ContentStore.updateCSData(undefined, undefined)).to.equal(false);
        })
    })
})

describe('ContentStore', function(){
    describe('#getGeneralCSData()', function(){
        it('should return array of data if userid valid, otherwise false', function(){

        	// TODO - tests

        })
    })
})

describe('ContentStore', function(){
    describe('#getSpecificCSData()', function(){
        it('should return a specific entry, otherwise false', function(){

        	// TODO - tests

        })
    })
})