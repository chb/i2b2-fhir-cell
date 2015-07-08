package org.bch.fhir.i2b2.external;

import org.bch.fhir.i2b2.config.AppConfig;
import org.bch.fhir.i2b2.exception.I2ME2Exception;
import org.bch.fhir.i2b2.rest.JaxRsActivator;
import org.bch.fhir.i2b2.util.*;
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
                .addClasses(HttpRequest.class, JaxRsActivator.class,
                        AppConfig.class, I2ME2Exception.class, Response.class, JSONPRequestFilter.class,
                        WrapperAPI.class, SoapRequest.class, Validator.class, Utils.class, AbstractIT.class)
                .addAsResource("org/bch/i2me2/core/config/config.properties",
                        "org/bch/i2me2/core/config/config.properties")
                        //.addAsWebInfResource("arquillian-ds.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}
