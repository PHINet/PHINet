
/** 
 * File contains code for creating 
 * NDN-compliant data packets
 **/

var nameField = require('./namefield');


exports.dataPacket = function() {
	// NOTE: contents will be returned when 
	// other modules "require" this 


	function dataPacket(name, content) {
    	this.nameField = nameField.nameField("");
    	this.NON_NEG_INT_CONST = 0; // TODO - rework
    	this.content = content; // TODO - rework
    }

	/**
     MetaInfo ::=
     --------------
     META-INFO-TYPE TLV-LENGTH
     ContentType?
     FreshnessPeriod?
     FinalBlockId?
     --------------
     **/
    function createMetaInfo() {
        //String content = createContentType();
        // TODO - later content += " " + createFreshnessPeriod()
        // TODO - later content += " " + createFinalBlockId()

        return "META-INFO-TYPE 0";
    }

    /**
     ContentType ::=
     --------------
     CONTENT-TYPE-TYPE TLV-LENGTH
     nonNegativeInteger
     --------------
     **/
    function createContentType() {
        var content = "";//Integer.parseInt(NON_NEG_INT_CONST);
        return "CONTENT-TYPE-TYPE " + (content.length).toString() + " " + content;
    }

    /**
     FreshnessPeriod ::=
     --------------
     FRESHNESS-PERIOD-TLV TLV-LENGTH
     nonNegativeInteger
     --------------
     **/
    function createFreshnessPeriod() {
        var content = Integer.toString(NON_NEG_INT_CONST); // TODO - rework

        return "FRESHNESS-PERIOD-TLV " + (content.length).toString() + " " + content;
    }

    /**
     FinalBlockId ::=
     --------------
     FINAL-BLOCK-ID-TLV TLV-LENGTH
     NameComponent
     --------------
     **/
    function createFinalBlockId() {
        var content = nameField.createNameComponent();

        return "FINAL-BLOCK-ID-TLV " + (content.length).toString() + " " + content;
    }

    /**
     Content ::=
     --------------
     CONTENT-TYPE TLV-LENGTH Byte*
     --------------
     **/
    function createContent() {
        var content = this.content; // TODO - generate content
        return "CONTENT-TYPE " + (content.length).toString() + " " + content;
    }

    /**
     Signature ::==
     --------------
     SignatureInfo SignatureBits
     --------------
     **/
    function createSignature() {

        // TODO - rework
        var signatureBits = " "; // TODO - rework
        return createSignatureInfo() + " " + signatureBits;
    }

    /**
     SignatureInfo ::==
     --------------
     SIGNATURE-INFO-TYPE TLV-LENGTH
     SignatureType
     KeyLocator?
     --------------
     **/
    function createSignatureInfo() {
        var content = createSignatureType();
        // TODO - later content += " " + createKeyLocator()
        return "SIGNATURE-INFO-TYPE " + (content.length).toString() + " " + content;
    }

    /**
     SignatureType ::==
     --------------
     SIGNATURE-TYPE-TYPE TLV-LENGTH
     nonNegativeInteger
     --------------
     **/
    function createSignatureType() {
        var content = "0"; // TODO - rework later
        return "SIGNATURE-TYPE-TYPE " + (content.length).toString() + " " + content;
    }

    /**
     DATA ::=
     --------------
     DATA-TLV TLV-LENGTH
     Name
     MetaInfo?
     Content
     Signature
     --------------
     **/
    function createDATA() {
        var content = nameField.createName();
        content += " " + createMetaInfo();
        content += " " + createContent();
        content += " " + createSignature();

        return "DATA-TLV " + (content.length).toString() + " " + content;
    }

	function toString() {
        return createDATA();
    }

}