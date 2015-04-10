package org.bch.i2me2.core.service;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.external.I2B2QueryService;
import org.bch.i2me2.core.util.Utils;
import org.bch.i2me2.core.util.mapper.Mapper;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by CH176656 on 4/10/2015.
 */
public class MedicationsManagement extends WrapperService {
    /**
     * IME-26: Implemetnts the business logic of IME-26 returning xmlpdo
     * @param patiendId
     * @param source
     * @return
     */
    @Inject
    private I2B2QueryService i2b2QueryService;

    @Inject
    private SurescriptsRefresh surescriptsRefresh;

    public static String INTERNAL_RX_RECS_RETURNED = "rxRecsReturned";
    public static String ALL_RX_RECS_RETURNED_VALUE = "ALL";

    private static String MODULE = "[MEDICATION_MANAGEMENT]";
    private static String OP_GET_MED = "[GET_MEDICATIONS]";
    private static String OP_REFRESH = "[REFRESH_SURESCRIPTS]";

    public String getMedications(String patientId, String token) throws I2ME2Exception, IOException {
        this.log(Level.INFO, MODULE+OP_GET_MED+"IN. PatiendId:"+patientId);
        int daysWindow = Integer.parseInt(AppConfig.getProp(AppConfig.DAYS_WINDOW));

        // Set the baseline date for the medication list
        Date dateWindow = Utils.subtractDays(new Date(), daysWindow);

        // Get medication list from i2b2
        I2B2QueryService.QueryResponse resp = i2b2QueryService.getPatientData(
                patientId, AppConfig.getProp(AppConfig.I2B2_PDO_SOURCE_BCH), dateWindow);

        // we refresh surescripts if necessary
        if(sureScriptsNeedsRefresh(resp)) {
            surescriptsRefresh.refresh(token);
        }

        // We generate the xml pdo
        Document doc = resp.getObservationsByStartDate(dateWindow);
        String out;
        try {
            out = resp.documentToString(doc);
        } catch (TransformerException e) {
            this.log(Level.SEVERE, MODULE+OP_GET_MED+"Error transforming xmlDocument to String. PatiendId:"+patientId);
            throw new I2ME2Exception(MODULE+OP_GET_MED+"Error transforming xmlDocument to String. PatiendId:"+
                    patientId, e);
        }
        return out;
    }

    /**
     * Returns whether rxconnect data must be refreshed. See IME-26
     * @param resp
     * @return
     * @throws IOException
     * @throws I2ME2Exception
     */
    private boolean sureScriptsNeedsRefresh(I2B2QueryService.QueryResponse resp) throws IOException, I2ME2Exception {
        Map<String, String> modifiersMap = AppConfig.getRealModifiersMap();
        int daysSurescript = Integer.parseInt(AppConfig.getProp(AppConfig.DAYS_WINDOW_SURESCRIPT));
        Date dateSurescript = Utils.subtractDays(new Date(), daysSurescript);

        // We filter first by modifier_cd
        Document doc = resp.getObservationsByValue(
                Mapper.XmlPdoObservationTag.TAG_MODIFIER_CD.toString(), modifiersMap.get(INTERNAL_RX_RECS_RETURNED));

        // We filter by date
        doc = resp.getObservationsByStartDate(doc, dateSurescript);

        // We get the status value of the tval_char
        NodeList list = doc.getElementsByTagName(Mapper.XmlPdoObservationTag.TAG_TVAL.toString());
        if(list.getLength()==0) return true;

        String value = list.item(0).getTextContent().trim().toUpperCase();

        // We return true if value of rxStatusMsg is different of ALL
        return !value.equals(ALL_RX_RECS_RETURNED_VALUE.toUpperCase());
    }

    // For testing purposes only
    public void setI2b2QueryService(I2B2QueryService i2b2QueryService) {
        this.i2b2QueryService=i2b2QueryService;
    }

    public void setSurescriptsRefresh(SurescriptsRefresh surescriptsRefresh) {
        this.surescriptsRefresh = surescriptsRefresh;
    }
}
