package org.bch.i2me2.core.external;

import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.util.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by CH176656 on 3/23/2015.
 */
public class IDMTest {

    private IDM idm;

    @Mock
    private HttpRequest http;

    @Mock
    private HttpRequest.Response resp;

    private static String token = "THISISATOKEN";

    private static String firstName = "FIRST_NAME";
    private static String lastName = "LAST_NAME";
    private static String birthDate = "19900205";
    private static String gender = "F";
    private static String zip = "12345-1234";
    private static String subjectId = "444444";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        idm = new IDM();
        idm.setHttp(http);
    }

    // Test token
    @Test
    public void getPersonalInormationTokenTest() throws Exception {

        // null
        try {
            idm.getPersonalInormation(null);
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(IDM.PARAM_TOKEN));
        }

        // empty
        try {
            idm.getPersonalInormation("");
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains(IDM.PARAM_TOKEN));
        }
    }

    // Return 400 code error
    @Test
    public void getPersonalInormationExceptionPathIDMTest() throws Exception {
        when(resp.getResponseCode()).thenReturn(400);
        when(http.doPostGeneric(anyString(), anyString(), anyString(), anyString())).thenReturn(resp);
        try {
            this.idm.getPersonalInormation(token);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().contains("Error code: 400"));
        }
    }

    // Happy path. We check all returned params are fine
    @Test
    public void getPersonalInormationHappyPathIDMTest() throws Exception {
        when(resp.getResponseCode()).thenReturn(200);
        when(resp.getContent()).thenReturn(generateJSON());
        when(http.doPostGeneric(anyString(), anyString(), anyString(), anyString())).thenReturn(resp);
        IDM.PersonalInfo pi = this.idm.getPersonalInormation(token);
        assertEquals(firstName, pi.getFirstName());
        assertEquals(lastName, pi.getLastName());
        assertEquals(birthDate,pi.getBirthDate());
        assertEquals(gender,pi.getGender());
        assertEquals(zip, pi.getZipCode());
        assertEquals(subjectId, pi.getSubjectId());
    }

    private String generateJSON() {
        String out = "{";
        out += keyValue(IDM.FIRST_NAME_KEY, firstName) + ",";
        out += keyValue(IDM.LAST_NAME_KEY, lastName) + ",";
        out += keyValue(IDM.BIRTH_DATE_KEY,birthDate ) + ",";
        out += keyValue(IDM.GENDER_KEY, gender) + ",";
        out += keyValue(IDM.ZIP_CODE_KEY, zip) + ",";
        out += keyValue(IDM.SUBJECT_ID_KEY, subjectId) + ",";
        out += "}";
        return out;
    }

    private String keyValue(String key, String value) {
        return "\""+key+"\":\""+value+"\"";
    }
}
