package org.bch.i2me2.core.service;

import org.bch.i2me2.core.config.AppConfig;
import org.junit.Test;

/**
 * Created by CH176656 on 3/20/2015.
 */


public class SurescriptsRefreshTest {

    @Test
    public void test1() throws Exception {
        System.out.println(AppConfig.getProp(AppConfig.URL_RXCONNECT));
        System.out.println(AppConfig.getProp(AppConfig.URL_IDM_RESOURCE));
        System.out.println(AppConfig.getProp(AppConfig.URL_IDM_ID));
        System.out.println(AppConfig.getProp(AppConfig.URL_I2B2_CRC));
    }
}
