package org.bch.fhir.i2b2.pdomodel;

import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Models an individual element of a pdo xml {patient, pid, eid, event, observation}
 * @author CHIP-IHL
 */
public class Element {
    public static String PDO_PATIENT = "patient";
    public static String PDO_PID = "pid";
    public static String PDO_EID = "eid";
    public static String PDO_EVENT = "event";
    public static String PDO_OBSERVATION = "observation";

    private String typePDO = null;
    private List<String> rows = new ArrayList<>();

    /**
     * Sets the xml pdo type:
     * {@link Element#PDO_PATIENT}
     * {@link Element#PDO_EID}
     * {@link Element#PDO_PID}
     * {@link Element#PDO_EVENT}
     * {@link Element#PDO_OBSERVATION}
     * @param typePDO
     */
    public void setTypePDO(String typePDO) {
        this.typePDO = typePDO;
    }

    /**
     * Get the element type. See {@link Element#setTypePDO(String)}
     * @return The type
     */
    public String getTypePDO() {
        return this.typePDO;
    }

    /**
     * Add a row in the element
     * @param row the row. e.i. "<tval_char>value</tval_char>"
     */
    public void addRow(String row) {
        this.rows.add(row);
    }

    /**
     * Returns the element as String
     * @return  The String
     * @throws FHIRI2B2Exception In case the element type has not been initialized
     */
    public String toPDOString() throws FHIRI2B2Exception {
        if (this.typePDO == null) throw new FHIRI2B2Exception("typePDO in Element object has not been initialized");
        StringBuffer out = new StringBuffer();
        out.append("<").append(this.getTypePDO()).append(">\n");
        for(String row: this.rows) {
            out.append(row).append("\n");
        }
        out.append("</").append(this.getTypePDO()).append(">\n");

        return out.toString();
    }
}
