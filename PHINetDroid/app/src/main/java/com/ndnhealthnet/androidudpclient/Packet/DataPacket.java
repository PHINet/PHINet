package com.ndnhealthnet.androidudpclient.Packet;

import com.ndnhealthnet.androidudpclient.Utility.StringConst;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

/**
 * Class creates an NDN-Compliant Data packet in the form of a string.
 *
 * Each component of a packet has its own creation method.
 *
 * Specification found here: http://named-data.net/doc/ndn-tlv/data.html
 */
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

    /**
     * Constructor allows specified freshnessPeriod, signatureType,
     * and contentType - all params that user may tune for their benefit.
     *
     * @param userDataID specifies data producer
     * @param sensorID specifies health-sensor type (e.g., heart sensor)
     * @param timeString specifies when packet was created
     * @param processID specifies what process should be invoked upon reception (e.g., store in cache)
     * @param content packet content payload
     * @param contentType type of content (see NDN specification)
     * @param freshnessPeriod time until data "expires" (see NDN specification)
     * @param signatureType type of signature used on packet (see NDN specification)
     */
    public DataPacket(String userDataID, String sensorID,
                      String timeString, String processID, String content, int contentType,
                      int freshnessPeriod, int signatureType) {

        // if current time requested, provide it
        if (timeString.equals(StringConst.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

        this.nameField = new NameField(userDataID, sensorID, timeString, processID);
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
     * Constructor assigns defaults to params
     *
     * @param userDataID specifies data producer
     * @param sensorID specifies health-sensor type (e.g., heart sensor)
     * @param timeString specifies when packet was created
     * @param processID specifies what process should be invoked upon reception (e.g., store in cache)
     * @param content packet content payload
     */
    public DataPacket(String userDataID, String sensorID,
                      String timeString, String processID, String content) {

        // if current time requested, provide it
        if (timeString.equals(StringConst.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

        this.nameField = new NameField(userDataID, sensorID, timeString, processID);
        this.content = content;

        this.contentType = CONTENT_TYPE_DEFAULT;
        this.signatureType = SIGNATURE_DIGEST_SHA;
        this.freshnessPeriod = 0;
    }

    /**
     * MetaInfo ::=
     * --------------
     * META-INFO-TYPE TLV-LENGTH
     * ContentType?
     * FreshnessPeriod?
     * FinalBlockId?
     * --------------
     *
     * @return MetaInfo as definition above shows (see NDN specification)
     */
    String createMetaInfo() {
        String content = createContentType();
        content += " " + createFreshnessPeriod();
        content += " " + createFinalBlockId();

        return "META-INFO-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * ContentType ::=
     * --------------
     * CONTENT-TYPE-TYPE TLV-LENGTH
     * nonNegativeInteger
     * --------------
     *
     * @return ContentType as definition above shows (see NDN specification)
     **/
    String createContentType() {
        String content = Integer.toString(contentType);
        return "CONTENT-TYPE-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * FreshnessPeriod ::=
     * --------------
     * FRESHNESS-PERIOD-TLV TLV-LENGTH
     * nonNegativeInteger
     * --------------
     *
     * @return FreshnessPeriod as definition above shows (see NDN specification)
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
     * FinalBlockId ::=
     * --------------
     * FINAL-BLOCK-ID-TLV TLV-LENGTH
     * NameComponent
     * --------------
     *
     * @return FinalBlockId as definition above shows (see NDN specification)
     **/
    String createFinalBlockId() {
        String content = nameField.createNameComponent();

        return "FINAL-BLOCK-ID-TLV " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * Content ::=
     * --------------
     * CONTENT-TYPE TLV-LENGTH Byte*
     * --------------
     *
     * @return Content as definition above shows (see NDN specification)
     **/
    String createContent() {
        String content = this.content;
        return "CONTENT-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * Signature ::==
     * --------------
     * SignatureInfo SignatureBits
     * --------------
     *
     * @return Signature as definition above shows (see NDN specification)
     **/
    String createSignature() {
        String signatureBits = "null"; // TODO - rework
        return createSignatureInfo() + " " + signatureBits;
    }

    /**
     * SignatureInfo ::==
     * --------------
     * SIGNATURE-INFO-TYPE TLV-LENGTH
     * SignatureType
     * KeyLocator?
     * ... (SignatureType-specific TLVs)
     * --------------
     *
     * @return SignatureInfo as definition above shows (see NDN specification)
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
     *
     * @return KeyLocator as definition above shows (see NDN specification)
     */
    String createKeyLocator() {
        String content = "null"; // TODO - rework
        return "KEY-LOCATOR-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * SignatureType ::==
     * --------------
     * SIGNATURE-TYPE-TYPE TLV-LENGTH
     * nonNegativeInteger
     * --------------
     *
     * @return SignatureType as definition above shows (see NDN specification)
     **/
    String createSignatureType() {
        String content = Integer.toString(this.signatureType);
        return "SIGNATURE-TYPE-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * DATA ::=
     * --------------
     * DATA-TLV TLV-LENGTH
     * Name
     * MetaInfo?
     * Content
     * Signature
     * --------------
     *
     * @return Data packet as definition above shows (see NDN specification)
     **/
    String createDATA() {
        String content = nameField.createName();
        content += " " + createMetaInfo();
        content += " " + createContent();
        content += " " + createSignature();

        return "DATA-TLV " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * TODO - document
     *
     * @return
     */
    public String getName() {
        return nameField.createName();
    }

    @Override
    public String toString() {
        return createDATA();
    }
}
