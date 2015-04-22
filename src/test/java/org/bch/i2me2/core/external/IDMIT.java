package org.bch.i2me2.core.external;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

/**
 * Created by CH176656 on 4/21/2015.
 */
public class IDMIT {
    // A token that never expires created just for testing purposes
    private static String tokenTest="eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE0Mjk3MTg0Mjc0MDUsInN1YmplY3RJZCI6NjU1MzcsImp0aSI6IjgxZjQyYTNmLTFmMzYtNDNmZS1iMDZjLTQzZDIwMDNlZThiYiIsImlhdCI6MTQyOTcxODEyNzQwNX0.qGQT2Lk30hn3vBNuZ9xvhovwto0FA-GoIbauQE8h75_0abaCJU7_2Wt35_hJUk98OLSQrHbjZJf6Ewe5o2J8zw";

    private static IDM idm = new IDM();

    @Test
    public void testSimpleIT() throws Exception {
        IDM.PersonalInfo subjectIdInfo = idm.getPersonalSubjectId(tokenTest);
        IDM.PersonalInfo demoInfo = idm.getPersonalInfo(tokenTest);

        assertEquals("565656", subjectIdInfo.getSubjectId());
        assertEquals("John", demoInfo.getFirstName());
        assertEquals("Smith", demoInfo.getLastName());
        assertEquals("1985-04-22", demoInfo.getBirthDate());
        assertEquals("M", demoInfo.getGender());
        assertEquals("02130", demoInfo.getZipCode());
    }
}
