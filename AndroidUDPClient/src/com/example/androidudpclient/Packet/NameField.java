package com.example.androidudpclient.Packet;

public class NameField {

    /**
     Our Name Format:
     "/ndn/userID/sensorID/timestring/processID"
     **/

    private String name;

    public NameField(String name) {
        this.name = "/ndn/7/9/8/6"; // TODO - rework
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
        return createName(); // TODO - rework
    }
}
