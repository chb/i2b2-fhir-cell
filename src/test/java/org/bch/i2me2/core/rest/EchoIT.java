package org.bch.i2me2.core.rest;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bch.i2me2.core.util.JSONPRequestFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
//import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
//import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
//import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
//import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EchoIT {
	
	@Deployment
    public static Archive<?> createTestArchive() {
       //MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class)
       //         .loadMetadataFromPom("pom.xml");  
       return ShrinkWrap.create(WebArchive.class, "test.war")
    		    //.addAsLibraries(resolver.artifact("org.mockito:mockito-all:1.8.3").resolveAsFiles())
                .addClasses(Echo.class, EchoIT.class, JaxRsActivator.class, JSONPRequestFilter.class)
               //.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
               //.addAsWebInfResource("arquillian-ds.xml")
               .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
      }
    
    @Before
    public void setUp() throws Exception {
    }
    
    @After
    public void tearDown() throws Exception {
    }
    
    // Requires credentials in JBoss for MedRec2:MedRecApp1_
	@Test
	public void getEchoIT() throws Exception {
		OutputStream out;
		BufferedReader in;
		HttpURLConnection con;
		String response = "";
		URL url = new URL("http://localhost:8080/i2me2/rest/echo/getEcho/hola");	
		con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		//con.setRequestProperty("Content-type: ", "text/plain");
		//con.setRequestProperty("Authorization", "Basic "+ "MedRec:MedRecApp1_");
		//byte[] b = Base64.encodeBase64("MedRec:MedRecApp1_".getBytes("UTF-8"));
		//String auth = new String(b);
		String authetication = "MedRec2:MedRecApp1_";
		String encoding =  javax.xml.bind.DatatypeConverter.printBase64Binary(authetication.getBytes("UTF-8"));	
		con.setRequestProperty("Authorization", "Basic " + encoding);
		//out = con.getOutputStream();
		//out.write(httpReq.getBytes());
		//out.flush();		
		assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
		in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		char[] cbuf = new char[200 + 1];			
		while (true) {		
			int numCharRead = in.read(cbuf, 0, 200);
			if (numCharRead == -1) {
					break;
			}
			String line = new String(cbuf, 0, numCharRead);
			response += line;
		}
		assertEquals(response.trim(), "{\"var\":\"Echo: hola\"}");
		System.out.println(response);
		in.close();
		//out.close();
		con.disconnect();
	}
	
	@Test
	public void getEchoNoPermIT() throws Exception {
		OutputStream out;
		BufferedReader in;
		HttpURLConnection con;
		String response = "";
		URL url = new URL("http://localhost:8080/i2me2/rest/echo/getEcho/hola");	
		con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		//con.setRequestProperty("Content-type: ", "text/plain");
		//con.setRequestProperty("Authorization", "Basic "+ "MedRec:MedRecApp1_");
		//byte[] b = Base64.encodeBase64("MedRec:MedRecApp1_".getBytes("UTF-8"));
		//String auth = new String(b);
		String authetication = "MedRec:MedRe";
		String encoding =  javax.xml.bind.DatatypeConverter.printBase64Binary(authetication.getBytes("UTF-8"));	
		con.setRequestProperty("Authorization", "Basic " + encoding);
		//out = con.getOutputStream();
		//out.write(httpReq.getBytes());
		//out.flush();		
		assertEquals(con.getResponseCode(), HttpURLConnection.HTTP_UNAUTHORIZED);
		//out.close();
		con.disconnect();
	}
	
	@Test
	public void getEchoNoPerm2IT() throws Exception {
		OutputStream out;
		BufferedReader in;
		HttpURLConnection con;
		String response = "";
		URL url = new URL("http://localhost:8080/i2me2/rest/echo/getEcho/hola");	
		con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		//con.setRequestProperty("Content-type: ", "text/plain");
		//con.setRequestProperty("Authorization", "Basic "+ "MedRec:MedRecApp1_");
		//byte[] b = Base64.encodeBase64("MedRec:MedRecApp1_".getBytes("UTF-8"));
		//String auth = new String(b);
		String authetication = "MedRe:MedRecApp1_";
		String encoding =  javax.xml.bind.DatatypeConverter.printBase64Binary(authetication.getBytes("UTF-8"));	
		con.setRequestProperty("Authorization", "Basic " + encoding);
		//out = con.getOutputStream();
		//out.write(httpReq.getBytes());
		//out.flush();		
		assertEquals(con.getResponseCode(), HttpURLConnection.HTTP_UNAUTHORIZED);
		//out.close();
		con.disconnect();
	}
}
