package com.ndnhealthnet.androidudpclient.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.Hashing.BCrypt;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;


/**
 * Class facilitates user credential storage, which is necessary for NDN
 * communication - as well as numerous other helpful, miscellaneous features.
 */
public class Utils {

    /**
     * Code from stackoverflow user umair.ali @ http://stackoverflow.com/users/1334114/umair-ali
     *
     * Called to save supplied value in shared preferences against given key.
     * @param context Context of caller activity
     * @param key Key of value to save against
     * @param value Value to save
     */
    public static boolean saveToPrefs(Context context, String key, String value) {

        if (context == null || key == null || value == null
                || (!key.equals(ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY) // key must equal either one
                && !key.equals(ConstVar.PREFS_LOGIN_USER_ID_KEY))) { // otherwise, it's invalid) {
            return false;
        }

        if (key.equals(ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY)) {
            key = BCrypt.hashpw(key, BCrypt.gensalt()); // hash if password
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,value);
        editor.apply();

        return true;
    }

    /**
     * Code from stackoverflow user umair.ali @ http://stackoverflow.com/users/1334114/umair-ali
     *
     * Called to retrieve required value from shared preferences, identified by given key.
     * Default value will be returned of no value found or error occurred.
     * @param context Context of caller activity
     * @param key Key to find value against
     * @param defaultValue Value to return if no data found against given key
     * @return Return the value found against given key, default if not found or any error occurs
     */
    public static String getFromPrefs(Context context, String key, String defaultValue) {

        if (context == null || key == null || defaultValue == null
                || (!key.equals(ConstVar.PREFS_LOGIN_PASSWORD_ID_KEY) // key must equal either one
                && !key.equals(ConstVar.PREFS_LOGIN_USER_ID_KEY))) { // otherwise, it's invalid
            return null;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return sharedPrefs.getString(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Method takes query results, filters based upon input parameters,
     * and then converts to a format that can be presented via graph
     *
     * @param myData array list of database data
     * @param sensor the name of selected sensor
     * @param startDate of requested interval
     * @param endDate of requested interval
     * @return data from input in graphable format
     */
    public static ArrayList<Float> convertDBRowTFloats(ArrayList<DBData> myData, String sensor,
                    String startDate, String endDate) {

        ArrayList<Float> myFloatData = new ArrayList<Float>();

        // syntax for interval: startDate,||endDate
        String requestInterval = startDate + "||" + endDate;

        for (int i = 0; i < myData.size(); i++) {

            // only get data if the sensor name matches && is valid for time interval
            if (myData.get(i).getSensorID().equals(sensor)
                    && isValidForTimeInterval(requestInterval, myData.get(i).getTimeString())) {

                String [] floatArray = myData.get(i).getDataFloat().trim().split(",");
                for (int j = 0; j < floatArray.length; j++) {

                    myFloatData.add(Float.parseFloat(floatArray[j].trim()));
                }
            }
        }

        return myFloatData;
    }

    /**
     * Converts the date format displayed to the user to one that better
     * captures the correct time and is used elsewhere in the program.
     *
     * Input Syntax: "DD/MM/YYYY - DD/MM/YYYY"
     * Output Syntax: "yyyy-MM-ddTHH.mm.ss.SSS||yyyy-MM-ddTHH.mm.ss.SSS"
     *
     * @param chosenInterval to be converted to alternative syntax
     * @return the syntactically-converted interval
     */
    public static String createAnalyticTimeInterval(String chosenInterval)  {

        try {
            String convertedAnalyticInterval = "";

            chosenInterval = chosenInterval.replace(" ", ""); // remove spaces

            String [] intervals = chosenInterval.split("-");

            String [] startInterval = intervals[0].split("/");
            String [] endInterval = intervals[1].split("/");

            // set hours,minutes,seconds,millis all to 0 as default
            convertedAnalyticInterval += startInterval[2] + "-" + startInterval[1] + "-" +
                    startInterval[0] + "T00.00.00.000||" + endInterval[2] + "-" + endInterval[1] +
                    "-" + endInterval[0] + "T00.00.00.000";

            return convertedAnalyticInterval;

        } catch (Exception e) {
            throw new IllegalArgumentException("!!Error occurred in Utils.createAnalyticTimeInterval: \" + e");
        }
    }

    /**
     * @return UTC-compliant current time
     */
    public static String getCurrentTime() {
        SimpleDateFormat formatUTC = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss.SSS");
        formatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

        // replace space with T; change makes parsing easier
        return formatUTC.format(new Date()).replace(" ", "T");
    }

    /**
     * Used to create start for synchronization time interval.
     *
     * @return timeString for previous hour
     */
    public static String getPreviousHourTime() {
        SimpleDateFormat formatUTC = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss.SSS");
        formatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date date = new Date();
        date.setTime(System.currentTimeMillis() - ConstVar.SYNCH_INTERVAL_MILLIS); // previous hour

        // replace space with T; change makes parsing easier
        return formatUTC.format(date).replace(" ", "T");
    }

    /**
     * Helper method that simplifies the code elsewhere.
     *
     * @param processID under question
     * @return true if processID is denotes an analytic task, false otherwise
     */
    public static boolean isAnalyticProcessID(String processID) {
        return processID.equals(ConstVar.MODE_ANALYTIC)
                || processID.equals(ConstVar.MEAN_ANALYTIC)
                || processID.equals(ConstVar.MEDIAN_ANALYTIC);

        // TODO - add more analytic process ids here once they are implemented
    }

    /**
     * tests validity of IP input
     *
     * @param ip input to be validated
     * @return validity status of input IP
     */
    public static boolean isValidIP(String ip) {

        if (ip == null) {
            return false;
        } else {
            boolean validIP;

            try {
                InetAddress.getByName(ip);
                validIP = true;
            } catch (Exception e) {
                validIP = false;
            }

            return validIP;
        }
    }

    /**
     * Method returns true if the data interval is within request interval
     *
     * @param requestInterval a request interval; necessarily must contain two times (start and end)
     * @param dataInterval the time stamp on specific data
     * @return determination of whether dataInterval is within requestInterval
     */
    static public boolean isValidForTimeInterval(String requestInterval, String dataInterval) {

        if (requestInterval == null || dataInterval == null) {
            return false; // reject bad input
        }

        String [] requestIntervals = requestInterval.split("\\|\\|"); // split interval into start/end

        // TIME_STRING FORMAT: "yyyy-MM-ddTHH.mm.ss.SSS||yyyy-MM-ddTHH.mm.ss.SSS"
        // the former is start interval, latter is end interval

        boolean beforeStartDate = false;
        boolean afterEndDate = false;

        Date startDate, endDate, dataDate;

        try {

            System.out.println("request interval: " + requestInterval);

            // replace "T" with empty char "", so that comparison is easier
            requestIntervals[0] = requestIntervals[0].replace("T", " ");
            requestIntervals[1] = requestIntervals[1].replace("T", " ");
            dataInterval = dataInterval.replace("T", " ");

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss.SSS");

            startDate = df.parse(requestIntervals[0]);
            endDate = df.parse(requestIntervals[1]);
            dataDate = df.parse(dataInterval);

            beforeStartDate = dataDate.before(startDate);
            afterEndDate = dataDate.after(endDate);

        } catch (Exception e) {
            e.printStackTrace();

            return false; // some problem occurred, default return is false
        }

        // if dataInterval is not before start and not after end, then its with interval
        return (!beforeStartDate && !afterEndDate) || requestIntervals[0].equals(dataInterval)
                || requestIntervals[1].equals(dataInterval);
    }

    /**
     * Each user-defined synch interval, this method is invoked and converts all data collected
     * during past interval into a string (syntax below) for easy placement in NDN Data packet.
     *
     * Syntax: Sensor1--data1,time1;; ... ;;dataN,timeN:: ... ::SensorN--data1,time1;; ... ;;dataN,timeN
     *
     * @param data to be converted
     * @return converted data
     */
    public static String formatSynchData(ArrayList<DBData> data) {

        try {
            Hashtable<String, ArrayList<DBData>> hashedBySensors = new Hashtable<>();
            String formattedSyncData = "";

            // first separate data based upon sensor
            for (int i = 0; i < data.size(); i++) {
                // sensor hasn't been stored yet, create ArrayList for its data and store now
                if (!hashedBySensors.containsKey(data.get(i).getSensorID())) {

                    ArrayList<DBData> dataForSensor = new ArrayList<>();
                    dataForSensor.add(data.get(i));

                    hashedBySensors.put(data.get(i).getSensorID(), dataForSensor);
                }
                // sensor has been seen, append data to its ArrayList now
                else {

                    hashedBySensors.get(data.get(i).getSensorID()).add(data.get(i));
                }
            }

            // now format data for each sensor
            for (String key : hashedBySensors.keySet()) {

                formattedSyncData += key + "--"; // '--' separates sensor's name from its data

                for (int i = 0; i < hashedBySensors.get(key).size(); i++) {
                    DBData sensorData = hashedBySensors.get(key).get(i);

                    formattedSyncData += sensorData.getDataFloat() + "," + sensorData.getTimeString();
                    formattedSyncData += ";;"; // ';;' separates each data piece for sensor
                }

                // remove last two chars, ';;', because they proceed no data
                formattedSyncData = formattedSyncData.substring(0, formattedSyncData.length() - 2);

                formattedSyncData += "::"; // '::' separates each sensor
            }

            // remove last two chars, '::', because they proceed no sensor
            formattedSyncData = formattedSyncData.substring(0, formattedSyncData.length() - 2);

            return formattedSyncData;
        } catch (Exception e) {
            throw new IllegalArgumentException("!!Error in utils.formatSynchData(): " + e);
        }
    }

    /**
     * Attempts to determine whether userID input is valid
     *
     * TODO - define proper syntax
     *
     * @param userID input to have validity assessed
     * @return boolean regarding validity of input
     */
    public static boolean isValidUserName(String userID) {

        // TODO - perform sophisticated input validation

        return userID.length() >= 3 && userID.length() <= 15 && !userID.contains(":") &&
                !userID.contains(";") && !userID.contains("-") && !userID.contains(",");
    }

    /**
     * Attempts to determine whether password is valid
     *
     * TODO - define proper syntax
     *
     * @param password input to have validity assessed
     * @return boolean regarding validity of input
     */
    public static boolean isValidPassword(String password) {

        // TODO - perform sophisticated input validation

        return password.length() >= 3 && password.length() <= 15;
    }

    /**
     * Uses Java Mail module to determine validity of email address.
     *
     * @param email specified by user
     * @return true if valid; otherwise, false
     */
    public static boolean isValidEmail(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    /**
     * Converts an NDN Name component to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/name.html
     *
     * @param name - an NDN name component
     * @return input param converted to string
     */
    public static String convertNameToString(Name name) {

        String hashComponent = "IMPLICIT-SHA256-DIGEST-COMPONENT-TYPE " + name.hashCode()
                + " TLV-LENGTH " + Integer.toString(name.hashCode()).length();

        String genericNameComponent = "NAME-COMPONENT-TYPE " + name.toUri() + " TLV-LENGTH "
                + name.toUri().length();

        return  "NAME-TYPE TLV-LENGTH " + (hashComponent.length() + genericNameComponent.length())
                + genericNameComponent + " " + hashComponent;
    }

    /**
     * Converts an Data packet's MetaInfo to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/data.html
     *
     * @param data used during conversion
     * @return string containing Data's MetaInfo
     */
    public static String convertMetaInfoToString(Data data) {

        String contentType = "CONTENT-TYPE-TYPE TLV-LENGTH " + data.getMetaInfo().getType();
        String freshnessPeriod = "FRESHNESS-PERIOD-TLV TLV-LENGTH " + data.getMetaInfo().getFreshnessPeriod();
        String finalBlockID = "FINAL-BLOCK-ID-TLV TLV-LENGTH " + data.getMetaInfo().getFinalBlockId().hashCode();

        int length = contentType.length() + freshnessPeriod.length() + finalBlockID.length();

        return "META-INFO-TYPE TLV-LENGTH " + length + " " + contentType + " " + freshnessPeriod
                + " " + finalBlockID;
    }

    /**
     * Converts an Data packet's Content to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/data.html
     *
     * @param data used during conversion
     * @return string containing Data's Content
     */
    public static String convertContentToString(Data data) {

        String contentType = "CONTENT-TYPE-TYPE TLV-LENGTH "; // TODO - get content type
        String content = data.getContent().toString();

        int length = contentType.length() + content.length();

        return "CONTENT-TYPE " + content + " TLV-LENGTH " + length + " " + contentType;
    }

    /**
     * Converts an Data packet's Signature to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/data.html
     * @param data used during conversion
     * @return string containing Data's Signature
     */
    public static String convertSignatureToString(Data data) {

        String signatureInfo = "SIGNATURE-INFO-TYPE TLV-LENGTH "; // TODO - get signature info
        String signatureBits = "SIGNATURE-VALUE-TYPE TLV-LENGTH "; // TODO - get signature value

       return signatureInfo + " " + signatureBits;
    }

    /**
     * Converts an Data packet to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/data.html
     *
     * @param data - a data packet
     * @return input param converted to string
     */
    public static String convertDataToString(Data data) {

        String name = convertNameToString(data.getName());
        String metaInfo = convertMetaInfoToString(data);
        String content = convertContentToString(data);
        String signature = convertSignatureToString(data);

        int length = name.length() + metaInfo.length() + content.length() + signature.length();

        return "DATA-TLV TLV-LENGTH " + length + " " + name + " " + metaInfo + " " + content
                + " " + signature;
    }

    /**
     * Converts an Interest packet's selectors to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/interest.html
     *
     * @param interest used during conversion
     * @return string containing Interest's selectors
     */
    public static String convertSelectorsToString(Interest interest) {

        String minSuffixComponents = "MIN-SUFFIX-COMPONENTS-TYPE TLV-LENGTH " + interest.getMinSuffixComponents();
        String maxSuffixComponents = "MAX-SUFFIX-COMPONENTS-TYPE TLV-LENGTH " + interest.getMaxSuffixComponents();
        String publisherPublicKeyLocator = interest.getKeyLocator().toString();
        String exclude = "EXCLUDE-TYPE TLV-LENGTH ANY-TYPE TLV-LENGTH(=0)";
        String childSelector = "CHILD-SELECTOR-TYPE TLV-LENGTH " + interest.getChildSelector();
        String mustBeFresh = "MUST-BE-FRESH-TYPE TLV-LENGTH(=0)";

        int length = minSuffixComponents.length() + maxSuffixComponents.length()
                + publisherPublicKeyLocator.length() + exclude.length() + childSelector.length()
                + mustBeFresh.length();

        return "SELECTORS-TYPE TLV-LENGTH " + length + " " + minSuffixComponents
                + " " + maxSuffixComponents + " " + publisherPublicKeyLocator + " " + exclude
                + " " + childSelector + " " + mustBeFresh;
    }

    /**
     * Converts an Interest packet to a string for further review.
     * This conversion attempts to stay close to the NDN documentation.
     *
     * See NDN documentation: http://named-data.net/doc/ndn-tlv/interest.html
     *
     * @param interest - an Interest packet
     * @return input param converted to string
     */
    public static String convertInterestToString(Interest interest) {

        String name = convertNameToString(interest.getName());
        String selectors = convertSelectorsToString(interest);
        String nonce = "NONCE-TYPE " + interest.getNonce().hashCode() + " TLV-LENGTH 4";
        String scope = "SCOPE-TYPE TLV-LENGTH " + interest.getScope();
        String interestLifetime = "INTEREST-LIFETIME-TYPE TLV-LENGTH " + interest.getInterestLifetimeMilliseconds();

        int length = name.length() + selectors.length() + nonce.length() + scope.length()
                + interestLifetime.length();

        return "INTEREST-TYPE TLV-LENGTH " + length + " " + name + " " + selectors + " "
                + nonce + " " + scope + " " + interestLifetime;
    }

    /**
     * Simplifies the insertion of Interest packet into the database.
     *
     * @param context used to access DBSingleton
     * @param interest packet to store in DB
     */
    public static void storeInterestPacket(Context context, Interest interest) {
        DBSingleton.getInstance(context).getDB()
                .addPacketData(new DBData(interest.getName().toUri(), convertInterestToString(interest)));
    }

    /**
     * Simplifies the insertion of a Data packet into the database.
     *
     * @param context used to access DBSingleton
     * @param data packet to store in DB
     */
    public static void storeDataPacket(Context context, Data data) {
        DBSingleton.getInstance(context).getDB()
                .addPacketData(new DBData(data.getName().toUri(), convertDataToString(data)));
    }
}