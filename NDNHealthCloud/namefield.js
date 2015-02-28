/** 
 * File contains code for creating 
 * NDN-compliant name components for packets
 **/

exports.NameField = function () {

    return {
        /**
         Our Name Format:
         "/ndn/userID/sensorID/timestring/processID/ [datacontents] or [ip]"

         The last field is specific to DATA and INTEREST packets, respectively.
         **/

        NameField: function (userDataID, sensorID, timestring, processID, finalField) {
            // NOTE: method assumes userID and sensorID are device specific
                    // meaning, no specification is needed; just get from memory

            this.name = "/ndn/" + userDataID + "/" + sensorID + "/" + timestring
                                + "/" + processID + "/" + finalField;
        },

        /**
         NameComponent ::=
         --------------
         GenericNameComponent | ImplicitSha256DigestComponent
         --------------
         **/
        createNameComponent: function () {
            return this.createGenericNameComponent() + " " + this.createImplicitSha256DigestComponent();
        },

        /**
         GenericNameComponent ::=
         --------------
         NAME-COMPONENT-TYPE TLV-LENGTH Byte*
         --------------
         **/
        createGenericNameComponent: function () {
            var content = this.name;
            return "NAME-COMPONENT-TYPE " + (content.length).toString() + " " + content;
        },

        /**
         ImplicitSha256DigestComponent ::=
         --------------
         IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE TLV-LENGTH(=32) Byte{32}
         --------------
         **/
        createImplicitSha256DigestComponent: function () {
            var exampleSha = "893259d98aca58c451453f29ec7dc38688e690dd0b59ef4f3b9d33738bff0b8d";
            return "IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE " + exampleSha.length.toString()
                    + " " + exampleSha;
        },

        /**
         Name ::=
         --------------
         NAME-TYPE TLV-LENGTH NameComponent
         --------------
         **/
        createName: function () {
            var content = this.createNameComponent();

            return "NAME-TYPE " + (content.length).toString() + " " + content;
        },

        toString: function () {
            return this.createName(); // TODO - rework
        }
    }
};
