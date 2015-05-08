package org.bch.i2me2.core.service;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by CH176656 on 4/13/2015.
 */
public class MapperFHIRToPDOTest {
    private static String subjectId ="1234";
    private static String gender = "F";
    private static String source = "BCH";
    private static String sourceEvent = "SCR";
    private static String dateTimeStr = "2015-02-12T12:00:00.00";
    private static Date date = new Date();
    private static String validJSON = "{}";
    /**
     * Test Exception paths related to subject id
     * @throws Exception
     */
    @Test
    public void getPDOXMLExceptionPathSubjectId() throws Exception {
        MapperFHIRToPDO mapper = new MapperFHIRToPDO();

        // Null subjectId
        try {
            mapper.getPDOXML(validJSON, null, gender, source, sourceEvent, date);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }

        // Empty subjectId
        try {
            mapper.getPDOXML(validJSON, "", gender, source, sourceEvent, date);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }

        // Not numeric subjectId
        try {
            mapper.getPDOXML(validJSON, "NotNum", gender, source, sourceEvent, date);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof NumberFormatException);
        }
    }
    /**
     * Test Exception paths related to gender
     * @throws Exception
     */
    @Test
    public void getPDOXMLExceptionPathGender() throws Exception {
        MapperFHIRToPDO mapper = new MapperFHIRToPDO();

        // Gender length is longer that 1
        try {
            mapper.getPDOXML(validJSON, subjectId, "FF", source, sourceEvent, date);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }

        // Gender length is 1 but different from 'M' and 'F'
        try {
            mapper.getPDOXML(validJSON, subjectId, "G", source, sourceEvent, date );
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }
    }

    /**
     * Test Exception paths related to source
     * @throws Exception
     */
    @Test
    public void getPDOXMLExceptionPathSource() throws Exception {
        MapperFHIRToPDO mapper = new MapperFHIRToPDO();

        // Null source
        try {
            mapper.getPDOXML(validJSON, subjectId, gender, null, sourceEvent, date);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }

        // Empty source
        try {
            mapper.getPDOXML(validJSON, subjectId, gender, "", sourceEvent, date);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }
    }

    /**
     * Test Exception paths related to source Event
     * @throws Exception
     */
    @Test
    public void getPDOXMLExceptionPathSourceEvent() throws Exception {
        MapperFHIRToPDO mapper = new MapperFHIRToPDO();

        // Null source
        try {
            mapper.getPDOXML(validJSON, subjectId, gender, source, null, date);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }

        // Empty source
        try {
            mapper.getPDOXML(validJSON, subjectId, gender, source, "", date);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }
    }

    @Test
    public void getPDOXML_NewModel_Test() throws Exception {
        String jsonFileName = "mrNewJSON0.json";
        String expectedXMLFileName = "mrNewXMLPDO0.xml";
        doTest(jsonFileName,expectedXMLFileName, subjectId, gender, source, sourceEvent, dateTimeStr);

        /*
        String jsonInput = readTextFile("mrNewJSON0.json");
        MapperFHIRToPDO mapper = new MapperFHIRToPDO();
        String outputDataFormat = AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2);
        SimpleDateFormat dateFormat = new SimpleDateFormat(outputDataFormat);
        Date date = dateFormat.parse(dateTimeStr);
        String map = mapper.getPDOXML(jsonInput,subjectId, gender, source, sourceEvent, date);
        PrintWriter out = new PrintWriter("output.xml");
        out.print(map);
        out.close();
        */

    }

    // test 0: Happy path with only onw medication
    @Ignore
    public void getPDOXMLDetail_0Test() throws Exception {
        String jsonFileName = "mrJSON0.json";
        String expectedXMLFileName = "mrXMLPDO0.xml";
        doTest(jsonFileName,expectedXMLFileName, subjectId, gender, source, sourceEvent, dateTimeStr);
    }

    // test 1: Happy with no medications
    @Ignore
    public void getPDOXMLDetail_1Test() throws Exception {
        String jsonFileName = "mrJSON1.json";
        String expectedXMLFileName = "mrXMLPDO1.xml";
        doTest(jsonFileName,expectedXMLFileName, subjectId, gender, source, sourceEvent, dateTimeStr);
    }

    // test 2: Happy with two medications
    @Ignore
    public void getPDOXMLDetail_2Test() throws Exception {
        String jsonFileName = "mrJSON2.json";
        String expectedXMLFileName = "mrXMLPDO2.xml";
        doTest(jsonFileName,expectedXMLFileName, subjectId, gender, source, sourceEvent, dateTimeStr);
    }

    private void doTest(String jsonFile, String expectedXMLFile, String subjectId, String gender,
                        String source, String sourceEvent, String dateTimeStr) throws Exception {
        String jsonInput = readTextFile(jsonFile);
        String xmlExpected = readTextFile(expectedXMLFile);

        MapperFHIRToPDO mapper = new MapperFHIRToPDO();
        String outputDataFormat = AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2);
        SimpleDateFormat dateFormat = new SimpleDateFormat(outputDataFormat);
        Date date = dateFormat.parse(dateTimeStr);

        String xmlResult = mapper.getPDOXML(jsonInput,subjectId, gender, source, sourceEvent,date);

        // We replace internal modifier_cd with the real ones
        xmlExpected = mapper.placeRealModifiersCodes(xmlExpected);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        Diff diff = new Diff(xmlExpected, xmlResult);
        // We override the ElementQualifier so, order of elements does not matter in the comparison
        //diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        //if (jsonFile.equals("mrJSON2.json")) System.out.println(diff.toString());
        assertTrue(diff.similar());
    }

    private String readTextFile(String fileName) throws Exception {
        InputStream in = MapperRxToPDOTest.class.getResourceAsStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sBuffer = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sBuffer.append(line).append('\n');
            }
        } catch(Exception e) {
            e.printStackTrace();

        } finally {
            in.close();
        }
        return sBuffer.toString();
    }
}
