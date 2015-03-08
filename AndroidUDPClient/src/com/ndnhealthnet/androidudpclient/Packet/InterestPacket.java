package com.ndnhealthnet.androidudpclient.Packet;

import java.util.Random;

/**
 * Class creates an NDN-Compliant packet in the form of a string.
 *
 * Each component of a packet has its own creation method.
 */
public class InterestPacket {

    // TODO - only send certain portions of InterestPacket (be able to avoid optional)
        // TODO - do this through constructor, etc

    private NameField nameField;
    final private int LIFETIME_CONST = 100;

    public InterestPacket(String userDataID, String sensorID,
                          String timestring, String processID, String ipAddr) {
        nameField = new NameField(userDataID, sensorID, timestring, processID, ipAddr);
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
    String createNonce() {

        Random r = new Random();

        String content = Integer.toString(r.nextInt(255)); // first octet
        content += Integer.toString(r.nextInt(255)); // second octet
        content += Integer.toString(r.nextInt(255)); // third octet
        content += Integer.toString(r.nextInt(255)); // fourth octet

        return "NONCE-TYPE " + Integer.toString(content.length()) + " " + content;    
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
     MinSuffixComponents ::=
     --------------
     MIN-SUFFIX-COMPONENTS-TYPE TLV-LENGTH
     nonNegativeInteger
     --------------
     */
    String createMinSuffixComponents() {
        String content = "0"; // TODO - rework
        return "MIN-SUFFIX-COMPONENTS-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     MaxSuffixComponents ::=
     --------------
     MAX-SUFFIX-COMPONENTS-TYPE TLV-LENGTH
     nonNegativeInteger
     --------------
     */
    String createMaxSuffixComponents() {
        String content = "0"; // TODO - rework
        return "MAX-SUFFIX-COMPONENTS-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     PublisherPublicKeyLocator ::=
     --------------
     KeyLocator
     --------------
     */
    String createPublisherPublicKeyLocator() {
        return "null"; // TODO - rework later
    }

    /**
     Any ::=
     --------------
     ANY-TYPE TLV-LENGTH(=0)
     --------------
     */
    String createAny() {
        return "ANY-TYPE 0";
    }

    /**
     Exclude ::=
     --------------
     EXCLUDE-TYPE TLV-LENGTH Any? (NameComponent (Any)?)+
     --------------
     */
    String createExclude() {
        String content = createAny(); // TODO - rework
        return "EXCLUDE-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     ChildSelector ::=
     --------------
     CHILD-SELECTOR-TYPE TLV-LENGTH
     nonNegativeInteger
     --------------
     */
    String createChildSelector() {
        String content = "0"; // TODO - rework
        return "CHILD-SELECTOR-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     MustBeFresh ::=
     --------------
     MUST-BE-FRESH-TYPE TLV-LENGTH(=0)
     --------------
     */
    String createMustBeFresh() {
        return "MUST-BE-FRESH-TYPE 0";
    }

    /**
    InterestLifeTime ::=
    --------------
    INTEREST-LIFETIME-TYPE TLV-LENGTH nonNegativeInteger
    --------------
    **/
    String createInterestLifetime() {
        String content = Integer.toString(LIFETIME_CONST); // TODO - add user-selected interval
        return "INTEREST-LIFETIME-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
    Scope ::=
    --------------
    SCOPE-TYPE TLV-LENGTH nonNegativeInteger
    --------------
    */
    String createScope() {
        String content = "0"; // TODO - rework
        return "SCOPE-TYPE " + Integer.toString(content.length()) + " " + content;
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
