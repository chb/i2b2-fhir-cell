package org.bch.i2me2.core.external;

import org.bch.i2me2.core.exception.I2ME2Exception;
import org.jboss.arquillian.junit.Arquillian;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import static org.junit.Assert.*;

/**
 * Integration Tests for RXConnect
 * NOTE: It does not require Arquillian, since RXConnect it's deployed in a dev VM machine
 * Created by CH176656 on 3/30/2015.
 */
@RunWith(Arquillian.class)
public class RXConnectIT extends AbstractIT {

    // Test case with data - Return ok. 6 claims and 53 fills
    private static String firstName="Bert";
    private static String lastName="Schnur";
    private static String birthDate="19450419";
    private static String gender="M";
    private static String zip="63050";

    // test case with no data -- 0 returned with error message
    private static String firstName2="David";
    private static String lastName2="Thrower";
    private static String birthDate2="19330222";
    private static String gender2="M";
    private static String zip2="34737";

    // test case with some data.. just 3 claims
    private static String firstName3="Johnathan";
    private static String lastName3="Swift";
    private static String birthDate3="19791024";
    private static String gender3="M";
    private static String zip3="55427";

    @Inject
    private RXConnect rxconnect;

    // We simulate the injection

    @Before
    public void setUp() {

    }

    @Test
    public void getMedicationsListCase_1_IT() throws Exception {
        String resp = rxconnect.getMedicationsList(firstName, lastName,zip, birthDate, gender);
        JSONObject json = new JSONObject(resp);
        JSONObject rxhistory = json; //json.getJSONObject("RxHistorySegments");
        String status = rxhistory.getString("rxRecsReturned");
        assertEquals("ALL", status);

        JSONArray orders = json.getJSONArray("orders");
        assertEquals(59, orders.length());
    }

    // Error
    @Test
    public void getMedicationsListCase_2_IT() throws Exception {
        try {
            String resp = rxconnect.getMedicationsList(firstName2, lastName2, zip2, birthDate2, gender2);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains("502"));
        }
    }

    @Test
    public void getMedicationsListCase_3_IT() throws Exception {
        String resp = rxconnect.getMedicationsList(firstName3, lastName3, zip3, birthDate3, gender3);
        JSONObject json = new JSONObject(resp);
        JSONObject rxhistory = json;//json.getJSONObject("RxHistorySegments");
        String status = rxhistory.getString("rxRecsReturned");
        assertEquals("SOME", status);

        JSONArray orders = json.getJSONArray("orders");
        assertEquals(3, orders.length());
    }

}
