package org.bch.i2me2.core.service;

import org.bch.i2me2.core.util.mapper.Mapper;

import javax.ejb.Stateless;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by CH176656 on 4/13/2015.
 */
@Stateless
public abstract class MapperI2ME2 extends Mapper {

    // The map between internal Modifier_CDs and real ones. Found in modifierCodes.i2me2 file
    // Each line: Internal_Modifier_CD,Real_Modifier_CD
    protected HashMap<String, String> realModifiersCD;

    // File name containing the list of real modifier codes
    protected static final String REAL_MODIFIERS_FILE = "modifierCodes.i2me2";

    // The concept_cd code when no NDC code is found
    protected final String NO_CONCEPT_CD = "NO_NDC_CODE";

    @Override
    public String placeRealModifiersCodes(String text) {
        if (this.realModifiersCD==null) {
            return text;
        }
        String out = text;
        Set<String> keys = this.realModifiersCD.keySet();
        for(String key: keys) {
            String value = this.realModifiersCD.get(key);
            String currentFullLine = buildModifierLine(key);
            String realFullLine = buildModifierLine(value);
            out = out.replaceAll(currentFullLine, realFullLine);
        }
        return out;
    }

    protected String buildModifierLine(String modifierCD) {
        String out = "<" + XmlPdoObservationTag.TAG_MODIFIER_CD.toString()+">";
        out = out + modifierCD + "</" + XmlPdoObservationTag.TAG_MODIFIER_CD.toString()+">";
        return out;
    }

    protected void loadRealModifiers() throws Exception {
        try {
            String realModifiers = readTextFile(REAL_MODIFIERS_FILE, ",");
            String [] modifiers = realModifiers.split(",");
            this.realModifiersCD = new HashMap<>();
            for (String modifier: modifiers){
                String [] codes = modifier.split(":");
                this.realModifiersCD.put(codes[0].trim(), codes[1].trim());
            }
        } catch (Exception e) {
            //this.log(Level.SEVERE, MODULE+OP_LOAD_MODIFIERS+"Error loading real modifiers. Error message:"
            //        + e.getMessage());
            throw e;
        }
    }

    protected String readTextFile(String fileName, String sep) throws Exception {
        InputStream in = MapperRxToPDO.class.getResourceAsStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sBuffer = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sBuffer.append(line).append(sep);
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw e;

        } finally {
            in.close();
        }
        return sBuffer.toString();
    }

    protected String formatKeyValueJSON(String key, String value, boolean isText) {
        if (isText) {
            return "\"" + key + "\" : \"" + value + "\"";
        } else {
            return "\"" + key + "\" : " + value + "";
        }
    }

    protected String placeEmptyConceptCD(String elem) {
        String newElem = elem;
        String conceptCD = this.getTagValueLine(newElem, XmlPdoObservationTag.TAG_CONCEPT_CD.toString());
        String newConceptCD = "<" + XmlPdoObservationTag.TAG_CONCEPT_CD.toString() + ">";
        newConceptCD = newConceptCD + NO_CONCEPT_CD;
        newConceptCD = newConceptCD + "</" + XmlPdoObservationTag.TAG_CONCEPT_CD.toString() + ">";
        newElem = newElem.replaceAll(conceptCD, newConceptCD);
        return newElem;
    }

    protected String getModifierCode(String xmlElem) {
        String modifier_cd_line = this.getTagValueLine(xmlElem, XmlPdoObservationTag.TAG_MODIFIER_CD.toString());
        String modifier_cd = modifier_cd_line.replace("<"+XmlPdoObservationTag.TAG_MODIFIER_CD.toString()+">","");
        modifier_cd = modifier_cd.replace("</"+XmlPdoObservationTag.TAG_MODIFIER_CD.toString()+">","");
        return modifier_cd;
    }

}
