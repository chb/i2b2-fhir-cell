package org.bch.fhir.i2b2.pdomodel;

import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ipinyol on 7/9/15.
 */
public class Element {
    public static String PDO_PATIENT = "patient";
    public static String PDO_PID = "pid";
    public static String PDO_EID = "eid";
    public static String PDO_EVENT = "event";
    public static String PDO_OBSERVATION = "observation";

    private String typePDO = null;
    private List<String> rows = new ArrayList<>();

    public void setTypePDO(String typePDO) {
        this.typePDO = typePDO;
    }

    public String getTypePDO() {
        return this.typePDO;
    }

    public void addRow(String row) {
        this.rows.add(row);
    }

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
