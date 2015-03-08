package com.ndnhealthnet.androidudpclient.Packet;

public class DataPacket {

    // TODO - only send certain portions of DataPacket (be able to avoid optional)
        // TODO - do this through constructor, etc

    // 3 content types are currently defined
    public static final int CONTENT_TYPE_DEFAULT = 0;
    public static final int CONTENT_TYPE_LINK = 1;
    public static final int CONTENT_TYPE_KEY = 2;

    // 3 types of signatures are currently defined
    public static final int SIGNATURE_DIGEST_SHA = 0;
    public static final int SIGNATURE_SHA_WITH_RSA = 1;
    public static final int SIGNATURE_SHA_WITH_ECDSA = 3;

    public static final int DEFAULT_FRESH = 100000; // arbitrarily chosen

    private NameField nameField;
    private String content;
    private int contentType;
    private int freshnessPeriod;
    private int signatureType;

    public DataPacket(String userDataID, String sensorID,
                      String timestring, String processID, String content, int contentType,
                      int freshnessPeriod, int signatureType) {

        this.nameField = new NameField(userDataID, sensorID, timestring, processID, content);
        this.content = content;

        // perform input validation on content type
        if (contentType == CONTENT_TYPE_KEY) {
            this.contentType = CONTENT_TYPE_KEY;
        } else if (contentType == CONTENT_TYPE_LINK) {
            this.contentType = CONTENT_TYPE_LINK;
        } else {
            this.contentType = CONTENT_TYPE_DEFAULT; // if no others match, assign default
        }

        // perform input validation on signature type
        if (signatureType == SIGNATURE_SHA_WITH_ECDSA) {
            this.signatureType = SIGNATURE_SHA_WITH_ECDSA;
        } else if (signatureType == SIGNATURE_SHA_WITH_RSA) {
            this.signatureType = SIGNATURE_SHA_WITH_RSA;
        } else {
            this.signatureType = SIGNATURE_DIGEST_SHA; // if no others match, assign digest sha
        }

        // perform input validation on freshness period
        if (freshnessPeriod >= 0) {
            this.freshnessPeriod = freshnessPeriod;
        } else {
            this.freshnessPeriod = 0; // negatives aren't possible; assign 0
        }
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
        String content = createContentType();
        content += " " + createFreshnessPeriod();
        content += " " + createFinalBlockId();

        return "META-INFO-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     ContentType ::=
     --------------
     CONTENT-TYPE-TYPE TLV-LENGTH
     nonNegativeInteger
     --------------
     **/
    String createContentType() {
        String content = Integer.toString(contentType);
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

        /** the integer here specifies
         *  "how long a node should wait after the arrival of this data before marking it stale"
         *
         *  NOTE: value is in milliseconds
         *
         *  NOTE: stale data is still valid data; stale just means producer may have updated data
         *  **/

        String content = Integer.toString(freshnessPeriod);

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
        String content = this.content;
        return "CONTENT-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     Signature ::==
     --------------
     SignatureInfo SignatureBits
     --------------
     **/
    String createSignature() {
        String signatureBits = "null"; // TODO - rework
        return createSignatureInfo() + " " + signatureBits;
    }

    /**
     SignatureInfo ::==
     --------------
     SIGNATURE-INFO-TYPE TLV-LENGTH
     SignatureType
     KeyLocator?
     ... (SignatureType-specific TLVs)
     --------------
     **/
    String createSignatureInfo() {
        String content = createSignatureType();
        content += " " + createKeyLocator();
        return "SIGNATURE-INFO-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * KeyLocator ::=
     * --------------
     * KEY-LOCATOR-TYPE TLV-LENGTH (Name | KeyDigest)
     * --------------
     */
    String createKeyLocator() {
        String content = "null"; // TODO - rework
        return "KEY-LOCATOR-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     SignatureType ::==
     --------------
     SIGNATURE-TYPE-TYPE TLV-LENGTH
     nonNegativeInteger
     --------------
     **/
    String createSignatureType() {
        String content = Integer.toString(this.signatureType);
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
