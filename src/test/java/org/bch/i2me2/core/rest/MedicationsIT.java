package org.bch.i2me2.core.rest;

import org.apache.http.client.methods.HttpPost;
import org.bch.i2me2.core.external.I2B2QueryService;
import org.bch.i2me2.core.util.HttpRequest;
import org.bch.i2me2.core.util.Response;
import org.bch.i2me2.core.util.mapper.Mapper;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

/**
 * Created by CH176656 on 4/14/2015.
 */
@RunWith(Arquillian.class)
public class MedicationsIT extends AbstractRestIT {

    @Inject
    private HttpRequest httpRequest;

    @Test
    public void getMedicationsByPassIT() throws Exception {
        String patientId = "1234";
        String credentials = "MedRec2:MedRecApp1_";
        String encoding =  javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes("UTF-8"));
        String auth = "Basic " + encoding;
        String url = "http://127.0.0.1:8080/i2me2/rest/medications/getMedicationsByPass/"+patientId;
        Response resp = httpRequest.doPostGeneric(url, auth);
        assertEquals(200,resp.getResponseCode());
        String xml = resp.getContent();
        I2B2QueryService.QueryResponse respXml = new I2B2QueryService.QueryResponse(xml);
        NodeList list = respXml.getAllObservations();
        assertEquals(2, list.getLength());
    }

    @Test
    public void getMedicationsByPass_2IT() throws Exception {
        String patientId = "123456";
        String credentials = "MedRec2:MedRecApp1_";
        String encoding =  javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes("UTF-8"));
        String auth = "Basic " + encoding;
        String url = "http://127.0.0.1:8080/i2me2/rest/medications/getMedicationsByPass/"+patientId;
        Response resp = httpRequest.doPostGeneric(url, auth);
        assertEquals(200,resp.getResponseCode());
        String xml = resp.getContent();

        I2B2QueryService.QueryResponse respXml = new I2B2QueryService.QueryResponse(xml);
        NodeList list = respXml.getAllObservations();
        assertEquals(32, list.getLength());
    }
}
