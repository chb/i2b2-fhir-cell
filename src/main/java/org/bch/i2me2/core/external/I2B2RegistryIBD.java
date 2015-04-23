package org.bch.i2me2.core.external;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by CH176656 on 4/22/2015.
 * The code of this class is provisional and only valid for the first release of i2me2
 */

public class I2B2RegistryIBD extends WrapperAPI {

    public void connect() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");

        Connection con = DriverManager.getConnection("jdbc:oracle:thin:@10.17.16.148:1521:xe", "idmuser", "idmuser");
    }
}
