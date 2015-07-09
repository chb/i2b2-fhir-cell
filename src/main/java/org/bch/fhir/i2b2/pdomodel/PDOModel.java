package org.bch.fhir.i2b2.pdomodel;

import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the construction of a PDO XML ready to be pushed to a i2b2 instance
 * Created by ipinyol on 7/9/15.
 */
public class PDOModel {
    public static final String PDO_EVENT_ID = "event_id";
    public static final String PDO_PATIENT_ID = "patient_id";
    public static final String PDO_START_DATE = "start_date";
    public static final String PDO_END_DATE = "end_date";
    public static final String PDO_OBSERVER_CD = "observer_cd";
    public static final String PDO_CONCEPT_CD = "concept_cd";
    public static final String PDO_TVAL_CHAR = "tval_char";
    public static final String PDO_NVAL_NUM = "nval_numr";
    public static final String PDO_MODIFIER_CD = "modifier_cd";
    public static final String PDO_VALUETYPE_CD = "valuetype_cd";


    private static String PDO_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<repository:patient_data\n" +
                "    xmlns:repository=\"http://i2b2.mgh.harvard.edu/repository_cell\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xsi:schemaLocation=\"http://i2b2.mgh.harvard.edu/repository_cell/patient_data.xsd\">+\n";
    private static String PDO_FOOT = "</repository:patient_data>";

    private List<ElementSet> elementSets = new ArrayList<>();

    public void addElementSet(ElementSet elementSet) {
        this.elementSets.add(elementSet);
    }

    public void removeElementSet(ElementSet elementSet) {
        this.elementSets.remove(elementSet);
    }

    public String generatePDOXML() throws FHIRI2B2Exception {
        StringBuffer out = new StringBuffer();
        out.append(PDO_HEADER);
        for(ElementSet e: this.elementSets) {
            out.append(e.toPDOString());
        }
        out.append(PDO_FOOT);
        return out.toString();
    }

}
