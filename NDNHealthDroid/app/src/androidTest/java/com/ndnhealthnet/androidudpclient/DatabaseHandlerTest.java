package com.ndnhealthnet.androidudpclient;

import android.content.Context;

import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.DB.DatabaseHandler;
import com.ndnhealthnet.androidudpclient.Utility.StringConst;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 *  Tests the functionality of DatabaseHandler.java
 */
public class DatabaseHandlerTest extends TestCase {

    DatabaseHandler datasource;

    // --- test PIT data ---

    DBData pitData1 = new DBData(StringConst.PIT_DB, "SENSOR1", StringConst.DATA_CACHE,
            StringConst.CURRENT_TIME, "USER1", "11.11.11.11");

    DBData pitData2 = new DBData(StringConst.PIT_DB, "SENSOR2", StringConst.DATA_CACHE,
            StringConst.CURRENT_TIME, "USER2", "12.12.12.12");

    DBData pitData3 = new DBData(StringConst.PIT_DB, "SENSOR1", StringConst.DATA_CACHE,
            StringConst.CURRENT_TIME, "USER1", "13.13.13.13");

    // --- test PIT data ---

    // --- test CS data ---

    DBData csData1 = new DBData(StringConst.CS_DB, "SENSOR1", StringConst.DATA_CACHE,
            StringConst.CURRENT_TIME, "USER1", "11,11,11,11");

    DBData csData2 = new DBData(StringConst.CS_DB, "SENSOR2", StringConst.DATA_CACHE,
            StringConst.CURRENT_TIME, "USER2", "12,12,12,12");

    // --- test CS data ---

    // --- test FIB data ---

    DBData fibData1 = new DBData("USER1", StringConst.CURRENT_TIME, "11.11.11.11");

    DBData fibData2 = new DBData("USER2", StringConst.CURRENT_TIME, "11.11.11.11");

    // --- test FIB data ---

    /**
     * @param context used to create DatabaseHandler object used in testing
     */
    public DatabaseHandlerTest(Context context) {
        datasource = new DatabaseHandler(context);
    }

    /**
     * Method invokes all test cases.
     *
     * @throws Exception for failed tests
     */
    public void runTests() throws Exception {
        testAddPITData();
        testAddCSData();
        testAddFIBData();
        testGetGeneralPITData();
        testGetSpecificPITData();
        testGetGeneralCSData();
        testGetAllFIBData();
        testGetSpecificCSData();
        testUpdateFIBData();
        testUpdatePITData();
        testUpdateCSData();
        testDeletePITEntry();
        testDeleteFIBEntry();
        testDeleteCSEntry();
        testDeleteEntirePIT();
        testDeleteEntireCS();
        testDeleteEntireFIB();

        // delete test entries
        datasource.deleteEntireCS();
        datasource.deleteEntirePIT();
        datasource.deleteEntireFIB();
    }

    /**
     * @throws Exception for failed tests
     */
    public void testAddPITData() throws Exception {

        datasource.deleteEntirePIT(); // delete PIT before testing insertion

        // test addition of valid data
        assertTrue(datasource.addPITData(pitData1));
        assertTrue(datasource.addPITData(pitData2));
        assertTrue(datasource.addPITData(pitData3));

        // test addition of null data
        assertFalse(datasource.addPITData(null));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testAddCSData() throws Exception {

        datasource.deleteEntireCS(); // delete CS before testing insertion

        // test addition of valid data
        assertTrue(datasource.addCSData(csData1));
        assertTrue(datasource.addCSData(csData2));

        // test addition of null and/or duplicate data
        assertFalse(datasource.addCSData(null));
        assertFalse(datasource.addCSData(csData1));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testAddFIBData() throws Exception {

        datasource.deleteEntireFIB(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(datasource.addFIBData(fibData1));
        assertTrue(datasource.addFIBData(fibData2));

        // test addition of null and/or duplicate data
        assertFalse(datasource.addFIBData(null));
        assertFalse(datasource.addFIBData(fibData1));
    }

    /**
     * method wipes pit, then adds two entries and checks that both are returned
     *
     * @throws Exception for failed tests
     */
    public void testGetGeneralPITData() throws Exception {
        datasource.deleteEntirePIT(); // delete PIT before testing insertion

        // check that null is returned for empty PIT
        assertEquals(datasource.getGeneralPITData("id1"), null);

        // test addition of valid data
        assertTrue(datasource.addPITData(pitData1));
        assertTrue(datasource.addPITData(pitData3));

        // check that both will be returned (due to same userID)
        assertEquals(pitData1.getUserID(), pitData3.getUserID());

        // check that both have different IP, so that we can verify 2 distinct entries
        assertTrue(!pitData1.getIpAddr().equals(pitData3.getIpAddr()));

        ArrayList<DBData> generalPITData = datasource.getGeneralPITData(pitData1.getUserID());

        assertEquals(generalPITData.size(), 2); // check that 2 entries have been added

        int foundCount = 0;
        for (int i = 0; i < generalPITData.size(); i++) {
            if (generalPITData.get(i).getIpAddr().equals(pitData1.getIpAddr())) {
                foundCount++;
            } else if (generalPITData.get(i).getIpAddr().equals(pitData3.getIpAddr())) {
                foundCount++;
            }
        }

        assertEquals(foundCount, 2); // assert that both were found

        // test null request
        ArrayList<DBData> nullPITData = datasource.getGeneralPITData(null);

        assertEquals(nullPITData, null); // verify that no entries were returned
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetSpecificPITData() throws Exception {
        datasource.deleteEntirePIT(); // delete PIT before testing insertion

        // check null is returned for empty PIT
        assertEquals(datasource.getSpecificPITData("id1", "ip1"), null);

        // test addition of valid data
        assertTrue(datasource.addPITData(pitData1));
        assertTrue(datasource.addPITData(pitData3));

        DBData pitSpecific1 = datasource.getSpecificPITData(pitData1.getUserID(), pitData1.getIpAddr());
        DBData pitSpecific3 = datasource.getSpecificPITData(pitData3.getUserID(), pitData3.getIpAddr());

        // test that pitData1 was returned correctly
        assertEquals(pitSpecific1.getIpAddr(), pitData1.getIpAddr());
        assertEquals(pitSpecific1.getUserID(), pitData1.getUserID());

        // test that pitData3 was returned correctly
        assertEquals(pitSpecific3.getIpAddr(), pitData3.getIpAddr());
        assertEquals(pitSpecific3.getUserID(), pitData3.getUserID());

        // test bad input gets a null return
        assertEquals(datasource.getSpecificCSData(null, null), null);
        assertEquals(datasource.getSpecificCSData("validUSERID", null), null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetFIBData() throws Exception {

        datasource.deleteEntireFIB(); // delete FIB before testing insertion

        // check null returned for empty FIB
        assertEquals(datasource.getAllFIBData(), null);

        // test addition of valid data
        assertTrue(datasource.addFIBData(fibData1));
        assertTrue(datasource.addFIBData(fibData2));

        DBData fibReturn1 = datasource.getFIBData(fibData1.getUserID());
        DBData fibReturn2 = datasource.getFIBData(fibData2.getUserID());

        // test valid return
        assertEquals(fibReturn1.getUserID(), fibData1.getUserID());
        assertEquals(fibReturn2.getUserID(), fibData2.getUserID());

        // test bad input
        assertEquals(datasource.getFIBData(null), null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetGeneralCSData() throws Exception {

        datasource.deleteEntireCS(); // delete CS before testing insertion

        // check null returned for empty CS
        assertEquals(datasource.getGeneralCSData("userid"), null);

        // test addition of valid data
        assertTrue(datasource.addCSData(csData1));
        assertTrue(datasource.addCSData(csData2));

        // TODO - rework and test when multiple entries can be returned

        ArrayList<DBData> csReturn1 = datasource.getGeneralCSData(csData1.getUserID());
        ArrayList<DBData> csReturn2 = datasource.getGeneralCSData(csData2.getUserID());

        // test valid data
        assertEquals(csReturn1.get(0).getDataFloat(), csData1.getDataFloat());
        assertEquals(csReturn2.get(0).getDataFloat(), csData2.getDataFloat());

        // test bad data
        assertEquals(datasource.getGeneralCSData(null), null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetAllFIBData() throws Exception {

        datasource.deleteEntireFIB(); // delete FIB before testing insertion

        assertEquals(datasource.getAllFIBData(), null); // check that empty FIB returns null

        // test addition of valid data
        assertTrue(datasource.addFIBData(fibData1));
        assertTrue(datasource.addFIBData(fibData2));

        ArrayList<DBData> allFIBData = datasource.getAllFIBData();

        assertEquals(allFIBData.size(), 2);

        int foundCount = 0;
        for (int i = 0; i < allFIBData.size(); i++) {
            if (allFIBData.get(i).getUserID().equals(fibData1.getUserID())) {
                foundCount++;
            } else if (allFIBData.get(i).getUserID().equals(fibData2.getUserID())) {
                foundCount++;
            }
        }

        assertEquals(foundCount, 2);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetSpecificCSData() throws Exception {
        datasource.deleteEntireCS(); // delete CS before testing insertion

        // check null returned for empty CS
        assertEquals(datasource.getSpecificCSData("userid", "timestring"), null);

        // test addition of valid data
        assertTrue(datasource.addCSData(csData1));
        assertTrue(datasource.addCSData(csData2));

        // TODO - rework name/timestring/etc so that only 1 can be returned
    }

    /**
     * @throws Exception for failed tests
     */
    public void testUpdateFIBData() throws Exception {

        datasource.deleteEntireFIB(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(datasource.addFIBData(fibData1));

        // check original return
        DBData originalReturn = datasource.getFIBData(fibData1.getUserID());
        assertEquals(originalReturn.getIpAddr(), fibData1.getIpAddr());

        // now, modify and check return
        fibData1.setIpAddr("99.99.99.99");

        // test validity of update
        assertTrue(datasource.updateFIBData(fibData1));

        DBData updatedReturn = datasource.getFIBData(fibData1.getUserID());
        assertEquals(updatedReturn.getIpAddr(), fibData1.getIpAddr());

        // test bad update return
        assertFalse(datasource.updateFIBData(null));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testUpdatePITData() throws Exception {
        datasource.deleteEntirePIT(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(datasource.addPITData(pitData1));

        // check original return
        DBData originalReturn = datasource.getSpecificPITData(pitData1.getUserID(), pitData1.getIpAddr());
        assertEquals(originalReturn.getIpAddr(), pitData1.getIpAddr());

        // now, modify and check return
        pitData1.setIpAddr("99.99.99.99");

        // test validity of update
        assertTrue(datasource.updatePITData(pitData1));

        DBData updatedReturn = datasource.getSpecificPITData(pitData1.getUserID(), pitData1.getIpAddr());
        assertEquals(updatedReturn.getIpAddr(), pitData1.getIpAddr());

        // test bad update return
        assertFalse(datasource.updatePITData(null));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testUpdateCSData() throws Exception {
        datasource.deleteEntireCS(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(datasource.addCSData(csData1));

        // check original return
        DBData originalReturn = datasource.getSpecificCSData(csData1.getUserID(), csData1.getTimeString());
        assertEquals(originalReturn.getDataFloat(), csData1.getDataFloat());

        // now, modify and check return
        csData1.setDataFloat("99,99,99,99");

        // test validity of update
        assertTrue(datasource.updateCSData(csData1));

        DBData updatedReturn = datasource.getSpecificCSData(csData1.getUserID(), csData1.getTimeString());
        assertEquals(updatedReturn.getDataFloat(), csData1.getDataFloat());

        // test bad update return
        assertFalse(datasource.updateCSData(null));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeletePITEntry() throws Exception {
        datasource.deleteEntirePIT(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(datasource.addPITData(pitData1));

        // check original return
        DBData originalReturn = datasource.getSpecificPITData(pitData1.getUserID(), pitData1.getIpAddr());
        assertEquals(originalReturn.getIpAddr(), pitData1.getIpAddr());

        // check deletePITEntry rejects bad data
        assertFalse(datasource.deletePITEntry(null, pitData1.getTimeString(), pitData1.getIpAddr()));

        // check deletePITEntry accepts valid deletion
        assertTrue(datasource.deletePITEntry(pitData1.getUserID(), pitData1.getTimeString(), pitData1.getIpAddr()));

        // check, now deleted, entry returns null
        assertEquals(datasource.getSpecificPITData(pitData1.getUserID(), pitData1.getIpAddr()), null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeleteFIBEntry() throws Exception {
        datasource.deleteEntireFIB(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(datasource.addFIBData(fibData1));

        // check original return
        DBData originalReturn = datasource.getFIBData(fibData1.getUserID());
        assertEquals(originalReturn.getIpAddr(), fibData1.getIpAddr());

        // check null data rejected
        assertFalse(datasource.deleteFIBEntry(null));

        // check valid data accepted
        assertTrue(datasource.deleteFIBEntry(fibData1.getUserID()));

        // check, now deleted, entry returns null
        assertEquals(datasource.getFIBData(fibData1.getUserID()), null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeleteCSEntry() throws Exception {
        datasource.deleteEntireCS(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(datasource.addCSData(csData1));

        // check original return
        DBData originalReturn = datasource.getSpecificCSData(csData1.getUserID(), csData1.getTimeString());
        assertEquals(originalReturn.getDataFloat(), csData1.getDataFloat());

        // check null data rejected
        assertFalse(datasource.deleteCSEntry(null, null));

        // check valid data accepted
        assertTrue(datasource.deleteCSEntry(csData1.getUserID(), csData1.getTimeString()));

        // check, now deleted, entry returns null
        assertEquals(datasource.getSpecificCSData(csData1.getUserID(), csData1.getTimeString()), null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeleteEntirePIT() throws Exception {
        // TODO
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeleteEntireCS() throws Exception {
        // TODO
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeleteEntireFIB() throws Exception {
        // TODO
    }
}