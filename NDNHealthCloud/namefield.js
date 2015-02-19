
/** 
 * File contains code for creating 
 * NDN-compliant name components for packets
 **/

    


exports.nameField = function() {

    /**
     Our Name Format:
     "/ndn/userID/sensorID/timestring/processID"
     **/
    function NameField(name) {
        this.name = "/ndn/7/9/8/6"; // TODO - rework
    }

    /**
     NameComponent ::=
     --------------
     GenericNameComponent | ImplicitSha256DigestComponent
     --------------
     **/
    function createNameComponent() {
        return createGenericNameComponent() + " " + createImplicitSha256DigestComponent();
    }

    /**
     GenericNameComponent ::=
     --------------
     NAME-COMPONENT-TYPE TLV-LENGTH Byte*
     --------------
     **/
    function createGenericNameComponent() {
        var content = name;
        return "NAME-COMPONENT-TYPE " + (content.length).toString() + " " + content;
    }

    /**
     ImplicitSha256DigestComponent ::=
     --------------
     IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE TLV-LENGTH(=32) Byte{32}
     --------------
     **/
    function createImplicitSha256DigestComponent() {
        var exampleSha = "893259d98aca58c451453f29ec7dc38688e690dd0b59ef4f3b9d33738bff0b8d";
        return "IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE " + Integer.toString(exampleSha.length())
                + " " + exampleSha;
    }

    /**
     Name ::=
     --------------
     NAME-TYPE TLV-LENGTH NameComponent
     --------------
     **/
    function createName() {
        var content = createNameComponent();

        return "NAME-TYPE " + (content.length).toString() + " " + content;
    }

    function toString() {
        return createName(); // TODO - rework
    }
}
