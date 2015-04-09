package com.ndnhealthnet.androidudpclient.Packet;

/**
 * Class creates an NDN-compliant name for use within packets, but
 * the name itself is structure defined specifically for this application.
 */
public class NameField {

    private String name;

    /**
     * Constructor our Our Name Format:
     * "/ndn/userID/sensorID/timeString/processID"
     * The last field is specific to DATA and INTEREST packets, respectively.
     *
     * @param userDataID specifies data producer
     * @param sensorID specifies health-sensor type (e.g., heart sensor)
     * @param timeString specifies when packet was created
     * @param processID specifies what process should be invoked upon reception (e.g., store in cache)
     */
    public NameField(String userDataID, String sensorID, String timeString, String processID) {
        // NOTE: method assumes userID and sensorID are device specific
                // meaning, no specification is needed; just get from memory

        this.name = "/ndn/" + userDataID + "/" + sensorID + "/" + timeString
                            + "/" + processID;
    }

    /**
     * NameComponent ::=
     * --------------
     * GenericNameComponent | ImplicitSha256DigestComponent
     * --------------
     *
     * @return NameComponent as definition above shows (see NDN specification)
     **/
    String createNameComponent() {
        return createGenericNameComponent() + " " + createImplicitSha256DigestComponent();
    }

    /**
     * GenericNameComponent ::=
     * --------------
     * NAME-COMPONENT-TYPE TLV-LENGTH Byte*
     * --------------
     *
     * @return GenericNameComponent as definition above shows (see NDN specification)
     **/
    String createGenericNameComponent() {
        String content = name;
        return "NAME-COMPONENT-TYPE " + Integer.toString(content.length()) + " " + content;
    }

    /**
     * ImplicitSha256DigestComponent ::=
     * --------------
     * IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE TLV-LENGTH(=32) Byte{32}
     * --------------
     *
     * @return ImplicitSha256DigestComponent as definition above shows (see NDN specification)
     **/
    String createImplicitSha256DigestComponent() {

        // TODO - create real component

        String exampleSha = "893259d98aca58c451453f29ec7dc38688e690dd0b59ef4f3b9d33738bff0b8d";
        return "IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE " + Integer.toString(exampleSha.length())
                + " " + exampleSha;
    }

    /**
     * Name ::=
     * --------------
     * NAME-TYPE TLV-LENGTH NameComponent
     * --------------
     *
     * @return Name as definition above shows (see NDN specification)
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
