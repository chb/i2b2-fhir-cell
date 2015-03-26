package org.bch.i2me2.core.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;

import javax.mail.util.ByteArrayDataSource;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by CH176656 on 3/26/2015.
 */
public class SoapRequest {

    private static ServiceClient sender = null;

    /**
     * Sends a SOAP request
     * @param endPointURL   The complete url end point, including http or https, the host, the port and the end point
     * @param xmlRequest    The xml request body of the soap message
     * @param action        The action
     * @return              The response
     * @throws Exception
     */
    public static SoapResponse sendSoap(String endPointURL, String xmlRequest, String action) throws Exception {
        return sendSoap(endPointURL, xmlRequest, action, null, null);
    }

    /**
     * Sends a SOAP request attaching a file
     * @param endpointURL   The complete url end point, including http or https, the host, the port and the end point
     * @param xmlRequest    The xml request body of the soap message
     * @param action        The action
     * @param fileName      The file name
     * @param fileData      The content of the file
     * @return              The response
     * @throws Exception
     */
    public static SoapResponse sendSoap(String endpointURL, String xmlRequest, String action, String fileName,
                                  String fileData) throws IOException {
        OMElement getFr = getFrPayLoad(xmlRequest);
        Options options = new Options();
        String serviceURL = endpointURL;
        if (serviceURL.endsWith("/")) {
            serviceURL = serviceURL.substring(0, serviceURL.length() - 1);
        }

        options.setTo(new EndpointReference(serviceURL));

        options.setAction(action);
        options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        // Increase the time out to receive large attachments
        options.setTimeOutInMilliSeconds(10000);

        options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
        options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,Constants.VALUE_TRUE);
        options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR, "temp");
        options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, "4000");

        ServiceClient sender = SoapRequest.getServiceClient();
        sender.setOptions(options);

        OperationClient mepClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);

        MessageContext mc = new MessageContext();
        if (fileData != null && fileName != null) {
            ByteArrayDataSource bytes = new ByteArrayDataSource(fileData.getBytes(), "");
            javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(bytes);
            mc.addAttachment(fileName, dataHandler);
        }
        mc.setDoingSwA(true);

        SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope env = sfac.getDefaultEnvelope();

        env.getBody().addChild(getFr);

        mc.setEnvelope(env);
        mepClient.addMessageContext(mc);
        mepClient.execute(true);

        MessageContext inMsgtCtx = mepClient.getMessageContext("In");

        SOAPEnvelope responseEnv = inMsgtCtx.getEnvelope();
        //OMElement soapResponse = responseEnv.getBody().getFirstElement();
        //OMElement soapResult = soapResponse.getFirstElement();
        //String i2b2Response = soapResponse.toString();

        return new SoapResponse(responseEnv);
        //getEnvelope().getBody().getFault().getFaultCode();
        //mepClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE).getEnvelope().toStringWithConsume();

    }

    public static ServiceClient getServiceClient() throws AxisFault {
        if (sender == null) {
            sender = new ServiceClient();
        }
        return sender;
    }

    /**
     * Function to convert Ont requestVdo to OMElement
     *
     * @param requestVdo String requestVdo to send to Ont web service
     * @return An OMElement containing the Ont web service requestVdo
     */
    public static OMElement getFrPayLoad(String requestVdo) {
        OMElement lineItem=null;
        try {
            StringReader strReader = new StringReader(requestVdo);
            XMLInputFactory xif = XMLInputFactory.newInstance();
            XMLStreamReader reader = xif.createXMLStreamReader(strReader);

            StAXOMBuilder builder = new StAXOMBuilder(reader);
            lineItem = builder.getDocumentElement();
        } catch (Exception e) {
            // Nothing to do.
        }
        return lineItem;
    }

    public static class SoapResponse implements Response {

        private int code;
        private String content=null;
        private SOAPEnvelope soapEnvelope;
        SoapResponse(SOAPEnvelope soapEnvelope) {
            this.soapEnvelope = soapEnvelope;
            if (soapEnvelope!=null) {
                OMElement soapResponse = soapEnvelope.getBody().getFirstElement();
                // This is SOAP, so, it is suppose we cannot deal with transport layer http. So, we assume that
                // if no faults, the code is 200. Otherwise, it's 500.
                this.code = 200;
                if (soapEnvelope.getBody().hasFault()) {
                    this.code= 500;
                }
                this.content = soapResponse.toString();
            }
        }

        @Override
        public int getResponseCode() {
            return this.code;
        }

        @Override
        public String getContent() {
            return this.content;
        }
    }


}
