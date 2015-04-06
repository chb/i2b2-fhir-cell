package org.bch.i2me2.core.external;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.rest.Echo;
import org.bch.i2me2.core.rest.JaxRsActivator;
import org.bch.i2me2.core.util.*;
import org.bch.i2me2.core.service.WrapperService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import static org.junit.Assert.*;

/**
 * Integration Tests for RXConnect
 * NOTE: It does not require Arquillian, since RXConnect it's deployed in a dev VM machine
 * Created by CH176656 on 3/30/2015.
 */
@RunWith(Arquillian.class)
public class RXConnectIT {

    // Test case with data - Return ok. 6 claims and 53 fills
    private static String firstName="Bert";
    private static String lastName="Schnur";
    private static String birthDate="19450419";
    private static String gender="M";
    private static String zip="63050";

    // test case with no data -- 0 returned with error message
    private static String firstName2="David";
    private static String lastName2="Thrower";
    private static String birthDate2="19330222";
    private static String gender2="M";
    private static String zip2="34737";

    // test case with some data.. just 3 claims
    private static String firstName3="Johnathan";
    private static String lastName3="Swift";
    private static String birthDate3="19791024";
    private static String gender3="M";
    private static String zip3="55427";

    // We simulate the injection
    private RXConnect rxconnect = new RXConnect();

    @Deployment
    public static Archive<?> createTestArchive() {
        MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class)
                 .loadMetadataFromPom("pom.xml");
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsLibraries(resolver.artifact("org.apache.axis2:axis2-transport-http:1.6.2").resolveAsFiles())
                .addAsLibraries(resolver.artifact("org.apache.axis2:axis2-transport-local:1.6.2").resolveAsFiles())
                .addAsLibraries(resolver.artifact("org.apache.axis2:axis2:1.6.2").resolveAsFiles())
                .addAsLibraries(resolver.artifact("commons-io:commons-io:2.0.1").resolveAsFiles())
                .addClasses(RXConnect.class, HttpRequest.class, RXConnectIT.class, JaxRsActivator.class,
                        AppConfig.class, I2ME2Exception.class, Response.class, JSONPRequestFilter.class,
                        WrapperAPI.class, SoapRequest.class, Validator.class, Utils.class)
                        .addAsResource("org/bch/i2me2/core/config/config.properties",
                                "org/bch/i2me2/core/config/config.properties")
                                //.addAsWebInfResource("arquillian-ds.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    // TODO: TO FIX
    @Test
    public void getMedicationsListCase_1_IT() throws Exception {
        System.setProperty("javax.net.ssl.keyStore","/etc/ssl/certs/cacerts");
        System.setProperty("javax.net.ssl.keyStorePassword","changeit");
        Double version = Double.parseDouble(System.getProperty("java.specification.version"));
        System.out.println("JAA VERSION:" + version);
        String resp = rxconnect.getMedicationsList(firstName, lastName,zip, birthDate, gender);
        JSONObject json = new JSONObject(resp);
        JSONObject rxhistory = json; //json.getJSONObject("RxHistorySegments");
        String status = rxhistory.getString("rxRecsReturned");
        assertEquals("ALL", status);

        JSONArray orders = json.getJSONArray("orders");
        assertEquals(59, orders.length());
    }

    // TODO: TO FIX
    @Ignore
    public void getMedicationsListCase_2_IT() throws Exception {
        String resp = rxconnect.getMedicationsList(firstName2, lastName2,zip2, birthDate2, gender2);
        JSONObject json = new JSONObject(resp);
        JSONObject rxhistory = json; //json.getJSONObject("RxHistorySegments");

        String status = rxhistory.getString("rxRecsReturned");
        assertEquals("Error", status);

        JSONArray orders = json.getJSONArray("orders");
        assertEquals(0, orders.length());
    }

    // TODO: TO FIX
    @Test
    public void getMedicationsListCase_3_IT() throws Exception {
        System.setProperty("javax.net.ssl.keyStore","/etc/ssl/certs/cacerts");
        System.setProperty("javax.net.ssl.keyStorePassword","changeit");
        String resp = rxconnect.getMedicationsList(firstName3, lastName3, zip3, birthDate3, gender3);
        JSONObject json = new JSONObject(resp);
        JSONObject rxhistory = json;//json.getJSONObject("RxHistorySegments");
        String status = rxhistory.getString("rxRecsReturned");
        assertEquals("SOME", status);

        JSONArray orders = json.getJSONArray("orders");
        assertEquals(3, orders.length());
    }

}
