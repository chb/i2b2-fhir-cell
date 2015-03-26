package org.bch.i2me2.core.util;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.rest.Echo;
import org.bch.i2me2.core.rest.JaxRsActivator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
//import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.HttpURLConnection;

/**
 * Created by CH176656 on 3/20/2015.
 */
@RunWith(Arquillian.class)
public class HttpRequestIT {

    @Deployment
    public static Archive<?> createTestArchive() {
        MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class)
                 .loadMetadataFromPom("pom.xml");

        WebArchive ret = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(Echo.class, JaxRsActivator.class, JSONPRequestFilter.class, HttpRequest.class,
                        AppConfig.class, I2ME2Exception.class, Response.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return ret;
    }

    // Requires credentials in JBoss for MedRec2:MedRecApp1_ in the role RestClient
    @Test
    public void doPostSimpleBasic() throws Exception {
        String url = "http://127.0.0.1:8080/i2me2/rest/echo/echo?var=hola";

        HttpRequest req = new HttpRequest();
        Response resp = req.doPostGeneric(url, "aaa", null, null);
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, resp.getResponseCode());
        String authentication = "MedRec2:MedRecApp1_";
        String encoding =  javax.xml.bind.DatatypeConverter.printBase64Binary(authentication.getBytes("UTF-8"));

        resp = req.doPostGeneric(url, null, "BASIC " + encoding, null);
        assertEquals(HttpURLConnection.HTTP_OK, resp.getResponseCode());
        assertEquals("{\"var\":\"Echo: hola\"}", resp.getContent());
    }
}
