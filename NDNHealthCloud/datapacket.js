
/** 
 * File contains code for creating 
 * NDN-compliant data packets
 **/

var NameField = require('./namefield').NameField;


exports.DataPacket = {
	// NOTE: contents will be returned when 
	// other modules "require" this 

    DataPacket: function (userDataID, sensorID, timestring, processID, content) {
        this.nameField = new NameField.NameField(userDataID, sensorID, timestring, processID, content);
        this.content = content;
    },

	/**
     MetaInfo ::=
     --------------
     META-INFO-TYPE TLV-LENGTH
     ContentType?
     FreshnessPeriod?
     FinalBlockId?
     --------------
     **/
    createMetaInfo: function () {
        //String content = createContentType();
        // TODO - later content += " " + createFreshnessPeriod()
        // TODO - later content += " " + createFinalBlockId()

        return "META-INFO-TYPE 0";
    },

    /**
     ContentType ::=
     --------------
     CONTENT-TYPE-TYPE TLV-LENGTH
     nonNegativeInteger
     --------------
     **/
    createContentType: function () {
        var content = "";//Integer.parseInt(NON_NEG_INT_CONST);
        return "CONTENT-TYPE-TYPE " + (content.length).toString() + " " + content;
    },

    /**
     FreshnessPeriod ::=
     --------------
     FRESHNESS-PERIOD-TLV TLV-LENGTH
     nonNegativeInteger
     --------------
     **/
    createFreshnessPeriod: function () {
        var content = Integer.toString(NON_NEG_INT_CONST); // TODO - rework

        return "FRESHNESS-PERIOD-TLV " + (content.length).toString() + " " + content;
    },

    /**
     FinalBlockId ::=
     --------------
     FINAL-BLOCK-ID-TLV TLV-LENGTH
     NameComponent
     --------------
     **/
    createFinalBlockId: function () {
        var content = nameField.createNameComponent();

        return "FINAL-BLOCK-ID-TLV " + (content.length).toString() + " " + content;
    },

    /**
     Content ::=
     --------------
     CONTENT-TYPE TLV-LENGTH Byte*
     --------------
     **/
    createContent: function () {
        var content = this.content; // TODO - generate content
        return "CONTENT-TYPE " + (content.length).toString() + " " + content;
    },

    /**
     Signature ::==
     --------------
     SignatureInfo SignatureBits
     --------------
     **/
    createSignature: function () {

        // TODO - rework
        var signatureBits = " "; // TODO - rework
        return createSignatureInfo() + " " + signatureBits;
    },

    /**
     SignatureInfo ::==
     --------------
     SIGNATURE-INFO-TYPE TLV-LENGTH
     SignatureType
     KeyLocator?
     --------------
     **/
    createSignatureInfo: function () {
        var content = createSignatureType();
        // TODO - later content += " " + createKeyLocator()
        return "SIGNATURE-INFO-TYPE " + (content.length).toString() + " " + content;
    },

    /**
     SignatureType ::==
     --------------
     SIGNATURE-TYPE-TYPE TLV-LENGTH
     nonNegativeInteger
     --------------
     **/
    createSignatureType: function () {
        var content = "0"; // TODO - rework later
        return "SIGNATURE-TYPE-TYPE " + (content.length).toString() + " " + content;
    },

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
    createDATA: function () {
        var content = nameField.createName();
        content += " " + createMetaInfo();
        content += " " + createContent();
        content += " " + createSignature();

        return "DATA-TLV " + (content.length).toString() + " " + content;
    },

	toString: function () {
        return createDATA();
    }

};