package org.bch.i2me2.core.external;

import org.bch.i2me2.core.util.Utils;
import org.junit.Test;

/**
 * Created by CH176656 on 3/31/2015.
 */
public class I2B2CellFRIT {
    private I2B2CellFR i2b2Cell = new I2B2CellFR();

    @Test
    public void pushPDOTest1_IT() throws Exception {
        StringBuffer sb = new StringBuffer();
        Utils.textFileToStringBuffer(I2B2CellFRIT.class, "XMLPDO_0_IT.xml", sb, "\n");
        i2b2Cell.pushPDOXML(sb.toString());
    }
}
