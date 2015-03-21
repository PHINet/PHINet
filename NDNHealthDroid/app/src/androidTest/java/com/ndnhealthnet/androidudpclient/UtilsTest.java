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
        assertFalse(Utils.saveToPrefs(context,"bad key", "sensorid_test"));

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
}