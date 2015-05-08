package org.bch.i2me2.core.external;

import org.junit.Test;
import org.w3c.dom.NodeList;

import static org.junit.Assert.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by CH176656 on 4/8/2015.
 */
public class I2B2QueryServiceIT {
    private I2B2QueryService i2b2Cell = new I2B2QueryService();

    // It requires patient 123456 up in the i2b2 instance with exactly 32 facts
    @Test
    public void test_HappyPath() throws Exception {
        String patiendId ="123456";
        String source = "BCH";
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
        Date date = format.parse("2015-02-20");
        I2B2QueryService.QueryResponse resp = i2b2Cell.getPatientData(patiendId,source,date);
        NodeList l = resp.getAllObservations();
        assertEquals(32,l.getLength());
    }
}
