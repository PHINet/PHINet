package com.ndnhealthnet.androidudpclient;

import android.content.Context;

import com.ndnhealthnet.androidudpclient.Comm.UDPListener;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.DB.DatabaseHandler;
import com.ndnhealthnet.androidudpclient.Utility.StringConst;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import junit.framework.TestCase;

/**
 * Tests the functionality of UDPListener.java
 */

// NOTE: the udp_comm module has several branches (i.e.,
// we send the packet along as more specific things are detected)
// here, we will only test the final branches
public class UDPListenerTest extends TestCase {

    UDPListener receiverThread;
    DatabaseHandler datasource;
    Context context;

    /**
     *
     * @param context used to create UDPListener object used in testing
     */
    public UDPListenerTest(Context context) {
        receiverThread = new UDPListener(context);
        datasource = new DatabaseHandler(context);
        this.context = context;
    }

    /**
     * Method invokes all test cases.
     *
     * @throws Exception for failed tests
     */
    public void runTests() throws Exception {
        testHandleInterestFIBRequest();
        testHandleInterestCacheRequest();
        testHandleCacheData();
        testHandleFIBData();

        // delete test entries
        DBSingleton.getInstance(context).getDB().deleteEntireCS();
        DBSingleton.getInstance(context).getDB().deleteEntirePIT();
        DBSingleton.getInstance(context).getDB().deleteEntireFIB();

        // erase test user ID
        Utils.saveToPrefs(this.context, StringConst.PREFS_LOGIN_USER_ID_KEY, "");
    }

    /**
     *
     * @throws Exception for failed tests
     */
    public void testHandleInterestFIBRequest() throws Exception {
        // 1. test if empty fib

        // TODO - more robust tests

        // 2. test if non-empty fib

        // 3. test bad input
    }

    /**
     *
     * @throws Exception for failed tests
     */
    public void testHandleInterestCacheRequest() throws Exception {


        // TODO - more robust tests
        // 1. handle with empty cache

        // 2. handle with non empty cache

        // 3. handle with non-empty pit

        // 4. handle with empty pit

        // 5. test bad data
    }

    /**
     *
     * @throws Exception for failed tests
     */
    public void testHandleCacheData() throws Exception {

        // TODO - more robust tests

        // 1. test bad input

        // 2. handle with empty cache for this data

        // 3. handle with non-empty cache for this data (update)

        // 4. handle with pit requests

        // 4. handle without pit requests
    }

    /**
     *
     * @throws Exception for failed tests
     */
    public void testHandleFIBData() throws Exception {

        // TODO - more robust tests

        /*final String IP1 = "11.11.11.11";
        final String IP2 = "12.12.12.12";
        final String userID1 = "user1";
        final String deviceID = "deviceID";

        // it's necessary to have a userID entered for device so that a FIB entry isn't added for self
        Utils.saveToPrefs(this.context, StringConst.PREFS_LOGIN_USER_ID_KEY, deviceID);

        String goodFIBEntry = userID1 + "," + IP1;
        String updateToGoodFIBEntry = userID1 + "," + IP2;
        String deviceFIBEntry = deviceID + "," + IP1; // should be rejected on basis of ID
        String badFIBEntry1 = "a,a";
        String badFIBEntry2 = "apsidfasdf";

        datasource.deleteEntireFIB(); // clear FIB before testing functionality

        // test on bad input
        assertFalse(receiverThread.handleFIBData(null));
        assertFalse(receiverThread.handleFIBData(""));
        assertFalse(receiverThread.handleFIBData(badFIBEntry1));
        assertFalse(receiverThread.handleFIBData(badFIBEntry2));

        // handle handle with self-FIB entry; should be rejected because it's the device's own ID
        assertFalse(receiverThread.handleFIBData(deviceFIBEntry));

        // --- handle with FIB entry of other entity ---

        receiverThread.handleFIBData(goodFIBEntry);

        assertTrue(receiverThread.handleFIBData(goodFIBEntry));

        DBData fibEntry = datasource.getFIBData(userID1);

        assertEquals(fibEntry.getIpAddr(), IP1); // test entry was added to FIB

        assertTrue(receiverThread.handleFIBData(updateToGoodFIBEntry));

        DBData updatedFIBEntry = datasource.getFIBData(userID1);

        assertEquals(updatedFIBEntry.getIpAddr(), IP2); // test entry was updated in FIB

        // --- handle with FIB entry of other entity ---*/
    }
}