package org.bch.i2me2.core.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bch.i2me2.core.exception.I2ME2Exception;
import org.json.JSONException;
import org.junit.Test;
import static org.junit.Assert.*;

public class MapperRxToPDOTest {

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
            mapper.getPDOXML("");
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof IOException);
        }

        mapper.setXmlMapFileTemplate("xmlpdoTemplate.xml");
        // Null
        try {
            mapper.getPDOXML(null);
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof JSONException);
        }

        // Empty String
        try {
            mapper.getPDOXML("");
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof JSONException);
        }

        // Malformed json
        try {
            mapper.getPDOXML("{\"a\":\"b}");
        } catch (I2ME2Exception e) {
            assertTrue(e.getInnerException() instanceof JSONException);
        }
    }

    /**
     * Test Exception paths
     * @throws Exception
     */
    @Test
    public void getPDOXMLExceptionPath() throws Exception {
        MapperRxToPDO mapper = new MapperRxToPDO();
        mapper.setXmlMapFileTemplate("xmlpdoTemplate.xml");

        //TODO


    }

    @Test
    public void mainTest() throws Exception {
        MapperRxToPDO mapper = new MapperRxToPDO();
        mapper.setXmlMapFileTemplate("xmlpdoTemplate.xml");
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
        System.out.println(mapper.getPDOXML(sBuffer.toString()));
    }

}
