package org.bch.i2me2.core.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;

import org.bch.i2me2.core.exception.I2ME2Exception;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.Diff;

import static org.junit.Assert.*;

public class MapperRxToPDOTest {

    /**
     * Test base exception paths
     * @throws Exception
     */
    private static String XML_TEMPLATE_FILE = "xmlpdoTemplate.xml";

    private String subjectId ="1234";
    private String zip = "12345";
    private String dob = "19451001";
    private String gender = "F";
    private String source = "BCH";
    private String validJSON = "{\"RxHistorySegments\":{\"orders\":[]}}";

    @Test
    public void getPDOXMLExceptionPathBase() throws Exception {
        MapperRxToPDO mapper = new MapperRxToPDO();

        // test no template file is found
        mapper.setXmlMapFileTemplate("no_file_test");
        try {
            mapper.getPDOXML("", subjectId, zip, dob, gender, source);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof IOException);
        }

        mapper.setXmlMapFileTemplate(XML_TEMPLATE_FILE);
        // Null
        try {
            mapper.getPDOXML(null, subjectId, zip, dob, gender, source);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof JSONException);
        }

        // Empty String
        try {
            mapper.getPDOXML("", subjectId, zip, dob, gender, source);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof JSONException);
        }

        // Malformed json
        try {
            mapper.getPDOXML("{\"a\":\"b}", subjectId, zip, dob, gender, source);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof JSONException);
        }
    }

    /**
     * Test Exception paths related to subject id
     * @throws Exception
     */
    @Test
    public void getPDOXMLExceptionPathSubjectId() throws Exception {
        MapperRxToPDO mapper = new MapperRxToPDO();
        mapper.setXmlMapFileTemplate(XML_TEMPLATE_FILE);

        // Null subjectId
        try {
            mapper.getPDOXML(validJSON, null, zip, dob, gender, source);
            fail();
        } catch (I2ME2Exception e) {}

        // Empty subjectId
        try {
            mapper.getPDOXML(validJSON, "", zip, dob, gender, source);
            fail();
        } catch (I2ME2Exception e) {}

        // Empty subjectId
        try {
            mapper.getPDOXML(validJSON, "NotNum", zip, dob, gender, source);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof NumberFormatException);
        }
    }

    /**
     * Test Exception paths related to zip code
     * @throws Exception
     */
    @Test
    public void getPDOXMLExceptionPathZip() throws Exception {
        MapperRxToPDO mapper = new MapperRxToPDO();
        mapper.setXmlMapFileTemplate(XML_TEMPLATE_FILE);

        // Null zip code
        try {
            mapper.getPDOXML(validJSON, subjectId, null, dob, gender, source);
            fail();
        } catch (I2ME2Exception e) {}

        // length < 5
        try {
            mapper.getPDOXML(validJSON, subjectId, "2222", dob, gender, source);
            fail();
        } catch (I2ME2Exception e) {}

        // length >= 5 nut not numeric
        try {
            mapper.getPDOXML(validJSON, subjectId, "2222a", dob, gender, source);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof NumberFormatException);
        }
    }

    /**
     * Test Exception paths related to dob
     * @throws Exception
     */
    @Test
    public void getPDOXMLExceptionPathDOB() throws Exception {
        MapperRxToPDO mapper = new MapperRxToPDO();
        mapper.setXmlMapFileTemplate(XML_TEMPLATE_FILE);

        // Null DOB
        try {
            mapper.getPDOXML(validJSON, subjectId, zip, null, gender, source);
        } catch (I2ME2Exception e) {}

        // No proper format
        try {
            mapper.getPDOXML(validJSON, subjectId, zip, "1970pp22", gender, source);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof ParseException);
        }

        // No proper format
        try {
            mapper.getPDOXML(validJSON, subjectId, zip, "19701110qqwq", gender, source);
            fail();
        } catch (I2ME2Exception e) {}

        // No proper format
        try {
            mapper.getPDOXML(validJSON, subjectId, zip, "1970-11-10qqwq", gender, source);
            fail();
        } catch (I2ME2Exception e) {}

        // Proper format but invalid data
        try {
            mapper.getPDOXML(validJSON, subjectId, zip, "19703030", gender, source);
            fail();
        } catch (I2ME2Exception e) {}
    }

    /**
     * Test Exception paths related to zip code
     * @throws Exception
     */
    @Test
    public void getPDOXMLExceptionPathGender() throws Exception {
        MapperRxToPDO mapper = new MapperRxToPDO();
        mapper.setXmlMapFileTemplate(XML_TEMPLATE_FILE);

        // Null gender
        try {
            mapper.getPDOXML(validJSON, subjectId, zip, dob, null, source);
            fail();
        } catch (I2ME2Exception e) {}

        // Gender length is longer that 1
        try {
            mapper.getPDOXML(validJSON, subjectId, zip, dob, "FF", source);
            fail();
        } catch (I2ME2Exception e) {}

        // Gender length is 1 but different from 'M' and 'F'
        try {
            mapper.getPDOXML(validJSON, subjectId, zip, dob, "G", source);
            fail();
        } catch (I2ME2Exception e) {}

    }
    // Happy path formatting check
    @Test
    public void getPDOXMLHappyPathFormattingCheckTest() throws Exception {
        MapperRxToPDO mapper = new MapperRxToPDO();
        mapper.setXmlMapFileTemplate(XML_TEMPLATE_FILE);
        InputStream in = MapperRxToPDOTest.class.getResourceAsStream("jsonRXExample.json");
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

        // Accepting dob in format 19900101
        mapper.getPDOXML(sBuffer.toString(), subjectId, zip, "19900101", gender, source);

        // Accepting dob in format 1990-01-01
        mapper.getPDOXML(sBuffer.toString(), subjectId, zip, "1990-01-01", gender, source);

        // Accepting gender 'M', 'F' and ''
        mapper.getPDOXML(sBuffer.toString(), subjectId, zip, dob, "F", source);
        mapper.getPDOXML(sBuffer.toString(), subjectId, zip, dob, "M", source);
        mapper.getPDOXML(sBuffer.toString(), subjectId, zip, dob, "", source);

        //System.out.println(mapper.getPDOXML(sBuffer.toString(), subjectId, zip, dob, gender, source));
        //mapper.getPDOXML(sBuffer.toString(), subjectId, zip, dob, gender, source);
        //mapper.getPDOXML(validJSON, subjectId, zip, dob, "M", source));
    }

    // test 0; No orders with field orders present
    @Test
    public void getPDOXMLDetail_0Test() throws Exception {
        String jsonFileName = "rxJSON0.json";
        String expectedXMLFileName = "rxXMLPDO0.xml";
        doTest(jsonFileName,expectedXMLFileName);
    }

    // test 1: No orders with no field orders present
    @Test
    public void getPDOXMLDetail_1Test() throws Exception {
        String jsonFileName = "rxJSON1.json";
        String expectedXMLFileName = "rxXMLPDO1.xml";
        doTest(jsonFileName,expectedXMLFileName);
    }

    // test 2: Two orders with NDC codes and duration
    @Test
    public void getPDOXMLDetail_2Test() throws Exception {
        String jsonFileName = "rxJSON2.json";
        String expectedXMLFileName = "rxXMLPDO2.xml";
        doTest(jsonFileName, expectedXMLFileName);
    }

    // test 3: Testing claims vf fills basic. Here, we have a claim, so enteringOrganizationAlternativeId = "Claim'
    @Test
    public void getPDOXMLDetail_3Test() throws Exception {
        String jsonFileName = "rxJSON3.json";
        String expectedXMLFileName = "rxXMLPDO3.xml";
        doTest(jsonFileName, expectedXMLFileName);
    }

    // test 4: Testing claims vf fills basic. Same as 3Test but enteringOrganizationAlternativeId = "Fill'
    @Test
    public void getPDOXMLDetail_4Test() throws Exception {
        String jsonFileName = "rxJSON4.json";
        String expectedXMLFileName = "rxXMLPDO4.xml";
        doTest(jsonFileName, expectedXMLFileName);
    }

    // test 5: Testing claims vf fills basic. Same as 4Test where enteringOrganizationAlternativeId is not present.
    // It should act as Fill
    @Test
    public void getPDOXMLDetail_5Test() throws Exception {
        String jsonFileName = "rxJSON5.json";
        String expectedXMLFileName = "rxXMLPDO5.xml";
        doTest(jsonFileName, expectedXMLFileName);
    }

    private void doTest(String jsonFile, String expectedXMLFile) throws Exception {
        String jsonInput = readTextFile(jsonFile);
        String xmlExpected = readTextFile(expectedXMLFile);

        MapperRxToPDO mapper = new MapperRxToPDO();
        mapper.setXmlMapFileTemplate(XML_TEMPLATE_FILE);

        String xmlResult = mapper.getPDOXML(jsonInput,subjectId,zip,dob,gender,source);
        //if (jsonFile.equals("rxJSON4.json")) System.out.println(xmlResult);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);

        Diff diff = new Diff(xmlExpected, xmlResult);
        // We override the ElementQualifier so, order of elements does not matter in the comparison
        //diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        //if (jsonFile.equals("rxJSON4.json")) System.out.println(diff.toString());
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
