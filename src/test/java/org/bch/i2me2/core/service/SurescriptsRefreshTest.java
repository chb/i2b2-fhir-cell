package org.bch.i2me2.core.service;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.external.I2B2CellFR;
import org.bch.i2me2.core.external.IDM;
import org.bch.i2me2.core.external.RXConnect;
import org.bch.i2me2.core.util.HttpRequest;
import org.bch.i2me2.core.util.SoapRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;
/**
 * Created by CH176656 on 3/20/2015.
 */


public class SurescriptsRefreshTest {

    // Mocks
    @Mock private IDM idm;
    @Mock private RXConnect rxConnect;
    @Mock private I2B2CellFR i2b2;

    @Mock private HttpRequest httpRequest;
    @Mock private SoapRequest soapRequest;

    // No need to mock this
    private MapperRxToPDO mapper = new MapperRxToPDO();

    // The class we want to test
    private SurescriptsRefresh surescriptsR;

    private static String firstName = "FIRST NAME";
    private static String lastName = "LAST NAME";
    private static String dob ="19551010";
    private static String dobF ="1955-10-10";
    private static String gender ="F";
    private static String zip = "12345";
    private static String id = "66666";
    private static String token = "TOKEN";

    private static String json =
            "{\n" +
            "  \"messageType\": \"RDS\",\n" +
            "  \"msgCtrlID\": \"666\",\n" +
            "  \"msgDateTime\": \"Feb 12, 2015 12:00:00 AM\",\n" +
            "  \"sendingSystem\": \"RXHUB\",\n" +
            "  \"orders\": [],\n" +
            "  \"rxRecsReturned\": \"ALL\",\n" +
            "  \"statusMsg\": \"\"\n" +
            "}";


    private static String xmlpdo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<repository:patient_data\n" +
            "    xmlns:repository=\"http://i2b2.mgh.harvard.edu/repository_cell\"\n" +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xsi:schemaLocation=\"http://i2b2.mgh.harvard.edu/repository_cell/patient_data.xsd\">    <pid_set>\n" +
            "        <pid>\n" +
            "            <patient_id source=\"BCH\">" + id + "</patient_id>\n" +
            "        </pid>\n" +
            "\n" +
            "    </pid_set>\n" +
            "    <eid_set>\n" +
            "        <eid>\n" +
            "            <event_id patient_id=\"" + id + "\" patient_id_source=\"BCH\" source=\"SCR\">666</event_id>\n" +
            "        </eid>\n" +
            "\n" +
            "    </eid_set>\n" +
            "    <event_set>\n" +
            "        <event>\n" +
            "            <event_id source=\"SCR\">666</event_id>\n" +
            "            <patient_id source=\"BCH\">" + id + "</patient_id>\n" +
            "            <start_date>2015-02-12T12:00:00.00</start_date>\n" +
            "        </event>\n" +
            "\n" +
            "    </event_set>\n" +
            "    <patient_set>\n" +
            "        <patient>\n" +
            "            <patient_id source=\"BCH\">" + id + "</patient_id>\n" +
            "            <param column=\"sex_cd\">" + gender + "</param>\n" +
            "            <param column=\"birth_date\">"+ dobF +"</param>\n" +
            "        </patient>\n" +
            "\n" +
            "    </patient_set>\n" +
            "    <observation_set>\n" +
            "        <observation>\n" +
            "            <event_id source=\"SCR\">666</event_id>\n" +
            "            <patient_id source=\"BCH\">" + id + "</patient_id>\n" +
            "            <start_date>2015-02-12T12:00:00.00</start_date>\n" +
            "            <observer_cd>@</observer_cd>\n" +
            "            <concept_cd>PBM_transaction</concept_cd>\n" +
            "            <tval_char>ALL</tval_char>\n" +
            "            <modifier_cd>rxRecsReturnedXX</modifier_cd>\n" +
            "            <valuetype_cd>T</valuetype_cd>\n" +
            "            <instance_num>1</instance_num>\n" +
            "        </observation>\n" +
            "        <observation>\n" +
            "            <event_id source=\"SCR\">666</event_id>\n" +
            "            <patient_id source=\"BCH\">" + id + "</patient_id>\n" +
            "            <start_date>2015-02-12T12:00:00.00</start_date>\n" +
            "            <observer_cd>@</observer_cd>\n" +
            "            <concept_cd>PBM_transaction</concept_cd>\n" +
            "            <tval_char></tval_char>\n" +
            "            <modifier_cd>statusMsg</modifier_cd>\n" +
            "            <valuetype_cd>T</valuetype_cd>\n" +
            "            <instance_num>1</instance_num>\n" +
            "        </observation>\n" +
            "\n" +
            "    </observation_set>\n" +
            "</repository:patient_data>";


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.surescriptsR = new SurescriptsRefresh();

        this.surescriptsR.setI2B2(i2b2);
        this.surescriptsR.setRXConnect(rxConnect);
        this.surescriptsR.setIdm(idm);
        this.surescriptsR.setMapper(mapper);

        this.idm.setHttpRequest(httpRequest);
        this.rxConnect.setHttpRequest(httpRequest);
        this.i2b2.setHttpRequest(httpRequest);
        this.i2b2.setSoapRequest(soapRequest);

    }

    @Test
    public void refresh_HappyPathTest() throws Exception {
        IDM.PersonalInfo phiId = new IDM.PersonalInfo();
        phiId.setSubjectId(id);
        IDM.PersonalInfo phiAdt = new IDM.PersonalInfo();
        phiAdt.setFirstName(firstName);
        phiAdt.setLastName(lastName);
        phiAdt.setBirthDate(dob);
        phiAdt.setZipCode(zip);
        phiAdt.setGender(gender);

        when(idm.getPersonalSubjectId(token)).thenReturn(phiId);
        when(idm.getPersonalInfo(token)).thenReturn(phiAdt);
        when(rxConnect.getMedicationsList(firstName,lastName,zip,dob,gender)).thenReturn(json);
        when(i2b2.pushPDOXML(xmlpdo)).thenReturn(null);

        this.surescriptsR.refresh(token);
        verify(idm, times(1)).getPersonalSubjectId(token);
        verify(idm, times(1)).getPersonalInfo(token);
        verify(rxConnect, times(1)).getMedicationsList(firstName,lastName,zip,dob,gender);
        verify(i2b2, times(1)).pushPDOXML(xmlpdo);
    }

    @Test
    public void refresh_TokenNullTest() throws Exception {
        try {
            this.surescriptsR.refresh(null);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("token"));
            assertTrue(e.getMessage().toLowerCase().contains("null"));
        }
    }
}
