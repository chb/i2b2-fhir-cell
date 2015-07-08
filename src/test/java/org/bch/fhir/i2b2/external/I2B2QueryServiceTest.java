package org.bch.fhir.i2b2.external;

import org.bch.fhir.i2b2.util.mapper.Mapper;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.Date;
import static org.junit.Assert.*;
/**
 * Created by CH176656 on 4/10/2015.
 */
public class I2B2QueryServiceTest {

    private static String xmlTest ="" +
            "<observation_set>\n" +
            "        <observation>\n" +
            "            <event_id source=\"SCR\">050045624</event_id>\n" +
            "            <patient_id source=\"BCH\">1234</patient_id>\n" +
            "            <start_date>2015-02-12T12:00:00.00</start_date>\n" +
            "        </observation>\n" +
            "        <observation>\n" +
            "            <event_id source=\"SCR\">050045624</event_id>\n" +
            "            <patient_id source=\"BCH\">1234</patient_id>\n" +
            "            <start_date>2015-02-12T12:00:00.00</start_date>\n" +
            "        </observation>\n" +
            "        <observation>\n" +
            "            <event_id source=\"SCR\">050045624</event_id>\n" +
            "            <patient_id source=\"BCH\">1234</patient_id>\n" +
            "            <start_date>2014-02-20T10:11:12.00</start_date>\n" +
            "        </observation>\n" +
            "        <observation>\n" +
            "            <event_id source=\"SCR\">050045624</event_id>\n" +
            "            <patient_id source=\"BCH\">123456</patient_id>\n" +
            "            <start_date>2013-02-20T10:11:12.00</start_date>\n" +
            "        </observation>\n" +
            "</observation_set>";

    @Test
    public void getObservationsByStartDate_baseCase_Test() throws Exception {

        // Test 0: with null data returns an empty set of observations
        I2B2QueryService.QueryResponse resp = new I2B2QueryService.QueryResponse(xmlTest);
        Document doc = resp.getObservationsByStartDate(null);
        NodeList list = doc.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
        assertEquals(0, list.getLength());

        // Test 1: with null xml throws Exception
        try {
            resp = new I2B2QueryService.QueryResponse(null);
            fail();
        } catch (Exception e) {
            // Totally fine
        }
    }

    @Test
    public void getObservationsByValue_baseCase_Test() throws Exception {

        // Test 0: with null data returns an empty set of observations
        I2B2QueryService.QueryResponse resp = new I2B2QueryService.QueryResponse(xmlTest);

        Document doc = resp.getObservationsByValue(null, null);
        NodeList list = doc.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
        assertEquals(0, list.getLength());

        // Test 1: With null one of the parameters still we get empty lists
        resp.getObservationsByValue(null, "1234");
        list = doc.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
        assertEquals(0, list.getLength());

        resp.getObservationsByValue("patient_id", null);
        list = doc.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
        assertEquals(0, list.getLength());


    }

    /**
     * Test QueryResponse filtering functionality
     * @throws Exception
     */
    @Test
    public void getObservationsByStartDate_Test() throws Exception {
        String date2015 = "2015-02-12T12:00:00.00";

        I2B2QueryService.QueryResponse resp = new I2B2QueryService.QueryResponse(xmlTest);

        // Test 0: Return empty set of observations
        Document doc = resp.getObservationsByStartDate(new Date());
        NodeList list = doc.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
        assertEquals(0, list.getLength());

        // Test1: Return all set of observations
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat("yyyy-MM-dd");
        Date date = dateFormatOutput.parse("2000-01-01");
        doc = resp.getObservationsByStartDate(date);
        list = doc.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
        assertEquals(4, list.getLength());

        // Test2: Return only two observations
        date = dateFormatOutput.parse("2015-01-01");
        doc = resp.getObservationsByStartDate(date);
        list = doc.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
        assertEquals(2, list.getLength());
            // --> we make sure that are the ones we are expecting
        Element element0 = (Element)list.item(0);
        element0.getElementsByTagName("start_date");
        Element element1 = (Element)list.item(1);
        element0.getElementsByTagName("start_date");
        assertEquals(date2015, element0.getElementsByTagName("start_date").item(0).getTextContent());
        assertEquals(date2015, element1.getElementsByTagName("start_date").item(0).getTextContent());
    }

    @Test
    public void getObservationsByValue_Test() throws Exception {
        I2B2QueryService.QueryResponse resp = new I2B2QueryService.QueryResponse(xmlTest);

        // test 0: Return empty set of observations because tag is not present
        Document doc = resp.getObservationsByValue("NO TAG", "1234");
        NodeList list = doc.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
        assertEquals(0, list.getLength());

        // test 1: Return empty set of observations because value is not the case
        doc = resp.getObservationsByValue("patient_id", "no id");
        list = doc.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
        assertEquals(0, list.getLength());

        // test 2: Return 1 observation
        doc = resp.getObservationsByValue("patient_id", "123456");
        list = doc.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
        assertEquals(1, list.getLength());
            // --> We make sure that it is the one
        Element element0 = (Element)list.item(0);
        element0.getElementsByTagName("patient_id");
        assertEquals("123456", element0.getElementsByTagName("patient_id").item(0).getTextContent());

        // test 3: Return 3 observations
        doc = resp.getObservationsByValue("patient_id", "1234");
        list = doc.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
        assertEquals(3, list.getLength());
            // --> We make sure that it is the one
        element0 = (Element)list.item(0);
        Element element1 = (Element)list.item(1);
        Element element2 = (Element)list.item(2);
        element0.getElementsByTagName("patient_id");
        assertEquals("1234", element0.getElementsByTagName("patient_id").item(0).getTextContent());
        assertEquals("1234", element1.getElementsByTagName("patient_id").item(0).getTextContent());
        assertEquals("1234", element2.getElementsByTagName("patient_id").item(0).getTextContent());
    }
}
