package org.bch.fhir.i2b2.external;

import org.bch.fhir.i2b2.config.AppConfig;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.bch.fhir.i2b2.exception.I2ME2Exception;
import org.bch.fhir.i2b2.util.Response;
import org.bch.fhir.i2b2.util.Utils;
import org.bch.fhir.i2b2.util.mapper.Mapper;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

import javax.ejb.Stateless;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeFilter;

/**
 * Created by CH176656 on 4/7/2015.
 */
@Stateless
public class I2B2QueryService extends WrapperAPI {

    private static StringBuffer queryPdoTemplate = new StringBuffer();

    private static String MODULE = "[I2B2QUERYSERVICE]";
    private static String OP_GET_PATIENT_DATA = "[GET_PATIENT_DATA]";

    public QueryResponse getPatientData(String patientId, String source, Date date) throws IOException, FHIRI2B2Exception {
        this.log(Level.INFO, MODULE+OP_GET_PATIENT_DATA+":IN, patientID:" + patientId);
        try {
            loadTemplateQueryPdo();
        } catch (IOException e) {
            this.log(Level.SEVERE, MODULE+OP_GET_PATIENT_DATA+"Error loading templates");
            throw e;
        }

        // Get the credentials to access i2b2
        String credentials=null;
        try {
            credentials = AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_FILE_I2B2);
        } catch (IOException e) {
            this.log(Level.WARNING, MODULE+OP_GET_PATIENT_DATA+"I2B2 credentials not found. Continue without");
            // It means the file does not exists
        }
        String i2b2user="";
        String i2b2pwd="";
        if (credentials!=null) {
            String[] usrpwd = credentials.split(":");
            i2b2user = usrpwd[0];
            if (usrpwd.length > 1) {
                i2b2pwd = usrpwd[1];
            }
        }
        // Prepare date
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat(AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2));
        String dateTime = dateFormatOutput.format(new Date());
        String dateTimeFrom = dateFormatOutput.format(date);

        // Generate the url
        String url = generateURLQuery();

        // Generate the body message
        String i2b2Message = generateQueryPdoRequest(
                dateTime,
                AppConfig.getProp(AppConfig.I2B2_DOMAIN),
                i2b2user,
                i2b2pwd,
                AppConfig.getProp(AppConfig.I2B2_PROJECT_ID),
                source,
                patientId,
                dateTimeFrom);

        // Get content type for http request
        String contentType = AppConfig.getProp(AppConfig.REST_CONTENT_TYPE_I2B2_CRC_QUERY);
        //System.out.println("Content-Type: " + contentType);
        //System.out.println("URL: " + url);
        //System.out.println(i2b2Message);
        
        // Do POST REST call
        Response response = getHttpRequest().doPostGeneric(url, i2b2Message, null, null, "PUT");
        //Response response = getHttpRequest().doPostGeneric(url+"/"+ URLEncoder.encode("i2b2Message", "UTF-8"), null, null, null);

        if (response.getResponseCode() >= 400) {
            this.log(Level.SEVERE, MODULE+OP_GET_PATIENT_DATA+ "Error querying i2b2. patientId: " + patientId + ", " +
                    "patientIdSource:" + source);
            throw new FHIRI2B2Exception("Error querying i2b2. patientId: " + patientId + ", "+
                    "patientIdSource:" + source);
        }

        QueryResponse out=null;
        try {
            out = new QueryResponse(response.getContent());
        } catch (Exception e) {
            this.log(Level.SEVERE, MODULE+OP_GET_PATIENT_DATA+ "Error parsing xml response from I2B2." +
                    patientId + ", " + "patientIdSource:" + source );
            throw new FHIRI2B2Exception("Error parsing xml file from I2B2.", e);

        }
        return out;
    }

    private static String generateURLQuery() throws FHIRI2B2Exception{
        return Utils.generateURL(
                AppConfig.getProp(AppConfig.NET_PROTOCOL_I2B2_CRC),
                AppConfig.getProp(AppConfig.HOST_I2B2_CRC),
                AppConfig.getProp(AppConfig.PORT_I2B2_CRC),
                AppConfig.getProp(AppConfig.EP_I2B2_CRC_PDOREQUEST));
    }

    private static String generateQueryPdoRequest(String dateMsg, String domain, String user, String password,
                                                  String projectId, String patientId, String patientIdSource,
                                                  String dateFrom) {
        //return queryPdoTemplate.toString();
        return String.format(queryPdoTemplate.toString(),
                dateMsg, domain, user, password, projectId, patientId, patientIdSource, dateFrom);

    }

    private static void loadTemplateQueryPdo() throws IOException, FHIRI2B2Exception{
        if (queryPdoTemplate.length()==0) {
            Utils.textFileToStringBuffer(
                    I2B2QueryService.class,
                    AppConfig.getProp(AppConfig.FILENAME_REST_TEMP_I2B2_CRC_QUERYPDO),
                    queryPdoTemplate, "\n");
        }
    }

    // the response class
    public static class QueryResponse {

        private static String START_DATE_TAG="start_date";
        private String xmlResponse;
        private Document doc;

        private DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        public QueryResponse(String xmlResponse) throws Exception {
            this.xmlResponse=xmlResponse;
            InputSource is = new InputSource(new StringReader(xmlResponse));
            this.doc = this.dBuilder.parse(is);
        }

        /**
         * Returns the xml Document containing the observations whose start_date is behind (not equal) the given date
         * @param date  The date
         * @return      The doc xml
         */
        public Document getObservationsByStartDate(Date date) {
            return getObservationsByStartDate(this.doc, date);
        }

        /**
         * The same as above but taking as a base the docBase document
         * @param docBase   The xml doc base
         * @param date      The date
         * @return          The new xml doc
         */
        public Document getObservationsByStartDate(Document docBase, Date date) {
            NodeList roots = docBase.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
            ObservationIterator iterator = new ObservationIterator(roots, START_DATE_TAG, date,1);
            return getFilteredDocument(iterator);
        }

        /**
         * Return the xml Document containing the observations that contain a 'tagName' element whose inner
         * test is exactly 'value'
         * @param tagName       The tag name
         * @param value         The value
         * @return              The doc xml
         */
        public Document getObservationsByValue(String tagName, String value) {
            return getObservationsByValue(this.doc, tagName, value);
        }

        /**
         * Same as above but taking as a base the docBase document
         * @param docBase       The base xml document
         * @param tagName       The tag name
         * @param value         The value
         * @return              The new xml doc
         */
        public Document getObservationsByValue(Document docBase, String tagName, String value) {
            NodeList roots = docBase.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
            ObservationIterator iterator = new ObservationIterator(roots, tagName, value);
            return getFilteredDocument(iterator);
        }

        public NodeList getAllObservations(Document docBase) {
            return docBase.getElementsByTagName(Mapper.XmlPdoTag.TAG_OBSERVATIONS.getTagValueIn());
        }

        public NodeList getAllObservations() {
            return getAllObservations(this.doc);
        }

        private Document getFilteredDocument(ObservationIterator iterator) {
            Document docOut = this.dBuilder.newDocument();
            Element mainElement = docOut.createElement(Mapper.XmlPdoTag.TAG_OBSERVATIONS.toString());
            docOut.appendChild(mainElement);
            while (iterator.hasNext()) {
                Node importedNode = docOut.importNode(iterator.next(), true);
                mainElement.appendChild(importedNode);
            }
            return docOut;
        }


        public String documentToString(Document document) throws TransformerException {
            DOMSource domSource = new DOMSource(document);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        }
    }

    // Implementation of an iterator to filter more comfortably
    private static final class ObservationIterator implements Iterator<Node> {
    	private String tagElement=null;
    	private String value=null;
    	private Date startDate=null;
    	// the filter operation with data -1, 0, 1 for less, equal or greater than the date in the xml element 
    	private int op=0;
        private int index=-1;
        private NodeList nodeList;
    	
    	public ObservationIterator(NodeList roots, String tagElement, String value) {
    		this.tagElement = tagElement;
    		this.value = value;
            this.nodeList = roots;
    	}
    	
    	public ObservationIterator(NodeList roots, String tagElement, Date startDate, int op) {
    		this.tagElement = tagElement;
    		this.startDate=startDate;
            this.op = op;
            this.nodeList = roots;
    	}

        @Override
        public boolean hasNext() {
            if (this.nodeList==null) return false;
            if (this.nodeList.getLength()>this.index+1) {
                int i = this.index;
                boolean done=false;
                do {
                    i++;
                    done = acceptNode(this.nodeList.item(i));
                } while (!done && i+1 <this.nodeList.getLength());
                this.index = i-1;
                return done;
            }
            return false;
        }
        @Override
        public void remove() {}

        @Override
        public Node next() {
            if (hasNext()) {
                this.index++;
                return this.nodeList.item(this.index);
            }
            return null;
        }

        private boolean acceptNode(Node n) {
            if (n instanceof Element) {
                Element elem = (Element)n;
                if (this.tagElement==null) return false;
                NodeList childs = elem.getElementsByTagName(this.tagElement);
                if (childs.getLength()==0) return false;
                Element e = (Element) childs.item(0);
                if (this.value != null) {
                    if (e.getTextContent().trim().equals(value)) return true;
                } else if (this.startDate != null) {
                    String dateText = e.getTextContent().trim();
                    SimpleDateFormat dateFormatOutput;
                    try {
                        dateFormatOutput = new SimpleDateFormat(AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2));
                    } catch (Exception ex) {
                        return false;
                    }
                    Date dateTime;
                    try {
                        dateTime = dateFormatOutput.parse(dateText);
                    } catch (ParseException e1) {
                        return false;
                    }
                    int result = dateTime.compareTo(startDate);
                    if (result==0 && this.op==0) return true;
                    if (result <0 && this.op <0) return true;
                    if (result >0 && this.op >0) return true;
                }
            }
            return false;
        }
    }   
}
