package org.bch.i2me2.core.rest;

import org.apache.http.client.methods.HttpPost;
import org.bch.i2me2.core.util.HttpRequest;
import org.bch.i2me2.core.util.Response;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

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
        String credentials = "MedRec2:MedRecApp1_";
        String encoding =  javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes("UTF-8"));
        String auth = "Basic " + encoding;
        String url = "http://127.0.0.1:8080/i2me2/rest/medications/getMedicationsByPass/1234";
        Response resp = httpRequest.doPostGeneric(url, auth);
        assertEquals(200,resp.getResponseCode());
        System.out.println(resp.getContent());
    }
}
