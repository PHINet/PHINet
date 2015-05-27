package com.ndnhealthnet.androidudpclient;

import android.content.Context;

import com.ndnhealthnet.androidudpclient.Utility.StringConst;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import junit.framework.TestCase;

/**
 * Tests the functionality of Utils.java
 */
public class UtilsTest extends TestCase {

    Context context;

    /**
     *@param context used to create Utils object used in testing
     */
    UtilsTest(Context context) {
        this.context = context;
    }

    /**
     * Method invokes all test cases.
     *
     * @throws Exception for failed tests
     */
    public void runTests() throws Exception {
        testSaveToPrefs();
        testGetFromPrefs();
        testConvertDBRowTFloats();
        testGetCurrentTime();
        testValidIP();
        testEncryption();

        // reset user credentials after test
        assertTrue(Utils.saveToPrefs(context, StringConst.PREFS_LOGIN_SENSOR_ID_KEY, ""));
        assertTrue(Utils.saveToPrefs(context, StringConst.PREFS_LOGIN_USER_ID_KEY, ""));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testSaveToPrefs() throws Exception {
        // test bad input
        assertFalse(Utils.saveToPrefs(null, null, null));

        // test bad key input
        assertFalse(Utils.saveToPrefs(context, "bad key", "sensorid_test"));

        // test good input
        assertTrue(Utils.saveToPrefs(context, StringConst.PREFS_LOGIN_SENSOR_ID_KEY, "sensorid_test"));
    }

    /**
     * Method stores USER_ID then tests retrieval, along with other functionality.
     *
     * @throws Exception for failed tests
     */
    public void testGetFromPrefs() throws Exception {

        String userID = "USER_TEST_ID";

        // store input and later test retrieval
        assertTrue(Utils.saveToPrefs(context, StringConst.PREFS_LOGIN_USER_ID_KEY, userID));

        // test bad input
        assertEquals(Utils.getFromPrefs(null, null, null), null);

        // test bad key
        assertEquals(Utils.getFromPrefs(context, "bad key", ""), null);

        // test real params
        assertEquals(Utils.getFromPrefs(context, StringConst.PREFS_LOGIN_USER_ID_KEY, ""), userID);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testConvertDBRowTFloats() throws Exception {
        // TODO
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetCurrentTime() throws Exception {
        // TODO
    }

    /**
     * @throws Exception for failed tests
     */
    public void testValidIP() throws Exception {

        // test bad IP input
        assertFalse(Utils.validIP("invalid"));
        assertFalse(Utils.validIP(null));
        assertFalse(Utils.validIP("11.11.11.500000"));

        // test good IP input
        assertTrue(Utils.validIP("10.10.10.10"));
    }

    /**
     *
     * @throws Exception for failed tests
     */
    public void testIsValidForTimeInterval() throws Exception {

        final String goodRequestInterval = "2012-05-04T08:08:08.888||2014-05-04T08:08:08.888";
        final String badRequestInterval = "2012-ERROR:08.888||2014-ERROR8:08.888";

        final String goodDataInterval1 = "2012-07-04T08:08:08.888"; // date is within goodRequestInterval
        final String goodDataInterval2 = "2012-01-04T08:08:08.888"; // date is before goodRequestInterval
        final String goodDataInterval3 = "2014-07-04T08:08:08.888"; // date is after goodRequestInterval

        final String testInterval1 = "2015-02-22T00:00:00.000||2015-04-22T00:00:00.000";
        final String dataTime1 = "2015-03-22T22:58:10.878";

        // --- test bad input ---
        assertFalse(Utils.isValidForTimeInterval(null, null)); // null entries

        // syntax error in request interval
        assertFalse(Utils.isValidForTimeInterval(badRequestInterval, goodDataInterval1));

        // two data intervals and no request interval
        assertFalse(Utils.isValidForTimeInterval(goodDataInterval1, goodDataInterval1));

        // --- test bad input ---

        // test input rejection if before interval
        assertFalse(Utils.isValidForTimeInterval(goodRequestInterval, goodDataInterval2));

        // test input rejection if after interval
        assertFalse(Utils.isValidForTimeInterval(goodRequestInterval, goodDataInterval3));

        // test input acceptance if during interval
        assertTrue(Utils.isValidForTimeInterval(goodRequestInterval, goodDataInterval1));
        assertTrue(Utils.isValidForTimeInterval(testInterval1, dataTime1));
    }

    /**
     * TODO - document
     * @throws Exception
     */
    public void testEncryption() throws Exception {
      /*  String password1 = "myPassword";
        String password2 = "hunter2";

        String encryptedPW1 = Utils.encrypt(password1);
        String encryptedPW2 = Utils.encrypt(password2);

        // verify that passwords match against encrypted version
        assertTrue(Utils.compareAgainstEncrypted(password1, encryptedPW1));
        assertTrue(Utils.compareAgainstEncrypted(password2, encryptedPW2));

        // verify that method returns false when checked against incorrect version
        assertFalse(Utils.compareAgainstEncrypted(password1, encryptedPW2));
        assertFalse(Utils.compareAgainstEncrypted(password2, encryptedPW1));*/
    }
}