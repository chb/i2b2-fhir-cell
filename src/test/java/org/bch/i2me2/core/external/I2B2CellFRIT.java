package org.bch.i2me2.core.external;

import org.bch.i2me2.core.service.MapperRxToPDO;
import org.bch.i2me2.core.util.Utils;
import org.bch.i2me2.core.util.mapper.Mapper;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by CH176656 on 3/31/2015.
 */
public class I2B2CellFRIT {
    private I2B2CellFR i2b2Cell = new I2B2CellFR();
    private MapperRxToPDO mapper = new MapperRxToPDO();

    @Test
    public void pushPDOTest0_IT() throws Exception {
        StringBuffer sb = new StringBuffer();
        Utils.textFileToStringBuffer(I2B2CellFRIT.class, "XMLPDO_0_IT.xml", sb, "\n");
        I2B2CellFR.UploadI2B2Response resp = i2b2Cell.pushPDOXML(sb.toString());
        assertEquals(1, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_PIDS));
        assertEquals(1, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_EIDS));
        assertEquals(1, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_PATIENTS));
        assertEquals(1, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_EVENTS));
        assertEquals(2, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_OBSERVATIONS));
    }

    @Test
    public void pushPDOTest1_IT() throws Exception {
        StringBuffer sb = new StringBuffer();
        Utils.textFileToStringBuffer(I2B2CellFRIT.class, "XMLPDO_1_IT.xml", sb, "\n");
        I2B2CellFR.UploadI2B2Response resp = i2b2Cell.pushPDOXML(sb.toString());
        assertEquals(1, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_PIDS));
        assertEquals(1, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_EIDS));
        assertEquals(1, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_PATIENTS));
        assertEquals(1, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_EVENTS));
        assertEquals(32, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_OBSERVATIONS));
    }

    @Test
    public void pushJSONToPDOTest_IT() throws Exception {
        StringBuffer sb = new StringBuffer();
        Utils.textFileToStringBuffer(I2B2CellFRIT.class, "rxJSON.json", sb, "\n");
        String xml = mapper.getPDOXML(sb.toString(),"565656","20000505","F","BCH", "SCR");
        I2B2CellFR.UploadI2B2Response resp = i2b2Cell.pushPDOXML(xml);
        assertEquals(1, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_PIDS));
        assertEquals(1, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_EIDS));
        assertEquals(1, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_PATIENTS));
        assertEquals(1, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_EVENTS));
        assertEquals(32, resp.getTotalRecords(Mapper.XmlPdoTag.TAG_OBSERVATIONS));
    }
}
