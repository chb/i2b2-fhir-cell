package org.bch.fhir.i2b2.pdomodel;

import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the construction of a PDO XML ready to be pushed to a i2b2 instance
 * @author CHIP-IHL
 */
public class PDOModel {
    public static final String PDO_EVENT_ID =           "event_id";
    public static final String PDO_PATIENT_ID =         "patient_id";
    public static final String PDO_START_DATE =         "start_date";
    public static final String PDO_END_DATE =           "end_date";
    public static final String PDO_OBSERVER_CD =        "observer_cd";
    public static final String PDO_CONCEPT_CD =         "concept_cd";
    public static final String PDO_INSTANCE_NUM =       "instance_num";
    public static final String PDO_TVAL_CHAR =          "tval_char";
    public static final String PDO_NVAL_NUM =           "nval_num";
    public static final String PDO_UNITS_CD =           "units_cd";
    public static final String PDO_MODIFIER_CD =        "modifier_cd";
    public static final String PDO_VALUETYPE_CD =       "valuetype_cd";
    public static final String PDO_PATIENT_ID_SOURCE =  "patient_id_source";
    public static final String PDO_PARAM =              "param";
    public static final String PDO_COLUMN_ZIP_CD =      "zip_cd";
    public static final String PDO_COLUMN_STATE_PATH =  "statecityzip_path";
    public static final String PDO_SOURCE =             "source";
    public static final String PDO_COLUMN =             "column";
    public static final String PDO_TYPE =               "type";



    private static String PDO_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<repository:patient_data\n" +
                "    xmlns:repository=\"http://i2b2.mgh.harvard.edu/repository_cell\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xsi:schemaLocation=\"http://i2b2.mgh.harvard.edu/repository_cell/patient_data.xsd\">\n";
    private static String PDO_FOOT = "</repository:patient_data>";

    private List<ElementSet> elementSets = new ArrayList<>();

    /**
     * Adds an {@link ElementSet} object in the pdo document
     * @param elementSet
     */
    public void addElementSet(ElementSet elementSet) {
        this.elementSets.add(elementSet);
    }

    /**
     * Removes an {@link ElementSet} object from the pdo document
     * @param elementSet
     */
    public void removeElementSet(ElementSet elementSet) {
        this.elementSets.remove(elementSet);
    }

    /**
     * Returns the pdo xml document as String
     * @return  The entire document
     * @throws FHIRI2B2Exception In case some of the {@link ElementSet} objects and {@link Element} are not initialized
     */
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
