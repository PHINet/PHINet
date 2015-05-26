
var StringConst = require('./string_const').StringConst;

/**
 * Returns object that holds/manipulates User Data.
 */
exports.User = function () {

    return {

        // --- member variables that may be manipulated ---
        userID: null,
        password: null,
        email: null,
        entityType: null,
        // --- member variables that may be manipulated ---

        /**
         * constructor for user
         *
         * @param userID associated with a given user
         * @param password associated with a given user
         * @param email associated with a given user
         * @param entityType associated with a given user
         */
        user: function (userID, password, email, entityType) {

            if (entityType !== StringConst.DOCTOR_ENTITY && entityType !== StringConst.PATIENT_ENTITY) {
                throw "!! Error in User constructor: entity is of invalid type \'" + entityType + "\' .";
            }

            this.userID = userID;
            this.password = password;
            this.email = email;
            this.entityType = entityType;
        },

        getUserID : function() {
            return this.userID;
        },

        setUserID : function(userID) {
            this.userID = userID;
        },

        getPassword : function() {
            return this.password;
        },

        setPassword : function(password) {
            this.password = password;
        },

        getEmail : function () {
            return this.email;
        },

        setEmail: function(email) {
            this.email = email;
        },

        getEntityType : function() {
            return this.entityType;
        },

        setEntityType : function(entityType) {
            this.entityType = entityType;
        }
    }
};