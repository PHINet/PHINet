
/** 
 * File contains code for creating 
 * NDN-compliant interest packets
 **/

var nameField = require('./namefield');

exports.interestPacket = function() {
   

    // TODO - add more relevant params
    function interestPacket(name) {
        this.nameField = nameField.nameField(""); // TODO - pass real name
        this.NON_NEG_INT_CONST = 0; // TODO - rework
    }

    /**
    Nonce ::=
    --------------
    NONCE-TYPE TLV-LENGTH(=4) BYTE{4}
    --------------

     "The Nonce carries a randomly-generated 4-octet long byte-string.
     The combination of Name and Nonce should uniquely identify an Interest packet.
     This is used to detect looping Interests."
    **/
    function createNonce() {

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
    }
    
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
    function createSelectors() {
        var content = createMinSuffixComponents();
        content += " " + createMaxSuffixComponents();
        content += " " + createPublisherPublicKeyLocator();
        content += " " + createExclude();
        content += " " + createChildSelector();
        content += " " + createMustBeFresh();

        return "SELECTORS-TYPE " + (content.length).toString() + " " + content;
    }

    /**
     MinSuffixComponents ::=
     --------------
     MIN-SUFFIX-COMPONENTS-TYPE TLV-LENGTH
     nonNegativeInteger
     --------------
     */
    function createMinSuffixComponents() {
        var content = "0"; // TODO - rework later
        return "MIN-SUFFIX-COMPONENTS-TYPE " + (content.length).toString() + " " + content;
    }

    /**
     MaxSuffixComponents ::=
     --------------
     MAX-SUFFIX-COMPONENTS-TYPE TLV-LENGTH
     nonNegativeInteger
     --------------
     */
    function createMaxSuffixComponents() {
        var content = "0"; // TODO - rework later
        return "MAX-SUFFIX-COMPONENTS-TYPE " + (content.length).toString() + " " + content;
    }

    /**
     PublisherPublicKeyLocator ::=
     --------------
     KeyLocator
     --------------
     */
    function createPublisherPublicKeyLocator() {
        return ""; // TODO - rework later
    }

    /**
     Any ::=
     --------------
     ANY-TYPE TLV-LENGTH(=0)
     --------------
     */
    function createAny() {
        return "ANY-TYPE 0"; // TODO - rework
    }

    /**
     Exclude ::=
     --------------
     EXCLUDE-TYPE TLV-LENGTH Any? (NameComponent (Any)?)+
     --------------
     */
    function createExclude() {
        return "EXCLUDE-TYPE 0"; // TODO - rework later
    }

    /**
     ChildSelector ::=
     --------------
     CHILD-SELECTOR-TYPE TLV-LENGTH
     nonNegativeInteger
     --------------
     */
    function createChildSelector() {
        var content = "0"; // TODO - rework
        return "CHILD-SELECTOR-TYPE " + (content.length).toString() + " " + content;
    }

    /**
     MustBeFresh ::=
     --------------
     MUST-BE-FRESH-TYPE TLV-LENGTH(=0)
     --------------
     */
    function createMustBeFresh() {
        return "MUST-BE-FRESH-TYPE 0"; // TODO - rework
    }

    /**
    InterestLifeTime ::=
    --------------
    INTEREST-LIFETIME-TYPE TLV-LENGTH nonNegativeInteger
    --------------
    **/
    function createInterestLifetime() {
        var content = "0"; // TODO - rework
        return "INTEREST-LIFETIME-TYPE " + (content.length).toString() + " " + content;
    }

    /**
    Scope ::=
    --------------
    SCOPE-TYPE TLV-LENGTH nonNegativeInteger
    --------------
    */
    function createScope() {
        var content = "0"; // TODO - rework
        return "SCOPE-TYPE " + (content.length).toString() + " " + content;
    }

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
    function createINTEREST() {
        var content = nameField.createName();
        content += " " + createSelectors();
        content += " " +  createNonce();
        content += " " + createScope();
        content += " " + createInterestLifetime();

        return "INTEREST-TYPE " + (content.length).toString() + " " + content;
    }

    function toString() {
        return createINTEREST();
    }
}