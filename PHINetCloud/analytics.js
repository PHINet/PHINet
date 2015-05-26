
var gauss = require('gauss'); // node module used for data analytics

/**
 * Returns data analytics module.
 */
exports.analytics = function() {

    // TODO - perform more complex analytics

    return {

        /**
         * Returns mean of input.
         *
         * @param data - array of values
         * @returns real valued mean
         */
        mean : function(data) {

            var vector = new gauss.vector(data);

            return vector.mean();
        },

        /**
         * Returns mode of input.
         *
         * @param data - array of values
         * @returns real valued mode
         */
        mode : function(data) {

            var vector = new gauss.vector(data);

            return vector.mode();
        },

        /**
         * Returns median of input.
         *
         * @param data - array of values
         * @returns real valued median
         */
        median: function(data) {
            var vector = new gauss.vector(data);

            return vector.median();
        }
    }
};