package org.bch.fhir.i2b2.pdomodel;

import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Models element sets of a pdo xml {patient_set, pid_set, eid_set, event_set, observation_set}
 * @author CHIP-IHL
 */
public class ElementSet {
    public static String PDO_PATIENT_SET = "patient_set";
    public static String PDO_PID_SET = "pid_set";
    public static String PDO_EID_SET = "eid_set";
    public static String PDO_EVENT_SET = "event_set";
    public static String PDO_OBSERVATION_SET = "observation_set";

    private String typePDOSet=null;
    private List<Element> elements = new ArrayList<>();

    /**
     * Sets the pdo set type
     * {@link ElementSet#PDO_OBSERVATION_SET}
     * {@link ElementSet#PDO_EID_SET}
     * {@link ElementSet#PDO_EVENT_SET}
     * {@link ElementSet#PDO_PID_SET}
     * {@link ElementSet#PDO_PATIENT_SET}
     * @param typePDOSet
     */
    public void setTypePDOSet(String typePDOSet) {
        this.typePDOSet = typePDOSet;
    }

    /**
     * Gets the pdo set element type
     * @return The type. See {@link ElementSet#setTypePDOSet(String)}
     */
    public String getTypePDOSet() {
        return this.typePDOSet;
    }

    /**
     * Add an {@link Element} object into the set
     * @param element The element
     */
    public void addElement(Element element) {
        this.elements.add(element);
    }

    /**
     * Removes an {@link Element} object from the set
     * @param element The element
     */
    public void removeElement(Element element) {
        this.elements.remove(element);
    }

    /**
     * Returns the element set as String
     * @return  The String
     * @throws FHIRI2B2Exception In case type pdo set has not been initialized
     */
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
