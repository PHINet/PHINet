package com.ndnhealthnet.androidudpclient;

import android.content.Context;

import com.ndnhealthnet.androidudpclient.DB.DBData;
import com.ndnhealthnet.androidudpclient.DB.DBSingleton;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 *  Tests the functionality of DatabaseHandler.java
 */
public class DatabaseHandlerTest extends TestCase {

    Context context;
    
    // --- test PIT data ---

    // NOTE: changing this test-data may break test cases; both must be changed together
    DBData pitData1 = new DBData(ConstVar.PIT_DB, "SENSOR1", ConstVar.DATA_CACHE,
            ConstVar.CURRENT_TIME, "USER1", "11.11.11.11");

    DBData pitData2 = new DBData(ConstVar.PIT_DB, "SENSOR2", ConstVar.DATA_CACHE,
            ConstVar.CURRENT_TIME, "USER2", "12.12.12.12");

    DBData emptyPIT = new DBData();

    DBData pitData3 = new DBData(ConstVar.PIT_DB, "SENSOR3", ConstVar.DATA_CACHE,
            ConstVar.CURRENT_TIME, "USER1", "13.13.13.13");

    // --- test PIT data ---

    // --- test CS data ---

    // NOTE: changing this test-data may break test cases; both must be changed together
    DBData csData1 = new DBData(ConstVar.CS_DB, "SENSOR1", ConstVar.DATA_CACHE,
            ConstVar.CURRENT_TIME, "USER1", "11,11,11,11");

    DBData csData2 = new DBData(ConstVar.CS_DB, "SENSOR2", ConstVar.DATA_CACHE,
            ConstVar.CURRENT_TIME, "USER2", "12,12,12,12");

    DBData csData3 = new DBData(ConstVar.CS_DB, "SENSOR1", ConstVar.DATA_CACHE,
            "2015-04-27T00:00:00.000", "USER1", "12");

    // --- test CS data ---

    // --- test FIB data ---

    // NOTE: changing this test-data may break test cases; both must be changed together
    DBData fibData1 = new DBData("USER1", ConstVar.CURRENT_TIME, "11.11.11.11");

    DBData fibData2 = new DBData("USER2", ConstVar.CURRENT_TIME, "11.11.11.11");

    // --- test FIB data ---

    // -- test Sensor Data ---

    DBData sensorData1 = new DBData("SENSOR1", 890);

    DBData sensorData2 = new DBData("SENSOR2", 4);

    // -- test Sensor Data ---

    // -- test Packet Data ---

    DBData packetData1 = new DBData("packetName1", "ContentContentContentContent");

    DBData packetData2 = new DBData("packetName2", "packetcontentpacketcontent");

    // -- test Packet Data ---

    /**
     * @param context used to create DatabaseHandler object used in testing
     */
    public DatabaseHandlerTest(Context context) {
        this.context = context;
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
        testAddSensorData();
        testAddPacketData();
        testGetGeneralPITData();
        testGetSpecificPITData();
        testGetGeneralCSData();
        testGetAllFIBData();
        testGetAllSensorData();
        testGetAllPacketData();
        testGetSpecificCSData();
        testGetSpecificSensorData();
        testUpdateFIBData();
        testUpdatePITData();
        testUpdateCSData();
        testUpdateSensorEntry();
        testDeletePITEntry();
        testDeleteAllPackets();
        testDeleteSensorEntry();
        testDeleteFIBEntry();
        testDeleteCSEntry();
        testDeleteEntirePIT();
        testDeleteEntireCS();
        testDeleteEntireFIB();

        // delete test entries
        DBSingleton.getInstance(context).getDB().deleteEntireCS();
        DBSingleton.getInstance(context).getDB().deleteEntirePIT();
        DBSingleton.getInstance(context).getDB().deleteEntireFIB();
    }

    /**
     * @throws Exception for failed tests
     */
    public void testAddPITData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete PIT before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData2));
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData3));

        // test addition of null data
        assertFalse(DBSingleton.getInstance(context).getDB().addPITData(null));
        assertFalse(DBSingleton.getInstance(context).getDB().addPITData(pitData1)); // test addition of duplicate entry
    }

    /**
     * @throws Exception for failed tests
     */
    public void testAddCSData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete CS before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData2));

        // test addition of null and/or duplicate data
        assertFalse(DBSingleton.getInstance(context).getDB().addCSData(null));
        assertFalse(DBSingleton.getInstance(context).getDB().addCSData(csData1));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testAddFIBData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData2));

        // test addition of null and/or duplicate data
        assertFalse(DBSingleton.getInstance(context).getDB().addFIBData(null));
        assertFalse(DBSingleton.getInstance(context).getDB().addFIBData(fibData1));

        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB after testing
    }

    /**
     * @throws Exception
     */
    public void testAddSensorData() throws Exception {

        // for safety, delete prior to test
        DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData1.getSensorID());
        DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData2.getSensorID());

        // test adding good data
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData2));

        // test adding bad data (null and/or duplicate entry)
        assertFalse(DBSingleton.getInstance(context).getDB().addSensorData(null));
        assertFalse(DBSingleton.getInstance(context).getDB().addSensorData(sensorData1));

        // delete after testing
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData1.getSensorID()));
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData2.getSensorID()));
    }

    /**
     * @throws Exception
     */
    public void testAddPacketData() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntirePacketDB(); // clear DB before testing

        // test adding good data
        assertTrue(DBSingleton.getInstance(context).getDB().addPacketData(packetData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addPacketData(packetData2));

        // test adding bad data (null and/or duplicate entry)
        assertFalse(DBSingleton.getInstance(context).getDB().addPacketData(packetData1));
        assertFalse(DBSingleton.getInstance(context).getDB().addPacketData(null));

        DBSingleton.getInstance(context).getDB().deleteEntirePacketDB(); // delete after testing
    }

    /**
     * method wipes pit, then adds two entries and checks that both are returned
     *
     * @throws Exception for failed tests
     */
    public void testGetGeneralPITData() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete PIT before testing insertion

        // check that null is returned for empty PIT
        assertEquals(DBSingleton.getInstance(context).getDB().getGeneralPITData("id1"), null);

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData3));

        // check that both will be returned (due to same userID)
        assertEquals(pitData1.getUserID(), pitData3.getUserID());

        // check that both have different IP, so that we can verify 2 distinct entries
        assertTrue(!pitData1.getIpAddr().equals(pitData3.getIpAddr()));

        ArrayList<DBData> generalPITData = DBSingleton.getInstance(context).getDB().getGeneralPITData(pitData1.getUserID());

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
        ArrayList<DBData> nullPITData = DBSingleton.getInstance(context).getDB().getGeneralPITData(null);

        assertEquals(nullPITData, null); // verify that no entries were returned
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetSpecificPITData() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete PIT before testing insertion

        // check null is returned for empty PIT
        assertEquals(DBSingleton.getInstance(context).getDB().getSpecificPITData("id1", "ip1"), null);

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData3));

        DBData pitSpecific1 = DBSingleton.getInstance(context).getDB().getSpecificPITData(pitData1.getUserID(), pitData1.getIpAddr());
        DBData pitSpecific3 = DBSingleton.getInstance(context).getDB().getSpecificPITData(pitData3.getUserID(), pitData3.getIpAddr());

        // test that pitData1 was returned correctly
        assertEquals(pitSpecific1.getIpAddr(), pitData1.getIpAddr());
        assertEquals(pitSpecific1.getUserID(), pitData1.getUserID());

        // test that pitData3 was returned correctly
        assertEquals(pitSpecific3.getIpAddr(), pitData3.getIpAddr());
        assertEquals(pitSpecific3.getUserID(), pitData3.getUserID());

        // test bad input gets a null return
        assertEquals(DBSingleton.getInstance(context).getDB().getSpecificCSData(null, null), null);
        assertEquals(DBSingleton.getInstance(context).getDB().getSpecificCSData("validUSERID", null), null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetFIBData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB before testing insertion

        // check null returned for empty FIB
        assertEquals(DBSingleton.getInstance(context).getDB().getAllFIBData(), null);

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData2));

        DBData fibReturn1 = DBSingleton.getInstance(context).getDB().getFIBData(fibData1.getUserID());
        DBData fibReturn2 = DBSingleton.getInstance(context).getDB().getFIBData(fibData2.getUserID());

        // test valid return
        assertEquals(fibReturn1.getUserID(), fibData1.getUserID());
        assertEquals(fibReturn2.getUserID(), fibData2.getUserID());

        // test bad input
        assertEquals(DBSingleton.getInstance(context).getDB().getFIBData(null), null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetGeneralCSData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete CS before testing insertion

        // check null returned for empty CS
        assertEquals(DBSingleton.getInstance(context).getDB().getGeneralCSData("userid"), null);

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData2));
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData3));

        ArrayList<DBData> csReturn1 = DBSingleton.getInstance(context).getDB().getGeneralCSData(csData1.getUserID());
        ArrayList<DBData> csReturn2 = DBSingleton.getInstance(context).getDB().getGeneralCSData(csData2.getUserID());

        // test valid data
        int foundCount = 0;
        for (int i = 0; i < csReturn1.size(); i++) {
            if (csReturn1.get(i).getTimeString().equals(csData1.getTimeString())) {
                foundCount ++;
            } else if (csReturn1.get(i).getTimeString().equals(csData3.getTimeString())) {
                foundCount ++;
            }
        }

        // should have found three entries (only three were inserted under this userID)
        assertEquals(foundCount, 2);

        // should only return one entry (only one test case inserted under this userID)
        assertEquals(csReturn2.get(0).getDataFloat(), csData2.getDataFloat());

        // test bad data
        assertEquals(DBSingleton.getInstance(context).getDB().getGeneralCSData(null), null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetAllFIBData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB before testing insertion

        assertEquals(DBSingleton.getInstance(context).getDB().getAllFIBData(), null); // check that empty FIB returns null

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData2));

        ArrayList<DBData> allFIBData = DBSingleton.getInstance(context).getDB().getAllFIBData();

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
     * @throws Exception
     */
    public void testGetAllSensorData() throws Exception {

        // add data before testing
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData2));

        ArrayList<DBData> sensorData = DBSingleton.getInstance(context).getDB().getAllSensorData();

        // both entries should have been returned (plus the HeartbeatSensor which is always present)
        assertTrue(sensorData.size() == 2 + 1);

        boolean entry1Found = false;
        boolean entry2Found = false;

        for (int i = 0; i < sensorData.size(); i++) {

            if (sensorData.get(i).getSensorID().equals(sensorData1.getSensorID())) {
                entry1Found = true;
            }

            if (sensorData.get(i).getSensorID().equals(sensorData2.getSensorID())) {
                entry2Found = true;
            }
        }

        assertTrue(entry1Found && entry2Found); // verify that both were detected

        // delete after testing
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData1.getSensorID()));
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData2.getSensorID()));
    }

    /**
     * @throws Exception
     */
    public void testGetAllPacketData() throws Exception {

        // add data before testing
        assertTrue(DBSingleton.getInstance(context).getDB().addPacketData(packetData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addPacketData(packetData2));

        ArrayList<DBData> packetData = DBSingleton.getInstance(context).getDB().getAllPacketData();

        boolean entry1Found = false;
        boolean entry2Found = false;

        for (int i = 0; i < packetData.size(); i++) {

            if (packetData.get(i).getPacketName().equals(packetData1.getPacketName())) {
                entry1Found = true;
            }

            if (packetData.get(i).getPacketName().equals(packetData2.getPacketName())) {
                entry2Found = true;
            }
        }

        assertTrue(entry1Found && entry2Found); // verify that both were detected

        DBSingleton.getInstance(context).getDB().getAllPacketData(); // delete after testing
    }

    /**
     * @throws Exception for failed tests
     */
    public void testGetSpecificCSData() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete CS before testing insertion

        // check null returned for empty CS
        assertEquals(DBSingleton.getInstance(context).getDB().getSpecificCSData("userid", "timestring"), null);

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData2));
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData3));

        // test that, although both have same userID, two different entries are returned
                    // due to their respective time stamps
        DBData csReturn1 = DBSingleton.getInstance(context).getDB().getSpecificCSData(csData1.getUserID(), csData1.getTimeString());
        DBData csReturn3 = DBSingleton.getInstance(context).getDB().getSpecificCSData(csData3.getUserID(), csData3.getTimeString());

        assertEquals(csReturn1.getDataFloat(), csData1.getDataFloat());
        assertEquals(csReturn3.getDataFloat(), csData3.getDataFloat());
    }

    /**
     * @throws Exception
     */
    public void testGetSpecificSensorData() throws Exception {

        // add data before testing
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData2));

        // test against actual entries
        DBData sensor1 = DBSingleton.getInstance(context).getDB().getSpecificSensorData(sensorData1.getSensorID());
        DBData sensor2 = DBSingleton.getInstance(context).getDB().getSpecificSensorData(sensorData2.getSensorID());

        assertTrue(sensor1.getSensorID().equals(sensorData1.getSensorID()));
        assertTrue(sensor2.getSensorID().equals(sensorData2.getSensorID()));

        // test against false entries
        DBData sensor3 = DBSingleton.getInstance(context).getDB().getSpecificSensorData("asdpfoiamsdf");
        assertTrue(sensor3 == null);

        // delete after testing
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData1.getSensorID()));
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData2.getSensorID()));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testUpdateFIBData() throws Exception {

        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData1));

        // check original return
        DBData originalReturn = DBSingleton.getInstance(context).getDB().getFIBData(fibData1.getUserID());
        assertEquals(originalReturn.getIpAddr(), fibData1.getIpAddr());

        // now, modify and check return
        fibData1.setIpAddr("99.99.99.99");

        // test validity of update
        assertTrue(DBSingleton.getInstance(context).getDB().updateFIBData(fibData1));

        DBData updatedReturn = DBSingleton.getInstance(context).getDB().getFIBData(fibData1.getUserID());
        assertEquals(updatedReturn.getIpAddr(), fibData1.getIpAddr());

        // test bad update return
        assertFalse(DBSingleton.getInstance(context).getDB().updateFIBData(null));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testUpdatePITData() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData1));

        // check original return
        DBData originalReturn = DBSingleton.getInstance(context).getDB().getSpecificPITData(pitData1.getUserID(), pitData1.getIpAddr());
        assertEquals(originalReturn.getIpAddr(), pitData1.getIpAddr());

        // now, modify and check return
        pitData1.setIpAddr("99.99.99.99");

        // test validity of update
        assertTrue(DBSingleton.getInstance(context).getDB().updatePITData(pitData1));

        DBData updatedReturn = DBSingleton.getInstance(context).getDB().getSpecificPITData(pitData1.getUserID(), pitData1.getIpAddr());
        assertEquals(updatedReturn.getIpAddr(), pitData1.getIpAddr());

        // test bad update return
        assertFalse(DBSingleton.getInstance(context).getDB().updatePITData(null));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testUpdateCSData() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData1));

        // check original return
        DBData originalReturn = DBSingleton.getInstance(context).getDB().getSpecificCSData(csData1.getUserID(), csData1.getTimeString());
        assertEquals(originalReturn.getDataFloat(), csData1.getDataFloat());

        // now, modify and check return
        csData1.setDataFloat("99,99,99,99");

        // test validity of update
        assertTrue(DBSingleton.getInstance(context).getDB().updateCSData(csData1));

        DBData updatedReturn = DBSingleton.getInstance(context).getDB().getSpecificCSData(csData1.getUserID(), csData1.getTimeString());
        assertEquals(updatedReturn.getDataFloat(), csData1.getDataFloat());

        // test bad update return
        assertFalse(DBSingleton.getInstance(context).getDB().updateCSData(null));
    }

    /**
     * @throws Exception
     */
    public void testUpdateSensorEntry() throws Exception {

        // add data before testing
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData2));

        // update the interval to a new, arbitrarily chosen, value
        sensorData1.setSensorCollectionInterval(10 + sensorData1.getSensorCollectionInterval() * 10);

        assertTrue(DBSingleton.getInstance(context).getDB().updateSensorData(sensorData1));

        DBData updatedSensor1 = DBSingleton.getInstance(context).getDB().getSpecificSensorData(sensorData1.getSensorID());

        assertTrue(updatedSensor1.getSensorCollectionInterval() == sensorData1.getSensorCollectionInterval());

        // delete after testing
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData1.getSensorID()));
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData2.getSensorID()));
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeletePITEntry() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntirePIT(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addPITData(pitData1));

        // check original return
        DBData originalReturn = DBSingleton.getInstance(context).getDB().getSpecificPITData(pitData1.getUserID(), pitData1.getIpAddr());
        assertEquals(originalReturn.getIpAddr(), pitData1.getIpAddr());

        // check deletePITEntry rejects bad data
        assertFalse(DBSingleton.getInstance(context).getDB().deletePITEntry(null, pitData1.getTimeString(), pitData1.getIpAddr()));

        // check deletePITEntry accepts valid deletion
        assertTrue(DBSingleton.getInstance(context).getDB().deletePITEntry(pitData1.getUserID(), pitData1.getTimeString(), pitData1.getIpAddr()));

        // check, now deleted, entry returns null
        assertEquals(DBSingleton.getInstance(context).getDB().getSpecificPITData(pitData1.getUserID(), pitData1.getIpAddr()), null);
    }

    /**
     * @throws Exception
     */
    public void testDeleteAllPackets() throws Exception {
        // for safety, delete prior to test
        DBSingleton.getInstance(context).getDB().deleteEntirePacketDB(); // delete after testing

        // test adding good data
        assertTrue(DBSingleton.getInstance(context).getDB().addPacketData(packetData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addPacketData(packetData2));

        // test return is valid object
        ArrayList<DBData> allPackets = DBSingleton.getInstance(context).getDB().getAllPacketData();
        assertTrue(allPackets != null);

        // delete
        DBSingleton.getInstance(context).getDB().deleteEntirePacketDB(); // delete after testing

        // now, after deletion, test return is a null object
        ArrayList<DBData> allPacketsAfterDeletion = DBSingleton.getInstance(context).getDB().getAllPacketData();

        assertTrue(allPacketsAfterDeletion == null);
    }

    /**
     * @throws Exception
     */
    public void testDeleteSensorEntry() throws Exception {
        // add data before testing
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData1));
        assertTrue(DBSingleton.getInstance(context).getDB().addSensorData(sensorData2));

        // delete
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData1.getSensorID()));
        assertTrue(DBSingleton.getInstance(context).getDB().deleteSensorEntry(sensorData2.getSensorID()));

        // now, after deletion, test return is a null object
        DBData afterDeleteSensor1 = DBSingleton.getInstance(context).getDB().getSpecificSensorData(sensorData1.getUserID());
        DBData afterDeleteSensor2 = DBSingleton.getInstance(context).getDB().getSpecificSensorData(sensorData2.getUserID());
        assertTrue(afterDeleteSensor1 == null);
        assertTrue(afterDeleteSensor2 == null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeleteFIBEntry() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntireFIB(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addFIBData(fibData1));

        // check original return
        DBData originalReturn = DBSingleton.getInstance(context).getDB().getFIBData(fibData1.getUserID());
        assertEquals(originalReturn.getIpAddr(), fibData1.getIpAddr());

        // check null data rejected
        assertFalse(DBSingleton.getInstance(context).getDB().deleteFIBEntry(null));

        // check valid data accepted
        assertTrue(DBSingleton.getInstance(context).getDB().deleteFIBEntry(fibData1.getUserID()));

        // check, now deleted, entry returns null
        assertEquals(DBSingleton.getInstance(context).getDB().getFIBData(fibData1.getUserID()), null);
    }

    /**
     * @throws Exception for failed tests
     */
    public void testDeleteCSEntry() throws Exception {
        DBSingleton.getInstance(context).getDB().deleteEntireCS(); // delete FIB before testing insertion

        // test addition of valid data
        assertTrue(DBSingleton.getInstance(context).getDB().addCSData(csData1));

        // check original return
        DBData originalReturn = DBSingleton.getInstance(context).getDB().getSpecificCSData(csData1.getUserID(), csData1.getTimeString());
        assertEquals(originalReturn.getDataFloat(), csData1.getDataFloat());

        // check null data rejected
        assertFalse(DBSingleton.getInstance(context).getDB().deleteCSEntry(null, null));

        // check valid data accepted
        assertTrue(DBSingleton.getInstance(context).getDB().deleteCSEntry(csData1.getUserID(), csData1.getTimeString()));

        // check, now deleted, entry returns null
        assertEquals(DBSingleton.getInstance(context).getDB().getSpecificCSData(csData1.getUserID(), csData1.getTimeString()), null);
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