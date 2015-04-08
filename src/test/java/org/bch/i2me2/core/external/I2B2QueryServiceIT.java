package org.bch.i2me2.core.external;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by CH176656 on 4/8/2015.
 */
public class I2B2QueryServiceIT {
    private I2B2QueryService i2b2Cell = new I2B2QueryService();

    @Test
    public void test_HappyPath() throws Exception {
        String patiendId ="1234";
        String source = "BCH";
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
        Date date = format.parse("2015-02-20");
        I2B2QueryService.QueryResponse resp = i2b2Cell.getPatientData(patiendId,source,date);
    }
}
