package com.example.androidudpclient.Packet;


public class DataPacket {

    private NameField nameField;
    final int NON_NEG_INT_CONST = 0; // TODO - rework


    // TODO - add more relevant params
    public DataPacket() {
        nameField = new NameField(""); // TODO - pass real name
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
    String createMetaInfo() {
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
    String createContentType() {
        String content = "";//Integer.parseInt(NON_NEG_INT_CONST);
        return "CONTENT-TYPE-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     FreshnessPeriod ::=
     --------------
     FRESHNESS-PERIOD-TLV TLV-LENGTH
     nonNegativeInteger
     --------------
     **/
    String createFreshnessPeriod() {
        String content = Integer.toString(NON_NEG_INT_CONST); // TODO - rework

        return "FRESHNESS-PERIOD-TLV " + Integer.toString(content.length()) + " " + content;
    }

    /**
     FinalBlockId ::=
     --------------
     FINAL-BLOCK-ID-TLV TLV-LENGTH
     NameComponent
     --------------
     **/
    String createFinalBlockId() {
        String content = nameField.createNameComponent();

        return "FINAL-BLOCK-ID-TLV " + Integer.toString(content.length()) + " " + content;
    }

    /**
     Content ::=
     --------------
     CONTENT-TYPE TLV-LENGTH Byte*
     --------------
     **/
    String createContent() {
        String content = "88,75,80,95,84,78,100,82"; // TODO - generate content
        return "CONTENT-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     Signature ::==
     --------------
     SignatureInfo SignatureBits
     --------------
     **/

    String createSignature() {

        // TODO - rework
        String signatureBits = " "; // TODO - rework
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
    String createSignatureInfo() {
        String content = createSignatureType();
        // TODO - later content += " " + createKeyLocator()
        return "SIGNATURE-INFO-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     SignatureType ::==
     --------------
     SIGNATURE-TYPE-TYPE TLV-LENGTH
     nonNegativeInteger
     --------------
     **/
    String createSignatureType() {
        String content = "0"; // TODO - rework later
        return "SIGNATURE-TYPE-TYPE " + Integer.toString(content.length()) + " " + content;
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
    String createDATA() {
        String content = nameField.createName();
        content += " " + createMetaInfo();
        content += " " + createContent();
        content += " " + createSignature();

        return "DATA-TLV " + Integer.toString(content.length()) + " " + content;
    }

    @Override
    public String toString() {
        return createDATA();
    }

}
