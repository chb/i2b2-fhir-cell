package org.bch.i2me2.core.service;

import org.bch.i2me2.core.config.AppConfig;
import org.junit.Test;

/**
 * Created by CH176656 on 3/20/2015.
 */


public class SurescriptsRefreshTest {

    @Test
    public void test1() throws Exception {
        System.out.println(AppConfig.getProp(AppConfig.EP_RXCONNECT));
        System.out.println(AppConfig.getProp(AppConfig.EP_IDM_RESOURCE));
        System.out.println(AppConfig.getProp(AppConfig.EP_IDM_ID));
        System.out.println(AppConfig.getProp(AppConfig.EP_I2B2_FR_SEND));
        System.out.println(AppConfig.getProp(AppConfig.EP_I2B2_FR_UPLOAD));
        System.out.println(AppConfig.getProp(AppConfig.NET_PROTOCOL_IDM));
        System.out.println(AppConfig.getProp(AppConfig.NET_PROTOCOL_RXCONNECT));
        System.out.println(AppConfig.getProp(AppConfig.NET_PROTOCOL_I2B2_FR));
        System.out.println(AppConfig.getProp(AppConfig.PORT_IDM));
        System.out.println(AppConfig.getProp(AppConfig.PORT_RXCONNECT));
        System.out.println(AppConfig.getProp(AppConfig.PORT_I2B2_FR));
        System.out.println(AppConfig.getProp(AppConfig.HOST_IDM));
        System.out.println(AppConfig.getProp(AppConfig.HOST_RXCONNECT));
        System.out.println(AppConfig.getProp(AppConfig.HOST_I2B2_FR));
    }
}
