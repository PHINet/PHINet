package com.ndnhealthnet.androidudpclient;

import android.content.Context;

import junit.framework.TestCase;

/**
 *
 */
public class UtilsTest extends TestCase {

    Context context;

    /**
     *
     * @param context
     */
    UtilsTest(Context context) {
        this.context = context;
    }

    /**
     *
     * @throws Exception
     */
    public void runTests() throws Exception {
        testSaveToPrefs();
        testGetFromPrefs();
        testConvertDBRowTFloats();
        testGetCurrentTime();
        testValidIP();
    }

    /**
     *
     * @throws Exception
     */
    public void testSaveToPrefs() throws Exception {
        // test bad input
        assertFalse(Utils.saveToPrefs(null, null, null));

        // test bad key
        assertFalse(Utils.saveToPrefs(context,"bad key", "sensorid_test"));

        // test good input
        assertTrue(Utils.saveToPrefs(context, StringConst.PREFS_LOGIN_SENSOR_ID_KEY, "sensorid_test"));
    }

    /**
     *
     * @throws Exception
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
     *
     * @throws Exception
     */
    public void testConvertDBRowTFloats() throws Exception {
        // TODO
    }

    /**
     *
     * @throws Exception
     */
    public void testGetCurrentTime() throws Exception {
        // TODO
    }

    /**
     *
     * @throws Exception
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