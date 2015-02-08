package com.example.androidudpclient.Packet;

import java.util.Random;

/**
 * Class creates an NDN-Compliant packet in the form of a string.
 *
 * Each component of a packet has its own creation method.
 */
public class InterestPacket {

    private NameField nameField;
    final int NON_NEG_INT_CONST = 0; // TODO - rework

    // TODO - add more relevant params
    public InterestPacket(String name) {
        nameField = new NameField(""); // TODO - pass real name
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
        String content = "0"; // TODO - rework later
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
        String content = "0"; // TODO - rework later
        return "MAX-SUFFIX-COMPONENTS-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     PublisherPublicKeyLocator ::=
     --------------
     KeyLocator
     --------------
     */
    String createPublisherPublicKeyLocator() {
        return ""; // TODO - rework later
    }

    /**
     Any ::=
     --------------
     ANY-TYPE TLV-LENGTH(=0)
     --------------
     */
    String createAny() {
        return "ANY-TYPE 0"; // TODO - rework
    }

    /**
     Exclude ::=
     --------------
     EXCLUDE-TYPE TLV-LENGTH Any? (NameComponent (Any)?)+
     --------------
     */
    String createExclude() {
        return "EXCLUDE-TYPE 0"; // TODO - rework later
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
        return "MUST-BE-FRESH-TYPE 0"; // TODO - rework
    }

    /**
    InterestLifeTime ::=
    --------------
    INTEREST-LIFETIME-TYPE TLV-LENGTH nonNegativeInteger
    --------------
    **/
    String createInterestLifetime() {
        String content = "0"; // TODO - rework
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
