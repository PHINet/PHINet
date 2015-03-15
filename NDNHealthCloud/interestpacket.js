/** 
 * File contains code for creating 
 * NDN-compliant interest packets
 **/

var NameField = require('./namefield');
var StringConst = require('./string_const').StringConst;
var Utils = require('./utils').Utils;

exports.InterestPacket = function () {
   
   return { 

        LIFETIME_CONST : 100,

        InterestPacket: function (userDataID, sensorID, timestring, processID, ipAddr) {

            console.log("TOIMESTRING: " + timestring);
            console.log("string const current time: " + StringConst.CURRENT_TIME);

             // if current time requested, provide it
            if (timestring === StringConst.CURRENT_TIME) {
                console.log("if");
                timestring = Utils.getCurrentTime();
            } else {
                console.log("else");
            }

            this.nameField = NameField.NameField();
            this.nameField.NameField(userDataID, sensorID, timestring, processID, ipAddr);
            this.NON_NEG_INT_CONST = 0; // TODO - rework
        },

        /**
        Nonce ::=
        --------------
        NONCE-TYPE TLV-LENGTH(=4) BYTE{4}
        --------------

         "The Nonce carries a randomly-generated 4-octet long byte-string.
         The combination of Name and Nonce should uniquely identify an Interest packet.
         This is used to detect looping Interests."
        **/
        createNonce: function () {

            var max = 255; // 2^8 -1 (max 8 bit number)
            var min = 0; // min unsigned 8 bit number
            var randomInt = Math.ceil(Math.random() * (max-min) + min);

            var content = randomInt.toString(); // first octet

            randomInt = Math.ceil(Math.random() * (max-min) + min);
            content += randomInt.toString(); // second octet

            randomInt = Math.ceil(Math.random() * (max-min) + min);
            content += randomInt.toString(); // third octet

            randomInt = Math.ceil(Math.random() * (max-min) + min);
            content += randomInt.toString(); // fourth octet

            return "NONCE-TYPE " + (content.length).toString() + " " + content;    
        },
        
        /**
        Selectors ::=
        --------------
         SELECTORS-TYPE TLV-LENGTH
         MinSuffixComponents?
         MaxSuffixComponents?
         PublisherPublicKeyLocator?
         Exclude?
         ChildSelector?
         MustBeFresh?
        --------------
        **/
        createSelectors: function () {
            var content = this.createMinSuffixComponents();
            content += " " + this.createMaxSuffixComponents();
            content += " " + this.createPublisherPublicKeyLocator();
            content += " " + this.createExclude();
            content += " " + this.createChildSelector();
            content += " " + this.createMustBeFresh();

            return "SELECTORS-TYPE " + (content.length).toString() + " " + content;
        },

        /**
         MinSuffixComponents ::=
         --------------
         MIN-SUFFIX-COMPONENTS-TYPE TLV-LENGTH
         nonNegativeInteger
         --------------
         */
        createMinSuffixComponents: function () {
            var content = "0"; // TODO - rework later
            return "MIN-SUFFIX-COMPONENTS-TYPE " + (content.length).toString() + " " + content;
        },

        /**
         MaxSuffixComponents ::=
         --------------
         MAX-SUFFIX-COMPONENTS-TYPE TLV-LENGTH
         nonNegativeInteger
         --------------
         */
        createMaxSuffixComponents: function () {
            var content = "0"; // TODO - rework later
            return "MAX-SUFFIX-COMPONENTS-TYPE " + (content.length).toString() + " " + content;
        },

        /**
         PublisherPublicKeyLocator ::=
         --------------
         KeyLocator
         --------------
         */
        createPublisherPublicKeyLocator: function () {
            return "null"; // TODO - rework later
        },

        /**
         Any ::=
         --------------
         ANY-TYPE TLV-LENGTH(=0)
         --------------
         */
        createAny: function () {
            return "ANY-TYPE 0"; // TODO - rework
        },

        /**
         Exclude ::=
         --------------
         EXCLUDE-TYPE TLV-LENGTH Any? (NameComponent (Any)?)+
         --------------
         */
        createExclude: function () {
            var content = this.createAny();
            return "EXCLUDE-TYPE 0 " + (content.length).toString() + " " + content; // TODO - rework later
        },

        /**
         ChildSelector ::=
         --------------
         CHILD-SELECTOR-TYPE TLV-LENGTH
         nonNegativeInteger
         --------------
         */
        createChildSelector: function () {
            var content = "0"; // TODO - rework
            return "CHILD-SELECTOR-TYPE " + (content.length).toString() + " " + content;
        },

        /**
         MustBeFresh ::=
         --------------
         MUST-BE-FRESH-TYPE TLV-LENGTH(=0)
         --------------
         */
        createMustBeFresh: function () {
            return "MUST-BE-FRESH-TYPE 0"; // TODO - rework
        },

        /**
        InterestLifeTime ::=
        --------------
        INTEREST-LIFETIME-TYPE TLV-LENGTH nonNegativeInteger
        --------------
        **/
        createInterestLifetime: function () {
            var content = this.LIFETIME_CONST.toString(); // TODO - add user-selected interval
            return "INTEREST-LIFETIME-TYPE " + (content.length).toString() + " " + content;
        },

        /**
        Scope ::=
        --------------
        SCOPE-TYPE TLV-LENGTH nonNegativeInteger
        --------------
        */
        createScope: function () {
            var content = "0"; // TODO - rework
            return "SCOPE-TYPE " + (content.length).toString() + " " + content;
        },

        /**
         INTEREST ::=
         --------------
         INTEREST-TYPE TLV-LENGTH
         Name
         Selectors?
         Nonce
         Scope?
         Interest Lifetime?
         --------------
         **/
        createINTEREST: function () {
            var content = this.nameField.createName();
            content += " " + this.createSelectors();
            content += " " +  this.createNonce();
            content += " " + this.createScope();
            content += " " + this.createInterestLifetime();

            return "INTEREST-TYPE " + (content.length).toString() + " " + content;
        },

        toString: function () {
            return this.createINTEREST();
        }
    }
};