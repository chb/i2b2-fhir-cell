package org.bch.fhir.i2b2.rest;

import org.bch.fhir.i2b2.config.AppConfig;
import org.bch.fhir.i2b2.exception.I2ME2Exception;
import org.bch.fhir.i2b2.util.*;
import org.bch.fhir.i2b2.util.mapper.Mapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;

import java.io.File;

/**
 * Created by CH176656 on 4/14/2015.
 */
public class AbstractRestIT {
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
                .addAsLibraries(resolver.artifact("com.oracle:ojdbc14:10.2.0.3.0").resolveAsFiles())
                .addPackage(AppConfig.class.getPackage())
                .addPackage(I2ME2Exception.class.getPackage())
                .addPackage(Response.class.getPackage())
                .addPackage(Mapper.class.getPackage())
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/web.xml"))
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/jboss-web.xml"))
                .addAsResource("org/bch/i2me2/core/external/REST_i2b2crc_template_query_pdo.xml",
                        "org/bch/i2me2/core/external/REST_i2b2crc_template_query_pdo.xml")
                .addAsResource("org/bch/i2me2/core/external/SOAP_i2b2fr_template_send.xml",
                        "org/bch/i2me2/core/external/SOAP_i2b2fr_template_send.xml")
                .addAsResource("org/bch/i2me2/core/external/SOAP_i2b2fr_template_upload.xml",
                        "org/bch/i2me2/core/external/SOAP_i2b2fr_template_upload.xml")
                .addAsResource("org/bch/i2me2/core/service/claimModifiers.i2me2",
                        "org/bch/i2me2/core/service/claimModifiers.i2me2")
                .addAsResource("org/bch/i2me2/core/service/fillModifiers.i2me2",
                        "org/bch/i2me2/core/service/fillModifiers.i2me2")
                .addAsResource("org/bch/i2me2/core/service/modifierCodes.i2me2",
                        "org/bch/i2me2/core/service/modifierCodes.i2me2")
                .addAsResource("org/bch/i2me2/core/config/config.properties",
                        "org/bch/i2me2/core/config/config.properties")
                .addAsResource("org/bch/i2me2/core/util/mapper/xmlpdoTemplate.xml",
                        "org/bch/i2me2/core/util/mapper/xmlpdoTemplate.xml")
                .addAsResource("org/bch/i2me2/core/util/mapper/xmlpdoTemplateMedRec.xml",
                        "org/bch/i2me2/core/util/mapper/xmlpdoTemplateMedRec.xml")
                .addAsResource("org/bch/i2me2/core/util/mapper/xmlpdoTemplateMedRecNew.xml",
                        "org/bch/i2me2/core/util/mapper/xmlpdoTemplateMedRecNew.xml")
                .addAsResource("org/bch/i2me2/core/rest/mrJSON0.json",
                        "org/bch/i2me2/core/rest/mrJSON0.json")
                .addAsResource("org/bch/i2me2/core/rest/mrNewJSON0.json",
                        "org/bch/i2me2/core/rest/mrNewJSON0.json")
                        //.addAsWebInfResource("arquillian-ds.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}
