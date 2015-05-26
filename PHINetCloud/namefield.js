/**
 * Enables creation of an NDN-compliant name for use within packets, but
 * the name itself is structure defined specifically for this application.
 */
exports.NameField = function () {

    return {

        // --- member variable that may be manipulated ---
        name: null,
        // --- member variable that may be manipulated ---

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
        NameField: function (userDataID, sensorID, timeString, processID) {
            // NOTE: method assumes userID and sensorID are device specific
                    // meaning, no specification is needed; just get from memory

            this.name = "/ndn/" + userDataID + "/" + sensorID + "/" + timeString + "/" + processID;
        },

        /**
         * NameComponent ::=
         * --------------
         * GenericNameComponent | ImplicitSha256DigestComponent
         * --------------
         *
         * @return String as definition above shows (see NDN specification)
         **/
        createNameComponent: function () {
            return this.createGenericNameComponent() + " " + this.createImplicitSha256DigestComponent();
        },

        /**
         * GenericNameComponent ::=
         * --------------
         * NAME-COMPONENT-TYPE TLV-LENGTH Byte*
         * --------------
         *
         * @return String as definition above shows (see NDN specification)
         **/
        createGenericNameComponent: function () {
            var content = this.name;
            return "NAME-COMPONENT-TYPE " + (content.length).toString() + " " + content;
        },

        /**
         * ImplicitSha256DigestComponent ::=
         * --------------
         * IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE TLV-LENGTH(=32) Byte{32}
         * --------------
         *
         * @return String as definition above shows (see NDN specification)
         **/
        createImplicitSha256DigestComponent: function () {

            // TODO - create real component

            var exampleSha = "893259d98aca58c451453f29ec7dc38688e690dd0b59ef4f3b9d33738bff0b8d";
            return "IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE " + exampleSha.length.toString()
                    + " " + exampleSha;
        },

        /**
         * Name ::=
         * --------------
         * NAME-TYPE TLV-LENGTH NameComponent
         * --------------
         *
         * @return String as definition above shows (see NDN specification)
         **/
        createName: function () {
            var content = this.createNameComponent();

            return "NAME-TYPE " + (content.length).toString() + " " + content;
        },

        /**
         * Method returns NDN-compliant Data string
         */
        toString: function () {
            return this.createName(); // TODO - rework
        }
    }
};
