package org.bch.i2me2.core.service;

import java.io.*;
import java.text.ParseException;

import org.bch.i2me2.core.exception.I2ME2Exception;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.Diff;

import static org.junit.Assert.*;

public class MapperRxToPDOTest {

    private static String XML_TEMPLATE_FILE = "xmlpdoTemplate.xml";

    private String subjectId ="1234";
    private String dob = "19451001";
    private String gender = "F";
    private String source = "BCH";
    private String sourceEvent = "SCR";
    private String validJSON = "{\"RxHistorySegments\":{\"orders\":[]}}";

    /**
     * Test base exception paths
     * @throws Exception
     */
    @Test
    public void getPDOXMLExceptionPathBase() throws Exception {
        MapperRxToPDO mapper = new MapperRxToPDO();

        // test no template file is found
        mapper.setXmlMapFileTemplate("no_file_test");
        try {
            mapper.getPDOXML("", subjectId, dob, gender, source, sourceEvent);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof IOException);
        }

        mapper.setXmlMapFileTemplate(XML_TEMPLATE_FILE);
        // Null
        try {
            mapper.getPDOXML(null, subjectId, dob, gender, source, sourceEvent);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof JSONException);
        }

        // Empty String
        try {
            mapper.getPDOXML("", subjectId, dob, gender, source, sourceEvent);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof JSONException);
        }

        // Malformed json
        try {
            mapper.getPDOXML("{\"a\":\"b}", subjectId, dob, gender, source, sourceEvent);
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
            mapper.getPDOXML(validJSON, null, dob, gender, source, sourceEvent);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }

        // Empty subjectId
        try {
            mapper.getPDOXML(validJSON, "", dob, gender, source, sourceEvent);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }

        // Empty subjectId
        try {
            mapper.getPDOXML(validJSON, "NotNum", dob, gender, source, sourceEvent);
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

        // No proper format
        try {
            mapper.getPDOXML(validJSON, subjectId, "1970pp22", gender, source, sourceEvent);
            fail();
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof ParseException);
        }

        // No proper format
        try {
            mapper.getPDOXML(validJSON, subjectId, "19701110qqwq", gender, source, sourceEvent);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }

        // No proper format
        try {
            mapper.getPDOXML(validJSON, subjectId, "1970-11-10qqwq", gender, source, sourceEvent);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }

        // Proper format but invalid data
        try {
            mapper.getPDOXML(validJSON, subjectId, "19703030", gender, source, sourceEvent);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }
    }

    /**
     * Test Exception paths related to gender
     * @throws Exception
     */
    @Test
    public void getPDOXMLExceptionPathGender() throws Exception {
        MapperRxToPDO mapper = new MapperRxToPDO();
        mapper.setXmlMapFileTemplate(XML_TEMPLATE_FILE);

        // Gender length is longer that 1
        try {
            mapper.getPDOXML(validJSON, subjectId, dob, "FF", source, sourceEvent);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }

        // Gender length is 1 but different from 'M' and 'F'
        try {
            mapper.getPDOXML(validJSON, subjectId, dob, "G", source, sourceEvent);
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
        MapperRxToPDO mapper = new MapperRxToPDO();
        mapper.setXmlMapFileTemplate(XML_TEMPLATE_FILE);

        // Null source
        try {
            mapper.getPDOXML(validJSON, subjectId, dob, gender, null, sourceEvent);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }

        // Empty source
        try {
            mapper.getPDOXML(validJSON, subjectId, dob, gender, "", sourceEvent);
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
        MapperRxToPDO mapper = new MapperRxToPDO();
        mapper.setXmlMapFileTemplate(XML_TEMPLATE_FILE);

        // Null source
        try {
            mapper.getPDOXML(validJSON, subjectId, dob, gender, source, null);
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }

        // Empty source
        try {
            mapper.getPDOXML(validJSON, subjectId, dob, gender, source, "");
            fail();
        } catch (I2ME2Exception e) {
            /* OK */
        }
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
        mapper.getPDOXML(sBuffer.toString(), subjectId, "19900101", gender, source, sourceEvent);

        // Accepting dob in format 1990-01-01
        mapper.getPDOXML(sBuffer.toString(), subjectId, "1990-01-01", gender, source, sourceEvent);

        // Accepting null dob
        mapper.getPDOXML(sBuffer.toString(), subjectId, null, gender, source, sourceEvent);

        // Accepting gender 'M', 'F' and null
        mapper.getPDOXML(sBuffer.toString(), subjectId, dob, "F", source, sourceEvent);
        mapper.getPDOXML(sBuffer.toString(), subjectId, dob, "M", source, sourceEvent);
        mapper.getPDOXML(sBuffer.toString(), subjectId, dob, "", source, sourceEvent);

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

    // test 0_0; No orders. testing optional patient data: No Gender
    @Test
    public void getPDOXMLDetail_0_0Test() throws Exception {
        String jsonFileName = "rxJSON0.json";
        String expectedXMLFileName = "rxXMLPDO0_0.xml";
        doTest(jsonFileName,expectedXMLFileName, subjectId, dob, null, source, sourceEvent);
    }

    // test 0_1; No orders. testing optional patient data: No DOB
    @Test
    public void getPDOXMLDetail_0_1Test() throws Exception {
        String jsonFileName = "rxJSON0.json";
        String expectedXMLFileName = "rxXMLPDO0_1.xml";
        doTest(jsonFileName,expectedXMLFileName, subjectId, null, gender, source, sourceEvent);
    }

    // test 0_2; No orders. testing optional patient data: No DOB and No gender
    @Test
    public void getPDOXMLDetail_0_2Test() throws Exception {
        String jsonFileName = "rxJSON0.json";
        String expectedXMLFileName = "rxXMLPDO0_2.xml";
        doTest(jsonFileName,expectedXMLFileName, subjectId, null, null, source, sourceEvent);
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

    // test 6: testing all modifiers as fill
    @Test
    public void getPDOXMLDetail_6Test() throws Exception {
        String jsonFileName = "rxJSON6.json";
        String expectedXMLFileName = "rxXMLPDO6.xml";
        doTest(jsonFileName, expectedXMLFileName);
    }

    // test 7: testing all modifiers as claim
    @Test
    public void getPDOXMLDetail_7Test() throws Exception {
        String jsonFileName = "rxJSON7.json";
        String expectedXMLFileName = "rxXMLPDO7.xml";
        doTest(jsonFileName, expectedXMLFileName);
    }

    // test 8: testing all modifiers. Two orders, one claim and one fill
    @Test
    public void getPDOXMLDetail_8Test() throws Exception {
        String jsonFileName = "rxJSON8.json";
        String expectedXMLFileName = "rxXMLPDO8.xml";
        doTest(jsonFileName, expectedXMLFileName);
    }

    // test 9: testing NDC code is not present
    @Test
    public void getPDOXMLDetail_9Test() throws Exception {
        String jsonFileName = "rxJSON9.json";
        String expectedXMLFileName = "rxXMLPDO9.xml";
        doTest(jsonFileName, expectedXMLFileName);
    }
    
    // test 10: testing NDC code is present but is empty
    @Test
    public void getPDOXMLDetail_10Test() throws Exception {
        String jsonFileName = "rxJSON10.json";
        String expectedXMLFileName = "rxXMLPDO10.xml";
        doTest(jsonFileName, expectedXMLFileName);
    }
    
    // Just to generate a xmlpdo from old mapping
    @Ignore
    public void currentMapping() throws Exception {
        String jsonInput = readTextFile("rxJSON7.json");
        MapperRxToPDO mapper = new MapperRxToPDO();
        mapper.setXmlMapFileTemplate("xmlpdoTemplate_CurrentMapping.xml");
        String xmlResult = mapper.getPDOXML(jsonInput,subjectId, dob, gender, source, source);
        BufferedWriter bw = new BufferedWriter( new FileWriter( "output.xml"));
        bw.write(xmlResult);
        bw.close();
    }


    private void doTest(String jsonFile, String expectedXMLFile) throws Exception {
        doTest(jsonFile, expectedXMLFile, subjectId, dob, gender, source, sourceEvent);
    }

    private void doTest(String jsonFile, String expectedXMLFile, String subjectId, String dob, String gender,
                        String source, String sourceEvent) throws Exception {
        String jsonInput = readTextFile(jsonFile);
        String xmlExpected = readTextFile(expectedXMLFile);

        MapperRxToPDO mapper = new MapperRxToPDO();
        mapper.setXmlMapFileTemplate(XML_TEMPLATE_FILE);
        String xmlResult = mapper.getPDOXML(jsonInput,subjectId, dob, gender, source, sourceEvent);

        // We place replace internal modifier_cd with the real ones
        xmlExpected = mapper.placeRealModifiersCodes(xmlExpected);
        if (jsonFile.equals("rxJSON0.json")) System.out.println(xmlResult);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);

        Diff diff = new Diff(xmlExpected, xmlResult);
        // We override the ElementQualifier so, order of elements does not matter in the comparison
        //diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        //if (jsonFile.equals("rxJSON10.json")) System.out.println(diff.toString());
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
