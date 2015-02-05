package com.example.androidudpclient;


/**
 * Class creates an NDN-Compliant packet in the form of a string.
 */
public class Packet {

    String type;
    // TODO - add more relevant params
    Packet(String type) {
        this.type = type;
    }

    /**
    Our Name Format:
            "/ndn/userID/sensorID/timestring/processID"
    **/

    // TODO - later convert from string length to byte length

    final String CONSTANT_NAME = "/ndn/7/9/8/6"; // TODO - rework
    final int NON_NEG_INT_CONST = 0; // TODO - rework

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
        // TODO - later content += " " + createFreshnessPeriod()
        // TODO - later content += " " + createFinalBlockId()

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
        String content = "";//str(NON_NEG_INT_CONST);

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
        String content = "";//getNameComponent();

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
    Nonce ::=
    --------------
    NONCE-TYPE TLV-LENGTH(=4) BYTE{4}
    --------------
    **/
    String createNonce() {
        String content = "234190Absdfa"; // TODO - rework;
        return "NONCE-TYPE " + Integer.toString(content.length()) + " " + content;    
    }
    
    /**
    Name ::=
    --------------
    NAME-TYPE TLV-LENGTH NameComponent
    --------------
    **/
    String createName() {
        String content = createNameComponent();

        return "NAME-TYPE " + Integer.toString(content.length()) + " " + content; 
    }

    
    /**
    NameComponent ::=
    --------------
    GenericNameComponent | ImplicitSha256DigestComponent
    --------------
    **/
    String createNameComponent() {
        return createGenericNameComponent() + " " + createImplicitSha256DigestComponent();
    }

    /**
    GenericNameComponent ::=
    --------------
    NAME-COMPONENT-TYPE TLV-LENGTH Byte*
    --------------
    **/
    String createGenericNameComponent() {
        String content = CONSTANT_NAME;
        return "NAME-COMPONENT-TYPE " + Integer.toString(content.length()) + " " + content;
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
        String content = createName();
        content += " " + createSelectors();
        content += " " +  createNonce();
        content += " " + createScope();
        content += " " + createInterestLifetime();

        return "INTEREST-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
    Selectors ::=
    --------------

    --------------
    **/
    String createSelectors() {
        return "";
    }

    /**
    InterestLifeTime ::=
    --------------

    --------------
    **/
    String createInterestLifetime() {
        return "";
    }

    /**
    Scope ::=
    --------------

    --------------
    */
    String createScope() {
        return "";
    }

    /**
    ImplicitSha256DigestComponent ::=
    --------------
    IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE TLV-LENGTH(=32) Byte{32}
    --------------
    **/
    String createImplicitSha256DigestComponent() {
        String exampleSha = "893259d98aca58c451453f29ec7dc38688e690dd0b59ef4f3b9d33738bff0b8d";
        return "";
      //  return "IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE " + str(sys.getsizeof(exampleSha)) + " " + exampleSha;
    }

    /**
    Signature ::==
    --------------
    SignatureInfo SignatureBits
    --------------
    **/

    String createSignature() {
        return createSignatureInfo(); // TODO - later + " " + createSignatureBits();
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
        String content = createName();
        content += " " + createMetaInfo();
        content += " " + createContent();
        content += " " + createSignature();

        return "DATA-TLV " + Integer.toString(content.length()) + " " + content;
    }

    @Override
    public String toString() {
        if (type.equals("DATA")) {
            return createDATA();
        } else {
            return createINTEREST();
        }
    }
}
