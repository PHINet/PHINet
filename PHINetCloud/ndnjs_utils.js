/**
 * Uses NDN-js library to create and manipulate NDN-compliant packets
 * Source: https://github.com/named-data/ndn-js
 */

var Data = require('./ndnjs/data.js').Data;
var Exclude = require('./ndnjs/exlude.js').Exclude;
var Interest = require('./ndnjs/interest.js').Interest;
var KeyLocator = require('./ndnjs/key-locator.js').KeyLocator;
var MetaInfo = require('./ndnjs/meta-info.js').MetaInfo;
var Name = require('./ndnjs/name.js').Name;
var Sha256WithRsaSignature = require('./ndnjs/sha256-with-rsa-signature.js').Sha256WithRsaSignature;
//var EncodingException = require('./ndnjs/encoding/encoding-exception.js').EncodingException;

exports.ndn_utils = {


    DEFAULT_FRESHNESS_PERIOD : 1000 * 60 * 60, // an hour

    /**
     * Returns decoded Interest packet
     *
     * @param byteBuffer - an encoded Interest packet
     * @return Interest packet if input valid; otherwise, false
     */
    decodeInterest: function decodeInterest(encodedInterest) {
        var interest = new Interest();

        interest.wireDecode(encodedInterest);

        return interest;
    },

    /**
     *
     *      * TODO - doc
     *
     * @param bb
     * @return
     */
    decodeData: function(encodedData) {
        var data = new Data();

        data.wireDecode(encodedData);

        return data;
    },

    /**
     *
     *      * TODO - doc
     *
     * @param data
     * @param name
     * @param locatorType
     * @return
     */
    createKeyLocator : function(data, name, locatorType) {
        var keyLocator = new KeyLocator();

        keyLocator.setKeyData(data);
        keyLocator.setKeyName(name);
        keyLocator.setType(locatorType);

        return keyLocator;
    },

    /**
     *
     *      * TODO - doc
     *
     * @param finalBlockID
     * @param freshnessPeriod
     * @param cType
     * @return
     */
    createMetaInfo: function(finalBlockID, freshnessPeriod, cType) {

        var metaInfo = new MetaInfo();

        metaInfo.setFreshnessPeriod(freshnessPeriod);
        metaInfo.setFinalBlockId(finalBlockID);
        metaInfo.setType(cType);

        return metaInfo;
    },

    /**
     *      * TODO - doc
     *
     * uses default values
     */
    createMetaInfo: function() {

        var metaInfo = new MetaInfo();

        // TODO - set final block id
        metaInfo.setFreshnessPeriod(this.DEFAULT_FRESHNESS_PERIOD);
      //  TODO- metaInfo.setType(ContentType.BLOB) // blob is the default type

    },

    /**
     * Creates application-specific name
     *
     *      * TODO - doc
     *
     * @param userID
     * @param sensorID
     * @param timeString
     * @param processID
     * @return
     */
    createName: function(userID, sensorID, timeString, processID) {

        var name = "/ndn/" + userID + "/" + sensorID + "/" + timeString + "/" + processID;

        return new Name(name);

    },

    /**
     *
     * @param content
     * @param metaInfo
     * @param name
     * @return
     */
    createDataPacket: function(content, metaInfo, name) {
        // TODO - how to use signature?

        var signature = new Sha256WithRsaSignature();

        var data = new Data();

        // TODO - set content data.setContent(new Blob(content));
        data.setMetaInfo(metaInfo);
        data.setName(new Name(name));
        data.setSignature(signature);

        return data;
    },

    /**
     * Uses the default values for MetaInfo
     *
     *      * TODO - doc
     *
     * @param content
     * @param metaInfo
     * @param name
     * @return
     */
    createDataPacket: function(content, name) {
        // TODO - how to use signature?

        var signature = new Sha256WithRsaSignature();

        var data = new Data();

        // TODO - set content data.setContent(new Blob(content));
        data.setMetaInfo(this.createMetaInfo());
        data.setName(new Name(name));
        data.setSignature(signature);

        return data;
    },

    /**
     *
     *  TODO - doc
     *
     * @param childSelector
     * @param interestLifetimeMillis
     * @param keyLocator
     * @param mustBeFresh
     * @param maxSuffixComponents
     * @param minSuffixComponents
     * @param name
     * @param scope
     * @return
     */
    createInterestPacket: function(childSelector, interestLifetimeMillis, keyLocator, mustBeFresh, maxSuffixComponents,
                minSuffixComponents, name, scope) {

        var exclude = new Exclude(); // TODO - how to use exclude

        var interest = new Interest();

        interest.setChildSelector(childSelector);
        interest.setExclude(exclude);
        interest.setInterestLifetimeMilliseconds(interestLifetimeMillis);
        interest.setKeyLocator(keyLocator);
        interest.setMustBeFresh(mustBeFresh);
        interest.setMaxSuffixComponents(maxSuffixComponents);
        interest.setMinSuffixComponents(minSuffixComponents);
        interest.setName(new Name(name));
        interest.setScope(scope);

        return interest;
    },

    /**
     * the default interest
     *
     * TODO - doc
     *
     * @param name
     * @return
     */
    createInterestPacket: function(name) {
        return new Interest(name);

    }
};