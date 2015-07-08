package org.bch.i2me2.core.external;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.rest.JaxRsActivator;
import org.bch.i2me2.core.util.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;

/**
 * Created by CH176656 on 4/7/2015.
 */
public abstract class AbstractIT {
    @Deployment
    public static Archive<?> createTestArchive() {
        MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class)
                .loadMetadataFromPom("pom.xml");
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsLibraries(resolver.artifact("org.apache.axis2:axis2-transport-http:1.6.2").resolveAsFiles())
                .addAsLibraries(resolver.artifact("org.apache.axis2:axis2-transport-local:1.6.2").resolveAsFiles())
                .addAsLibraries(resolver.artifact("org.apache.axis2:axis2:1.6.2").resolveAsFiles())
                .addAsLibraries(resolver.artifact("commons-io:commons-io:2.0.1").resolveAsFiles())
                .addAsLibraries(resolver.artifact("org.json:json:20090211").resolveAsFiles())
                .addClasses(RXConnect.class, HttpRequest.class, RXConnectIT.class, JaxRsActivator.class,
                        AppConfig.class, I2ME2Exception.class, Response.class, JSONPRequestFilter.class,
                        WrapperAPI.class, SoapRequest.class, Validator.class, Utils.class, AbstractIT.class,
                        IDM.class, IDMIT.class)
                .addAsResource("org/bch/i2me2/core/config/config.properties",
                        "org/bch/i2me2/core/config/config.properties")
                        //.addAsWebInfResource("arquillian-ds.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}
