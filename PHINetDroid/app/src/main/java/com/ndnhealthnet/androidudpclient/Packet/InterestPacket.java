package com.ndnhealthnet.androidudpclient.Packet;

import com.ndnhealthnet.androidudpclient.Utility.StringConst;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import java.util.Random;

/**
 * Class creates an NDN-Compliant Interest packet in the form of a string.
 *
 * Each component of a packet has its own creation method.
 *
 * Specification found here: http://named-data.net/doc/ndn-tlv/data.html
 */
public class InterestPacket {

    // TODO - only send certain portions of InterestPacket (be able to avoid optional)
        // TODO - do this through constructor, etc

    private NameField nameField;
    final private int LIFETIME_CONST = 100;

    /**
     * @param userDataID specifies data producer
     * @param sensorID specifies health-sensor type (e.g., heart sensor)
     * @param timeString specifies when packet was created
     * @param processID specifies what process should be invoked upon reception (e.g., store in cache)
     */
    public InterestPacket(String userDataID, String sensorID, String timeString, String processID) {

        // if current time requested, provide it
        if (timeString.equals(StringConst.CURRENT_TIME)) {
            timeString = Utils.getCurrentTime();
        }

        nameField = new NameField(userDataID, sensorID, timeString, processID);
    }

    /**
     * Nonce ::=
     * --------------
     * NONCE-TYPE TLV-LENGTH(=4) BYTE{4}
     * --------------
     *
     * "The Nonce carries a randomly-generated 4-octet long byte-string.
     * The combination of Name and Nonce should uniquely identify an Interest packet.
     * This is used to detect looping Interests."
     *
     * @return Nonce as definition above shows (see NDN specification)
    **/
    String createNonce() {

        Random r = new Random();

        String content = Integer.toString(r.nextInt(255)); // first octet
        content += Integer.toString(r.nextInt(255)); // second octet
        content += Integer.toString(r.nextInt(255)); // third octet
        content += Integer.toString(r.nextInt(255)); // fourth octet

        return "NONCE-TYPE " + Integer.toString(content.length()) + " " + content;    
    }
    
    /**
     * Selectors ::=
     * --------------
     * SELECTORS-TYPE TLV-LENGTH
     * MinSuffixComponents?
     * MaxSuffixComponents?
     * PublisherPublicKeyLocator?
     * Exclude?
     * ChildSelector?
     * MustBeFresh?
     * --------------
     *
     * @return Selectors as definition above shows (see NDN specification)
    **/
    String createSelectors() {
        String content = createMinSuffixComponents();
        content += " " + createMaxSuffixComponents();
        content += " " + createPublisherPublicKeyLocator();
        content += " " + createExclude();
        content += " " + createChildSelector();
        content += " " + createMustBeFresh();

        return "SELECTORS-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * MinSuffixComponents ::=
     * --------------
     * MIN-SUFFIX-COMPONENTS-TYPE TLV-LENGTH
     * nonNegativeInteger
     * --------------
     *
     * @return MinSuffixeComponents as definition above shows (see NDN specification)
     */
    String createMinSuffixComponents() {
        String content = "0"; // TODO - rework
        return "MIN-SUFFIX-COMPONENTS-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * MaxSuffixComponents ::=
     * --------------
     * MAX-SUFFIX-COMPONENTS-TYPE TLV-LENGTH
     * nonNegativeInteger
     * --------------
     *
     * @return MaxSuffixeComponents as definition above shows (see NDN specification)
     */
    String createMaxSuffixComponents() {
        String content = "0"; // TODO - rework
        return "MAX-SUFFIX-COMPONENTS-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * PublisherPublicKeyLocator ::=
     * --------------
     * KeyLocator
     * --------------
     *
     * @return PublisherPublicKeyLocator as definition above shows (see NDN specification)
     */
    String createPublisherPublicKeyLocator() {
        return "null"; // TODO - rework later
    }

    /**
     * Any ::=
     * --------------
     * ANY-TYPE TLV-LENGTH(=0)
     * --------------
     *
     * @return Any as definition above shows (see NDN specification)
     */
    String createAny() {
        return "ANY-TYPE 0";
    }

    /**
     * Exclude ::=
     * --------------
     * EXCLUDE-TYPE TLV-LENGTH Any? (NameComponent (Any)?)+
     * --------------
     *
     * @return Exclude as definition above shows (see NDN specification)
     */
    String createExclude() {
        String content = createAny(); // TODO - rework
        return "EXCLUDE-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * ChildSelector ::=
     * --------------
     * CHILD-SELECTOR-TYPE TLV-LENGTH
     * nonNegativeInteger
     * --------------
     *
     * @return ChildSelector as definition above shows (see NDN specification)
     */
    String createChildSelector() {
        String content = "0"; // TODO - rework
        return "CHILD-SELECTOR-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * MustBeFresh ::=
     * --------------
     * MUST-BE-FRESH-TYPE TLV-LENGTH(=0)
     * --------------
     *
     * @return MustBeFresh as definition above shows (see NDN specification)
     */
    String createMustBeFresh() {
        return "MUST-BE-FRESH-TYPE 0";
    }

    /**
     * InterestLifeTime ::=
     * --------------
     * INTEREST-LIFETIME-TYPE TLV-LENGTH nonNegativeInteger
     * --------------
     *
     * @return InterestLifeTime as definition above shows (see NDN specification)
    **/
    String createInterestLifetime() {
        String content = Integer.toString(LIFETIME_CONST); // TODO - add user-selected interval
        return "INTEREST-LIFETIME-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * Scope ::=
     * --------------
     * SCOPE-TYPE TLV-LENGTH nonNegativeInteger
     * --------------
     *
     * @return Scope as definition above shows (see NDN specification)
    */
    String createScope() {
        String content = "0"; // TODO - rework
        return "SCOPE-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * INTEREST ::=
     * --------------
     * INTEREST-TYPE TLV-LENGTH
     * Name
     * Selectors?
     * Nonce
     * Scope?
     * Interest Lifetime?
     * --------------
     *
     * @return Interest packet as definition above shows (see NDN specification)
     **/
    String createINTEREST() {
        String content = nameField.createName();
        content += " " + createSelectors();
        content += " " +  createNonce();
        content += " " + createScope();
        content += " " + createInterestLifetime();

        return "INTEREST-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    @Override
    public String toString() {
        return createINTEREST();
    }
}
