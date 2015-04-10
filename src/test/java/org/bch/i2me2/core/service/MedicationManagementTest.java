package org.bch.i2me2.core.service;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.external.I2B2QueryService;
import org.bch.i2me2.core.util.Utils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by CH176656 on 4/10/2015.
 */
public class MedicationManagementTest {
    private static String xmlTestRefresh ="" +
            "<observation_set>\n" +
            "        <observation>\n" +
            "            <event_id source=\"SCR\">050045624</event_id>\n" +
            "            <patient_id source=\"BCH\">1234</patient_id>\n" +
            "            <start_date>%s</start_date>\n" +
            "            <concept_cd>PBM_transaction</concept_cd>\n" +
            "            <tval_char>%s</tval_char>\n" +
            "            <modifier_cd>%s</modifier_cd>\n"+
            "        </observation>\n" +
            "        <observation>\n" +
            "            <event_id source=\"SCR\">050045624</event_id>\n" +
            "            <patient_id source=\"BCH\">1234</patient_id>\n" +
            "            <start_date>2011-02-12T12:00:00.00</start_date>\n" +
            "            <concept_cd>PBM_transaction</concept_cd>\n" +
            "            <tval_char>ALL</tval_char>\n" +
            "            <modifier_cd>%s</modifier_cd>\n"+
            "        </observation>\n" +
            "        <observation>\n" +
            "            <event_id source=\"SCR\">050045624</event_id>\n" +
            "            <patient_id source=\"BCH\">1234</patient_id>\n" +
            "            <start_date>2014-02-20T10:11:12.00</start_date>\n" +
            "        </observation>\n" +
            "        <observation>\n" +
            "            <event_id source=\"SCR\">050045624</event_id>\n" +
            "            <patient_id source=\"BCH\">123456</patient_id>\n" +
            "            <start_date>2013-02-20T10:11:12.00</start_date>\n" +
            "        </observation>\n" +
            "</observation_set>";

    @Mock
    private I2B2QueryService i2b2QueryService;

    @Mock
    private SurescriptsRefresh surescriptsRefresh;

    private MedicationsManagement medicationsManagement;
    private String rxStatusMessageMod;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.medicationsManagement = new MedicationsManagement();
        this.medicationsManagement.setI2b2QueryService(this.i2b2QueryService);
        this.medicationsManagement.setSurescriptsRefresh(this.surescriptsRefresh);
        rxStatusMessageMod = AppConfig.getRealModifiersMap().get(MedicationsManagement.INTERNAL_RX_RECS_RETURNED);

    }

    // Test that surescript Refresh is not call. We assume tha DAYS_WINDOW_SURESCRIPTS is higher than 1 day!
    @Test
    public void getMedications_NoSureScriptsRefresh_Test() throws Exception {
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat(AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2));
        String dateTime = dateFormatOutput.format(new Date());
        String xml = String.format(xmlTestRefresh, dateTime, "ALL", rxStatusMessageMod, rxStatusMessageMod);
        String patientId = "1234";
        String token = "token";
        Date date = new Date();

        I2B2QueryService.QueryResponse resp = new I2B2QueryService.QueryResponse(xml);
        when(i2b2QueryService.getPatientData(anyString(), anyString(), (Date) anyObject())).thenReturn(resp);

        this.medicationsManagement.getMedications(patientId, token);
        verify(surescriptsRefresh, times(0)).refresh(token);
    }

    // Test that surescript Refresh is not call in the limit. We assume tha DAYS_WINDOW_SURESCRIPTS is higher than 1 day!
    @Test
    public void getMedications_NoSureScriptsRefresh2_Test() throws Exception {
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat(AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2));
        int days = Integer.parseInt(AppConfig.getProp(AppConfig.DAYS_WINDOW_SURESCRIPT));
        Date date = Utils.subtractDays(new Date(), days-1);
        String dateTime = dateFormatOutput.format(date);
        String xml = String.format(xmlTestRefresh, dateTime, "ALL", rxStatusMessageMod ,rxStatusMessageMod);
        String patientId = "1234";
        String token = "token";

        I2B2QueryService.QueryResponse resp = new I2B2QueryService.QueryResponse(xml);
        when(i2b2QueryService.getPatientData(anyString(), anyString(), (Date) anyObject())).thenReturn(resp);

        this.medicationsManagement.getMedications(patientId, token);
        verify(surescriptsRefresh, times(0)).refresh(token);
    }

    // Test that surescript Refresh is called in the limit.
    @Test
    public void getMedications_SureScriptsRefresh_Test() throws Exception {
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat(AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2));
        int days = Integer.parseInt(AppConfig.getProp(AppConfig.DAYS_WINDOW_SURESCRIPT));
        Date date = Utils.subtractDays(new Date(), days);
        String dateTime = dateFormatOutput.format(date);
        String xml = String.format(xmlTestRefresh, dateTime, "ALL", rxStatusMessageMod, rxStatusMessageMod);
        String patientId = "1234";
        String token = "token";

        I2B2QueryService.QueryResponse resp = new I2B2QueryService.QueryResponse(xml);
        when(i2b2QueryService.getPatientData(anyString(), anyString(), (Date) anyObject())).thenReturn(resp);

        this.medicationsManagement.getMedications(patientId, token);
        verify(surescriptsRefresh, times(1)).refresh(token);
    }

    // Test that surescript Refresh is called.
    @Test
    public void getMedications_SureScriptsRefresh2_Test() throws Exception {
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat(AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2));
        int days = Integer.parseInt(AppConfig.getProp(AppConfig.DAYS_WINDOW_SURESCRIPT));
        Date date = Utils.subtractDays(new Date(), days+300);
        String dateTime = dateFormatOutput.format(date);
        String xml = String.format(xmlTestRefresh, dateTime, "ALL", rxStatusMessageMod, rxStatusMessageMod);
        String patientId = "1234";
        String token = "token";

        I2B2QueryService.QueryResponse resp = new I2B2QueryService.QueryResponse(xml);
        when(i2b2QueryService.getPatientData(anyString(), anyString(), (Date) anyObject())).thenReturn(resp);

        this.medicationsManagement.getMedications(patientId, token);
        verify(surescriptsRefresh, times(1)).refresh(token);
    }

    // Test that surescript Refresh is called when different of "ALL", even when dateTime is close
    @Test
    public void getMedications_SureScriptsRefresh3_Test() throws Exception {
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat(AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2));
        String dateTime = dateFormatOutput.format(new Date());
        String xml = String.format(xmlTestRefresh, dateTime, "SOME", rxStatusMessageMod, rxStatusMessageMod);
        String patientId = "1234";
        String token = "token";

        I2B2QueryService.QueryResponse resp = new I2B2QueryService.QueryResponse(xml);
        when(i2b2QueryService.getPatientData(anyString(), anyString(), (Date) anyObject())).thenReturn(resp);

        this.medicationsManagement.getMedications(patientId, token);
        verify(surescriptsRefresh, times(1)).refresh(token);
    }
}

