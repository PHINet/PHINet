package com.ndnhealthnet.androidudpclient;

import android.content.Context;

import com.ndnhealthnet.androidudpclient.Comm.UDPListener;

import junit.framework.TestCase;

/**
 * Tests the functionality of UDPListener.java
 */
public class UDPListenerTest extends TestCase {

    UDPListener receiverThread;

    /**
     *
     * @param context used to create UDPListener object used in testing
     */
    public UDPListenerTest(Context context) {
        receiverThread = new UDPListener(context);
    }

    /**
     * Method invokes all test cases.
     *
     * @throws Exception for failed tests
     */
    public void runTests() throws Exception {
        testHandleIncomingNDNPacket();
        testHandleInterestPacket();
        testHandleInterestFIBRequest();
        testHandleInterestCacheRequest();
        testIsValidForTimeInterval();
        testHandleDataPacket();
        testHandleCacheData();
        testHandleFIBData();
    }

    /**
     *
     * @throws Exception for failed tests
     */
    public void testHandleIncomingNDNPacket() throws Exception {

    }

    /**
     *
     * @throws Exception for failed tests
     */
    public void testHandleInterestPacket() throws Exception {
        // 1. test all, bad input

        // 2. test fib interest

        // 3. test cache data


    }

    /**
     *
     * @throws Exception for failed tests
     */
    public void testHandleInterestFIBRequest() throws Exception {
        // 1. test if empty fib

        // 2. test if non-empty fib

        // 3. test bad input
    }

    /**
     *
     * @throws Exception for failed tests
     */
    public void testHandleInterestCacheRequest() throws Exception {

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
    public void testIsValidForTimeInterval() throws Exception {

        // 1. test bad input

        // 2. test input before interval

        // 3. test input after interval

        // 4. test input during interval
    }

    /**
     *
     * @throws Exception for failed tests
     */
    public void testHandleDataPacket() throws Exception {

        // 1. test bad input

        // 2. test with empty PIT

        // 3. test with request for data in pit
    }

    /**
     *
     * @throws Exception for failed tests
     */
    public void testHandleCacheData() throws Exception {

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
        // 1. handle bad input

        // 2. handle handle with self-fib entry

        // 2. handle with fib entry of other entity (and update/non-update)
    }
}