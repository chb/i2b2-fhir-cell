package org.bch.i2me2.core.external;

import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.util.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * Created by CH176656 on 3/23/2015.
 */
public class RXConnectTest {

    private static String firstName="firstName";
    private static String lastName="lastName";
    private static String birthDate="19450505";
    private static String gender="F";
    private static String zip="12345";

    private RXConnect rxconnect;

    @Mock
    private HttpRequest http;

    @Mock
    private HttpRequest.Response resp;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        rxconnect = new RXConnect();
        rxconnect.setHttp(http);
    }

    // First Name test
    @Test
    public void getMedicationListsFirstNameTest() throws Exception {
        fillAll();

        // Null
        this.rxconnect.setFirstName(null);
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_FIRST_NAME));
        }

        // Empty
        this.rxconnect.setFirstName("");
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_FIRST_NAME));
        }
    }

    // Last Name test
    @Test
    public void getMedicationListsLastNameTest() throws Exception {
        fillAll();

        // Null
        this.rxconnect.setLastName(null);
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_LAST_NAME));
        }

        // Empty
        this.rxconnect.setLastName("");
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_LAST_NAME));
        }
    }

    // birth Date test
    @Test
    public void getMedicationListsBirthDateTest() throws Exception {
        fillAll();

        // Null
        this.rxconnect.setBirthDate(null);
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_BIRTH_DATE));
        }

        // Empty
        this.rxconnect.setBirthDate("");
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_BIRTH_DATE));
        }

        // Wrong format
        this.rxconnect.setBirthDate("1957-05-01");
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_BIRTH_DATE));
        }

        // Invalid date
        this.rxconnect.setBirthDate("19450230");
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_BIRTH_DATE));
        }
    }

    // Gender test
    @Test
    public void getMedicationListsGenderTest() throws Exception {
        fillAll();

        // Null
        this.rxconnect.setGender(null);
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_GENDER));
        }

        // Empty
        this.rxconnect.setGender("");
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_GENDER));
        }

        // Different than F and M
        this.rxconnect.setGender("R");
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_GENDER));
        }
    }

    // zip test
    @Test
    public void getMedicationListsZipTest() throws Exception {
        fillAll();

        // Null
        this.rxconnect.setZipCode(null);
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_ZIP_CODE));
        }

        // Empty
        this.rxconnect.setZipCode("");
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_ZIP_CODE));
        }

        // Invalid format
        this.rxconnect.setZipCode("1234");
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_ZIP_CODE));
        }

        // Invalid format
        this.rxconnect.setZipCode("1234-4565456");
        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(RXConnect.PARAM_ZIP_CODE));
        }

    }

    // Happy path test
    @Test
    public void getMedicationListsHappyPathTest() throws Exception {
        fillAll();
        String json = "JSONSTRING";
        when(resp.getResponseCode()).thenReturn(200);
        when(resp.getContent()).thenReturn(json);
        when(http.doPostGeneric(anyString(), anyString())).thenReturn(resp);

        // Make sure that with both "F" and "M" the call works
        this.rxconnect.setGender("F");
        String out = this.rxconnect.getMedicationLists();
        verify(http, times(1)).doPostGeneric(anyString(), anyString());
        assertEquals(json, out);

        this.rxconnect.setGender("M");
        out = this.rxconnect.getMedicationLists();
        verify(http, times(2)).doPostGeneric(anyString(), anyString());
        assertEquals(json, out);

        // Make sure that with an extended zip code it still works
        this.rxconnect.setZipCode("12345-1234");
        out = this.rxconnect.getMedicationLists();
        verify(http, times(3)).doPostGeneric(anyString(), anyString());
        assertEquals(json, out);

        // Make sure that with an extended zip code it still works2
        this.rxconnect.setZipCode("12345 1234");
        out = this.rxconnect.getMedicationLists();
        verify(http, times(4)).doPostGeneric(anyString(), anyString());
        assertEquals(json, out);
    }

    // Exception path in case rxconnect returns >=400
    @Test
    public void getMedicationListsExceptionPathRXTest() throws Exception {
        fillAll();
        when(resp.getResponseCode()).thenReturn(400);
        when(http.doPostGeneric(anyString(), anyString())).thenReturn(resp);

        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains("Error code: 400"));
        }
    }

    // Exception path in case rxconnect throws IOException
    @Test
    public void getMedicationListsExceptionIORXTest() throws Exception {
        fillAll();
        when(http.doPostGeneric(anyString(), anyString())).thenThrow(new IOException());

        try {
            this.rxconnect.getMedicationLists();
            fail();
        } catch (IOException e) {
            // totally fine
        }
    }

    private void fillAll() {
        this.rxconnect.setFirstName(firstName);
        this.rxconnect.setLastName(lastName);
        this.rxconnect.setBirthDate(birthDate);
        this.rxconnect.setGender(gender);
        this.rxconnect.setZipCode(zip);
    }
}
