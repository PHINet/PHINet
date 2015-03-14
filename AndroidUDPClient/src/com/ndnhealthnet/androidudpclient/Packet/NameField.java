package com.ndnhealthnet.androidudpclient.Packet;

/**
 * Class creates an NDN-compliant name for use within packets.
 */
public class NameField {

    /**
     Our Name Format:
     "/ndn/userID/sensorID/timestring/processID/ [datacontents] or [ip]"

     The last field is specific to DATA and INTEREST packets, respectively.
     **/

    private String name;

    public NameField(String userDataID, String sensorID, String timestring, String processID, String finalField) {
        // NOTE: method assumes userID and sensorID are device specific
                // meaning, no specification is needed; just get from memory




        this.name = "/ndn/" + userDataID + "/" + sensorID + "/" + timestring
                            + "/" + processID + "/" + finalField;
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
        String content = name;
        return "NAME-COMPONENT-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     ImplicitSha256DigestComponent ::=
     --------------
     IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE TLV-LENGTH(=32) Byte{32}
     --------------
     **/
    String createImplicitSha256DigestComponent() {

        // TODO - create real component

        String exampleSha = "893259d98aca58c451453f29ec7dc38688e690dd0b59ef4f3b9d33738bff0b8d";
        return "IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE " + Integer.toString(exampleSha.length())
                + " " + exampleSha;
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

    @Override
    public String toString() {
        return createName();
    }
}
