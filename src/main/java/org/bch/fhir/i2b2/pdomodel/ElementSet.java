package org.bch.fhir.i2b2.pdomodel;

import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ipinyol on 7/9/15.
 */
public class ElementSet {
    public static String PDO_PATIENT_SET = "patient_set";
    public static String PDO_PID_SET = "pid_set";
    public static String PDO_EID_SET = "eid_set";
    public static String PDO_EVENT_SET = "event_set";
    public static String PDO_OBSERVATION_SET = "observation_set";

    private String typePDOSet=null;
    private List<Element> elements = new ArrayList<>();

    public void setTypePDOSet(String typePDOSet) {
        this.typePDOSet = typePDOSet;
    }

    public String getTypePDOSet() {
        return this.typePDOSet;
    }

    public void addElement(Element element) {
        this.elements.add(element);
    }

    public void removeElement(Element element) {
        this.elements.remove(element);
    }

    public String toPDOString() throws FHIRI2B2Exception {
        if (this.typePDOSet == null) throw new FHIRI2B2Exception("typePDOSet in ElementSet object has not been " +
                "initialized");
        StringBuffer out = new StringBuffer();
        out.append("<").append(this.getTypePDOSet()).append(">\n");
        for(Element e: this.elements) {
            out.append(e.toPDOString());
        }
        out.append("</").append(this.getTypePDOSet()).append(">\n");
        return out.toString();
    }

}
