package org.bch.fhir.i2b2.util;

import org.bch.fhir.i2b2.config.AppConfig;

import javax.ejb.Stateless;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Abstract functionality for REST calls
 * @author CHIP-IHL
 */
@Stateless
public class HttpRequest {

    /**
     * Performs an empty POST rest call to to the given end point using authorization header
     * @param urlStr The url
     * @param headerAuth The authorization header
     * @return The http response
     * @throws IOException In case input/output error
     */
    public Response doPostGeneric(String urlStr,  String headerAuth) throws IOException {
        return doPostGeneric(urlStr, null, headerAuth, null);
    }

    /**
     * Performs a POST rest call to the given end point with content, authorization header and type header
     * @param urlStr The url
     * @param content The content
     * @param headerAuth The authorization header
     * @param headerContentType The Content type header
     * @return The hhtp response
     * @throws IOException In case input/output error
     */
    public Response doPostGeneric(String urlStr, String content, String headerAuth,
                                  String headerContentType) throws IOException {
        return doPostGeneric(urlStr, content, headerAuth, headerContentType, "POST");
    }

    /**
     * Performs the rest call provided by 'operation' to the given end point with content, authorization header and
     * type header
     * @param urlStr The url
     * @param content The content
     * @param headerAuth The authorization header
     * @param headerContentType The Content type header
     * @param operation The operation type: POST | GET | DELETE
     * @return The http response
     * @throws IOException In case input/output error
     */
    public Response doPostGeneric(String urlStr, String content, String headerAuth,
                                  String headerContentType, String operation) throws IOException {

        OutputStream out;
        HttpURLConnection con;

        URL url = new URL(urlStr);
        con = (HttpURLConnection) url.openConnection();

        con.setAllowUserInteraction(false);
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod(operation);
        if (content!=null) {
            con.setRequestProperty("Content-length", String.valueOf(content.length()));
        }
        if (headerContentType != null) {
            con.setRequestProperty("Content-type", headerContentType);
        }
        if (headerAuth!=null) {
            con.setRequestProperty("Authorization", headerAuth);
        }
        if (content!=null) {
            out = con.getOutputStream();
            out.write(content.getBytes("UTF-8"));
            out.flush();
            out.close();
        }

        Response resp = new ResponseJava(con);
        con.disconnect();
        return resp;
    }

    /**
     * Static class that captures the http responses
     * @author CHIP-IHL
     */
    public static class ResponseJava implements Response {
        private HttpURLConnection con;
        private String content;
        private int status;

        /**
         * Constructor
         * @param con The HttpURLConnection connection
         * @throws IOException In case of I/O error
         */
        ResponseJava(HttpURLConnection con) throws IOException {
            BufferedReader in;
            String response = "";
            this.con = con;
            this.status = con.getResponseCode();
            System.out.println("RETURN CODE:" + this.status);
            if (this.status >= 400) {
                this.content=null;
                return;
            }

            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            char[] buffer = new char[AppConfig.HTTP_TRANSPORT_BUFFER_SIZE + 1];
            while (true) {
                int numCharRead = in.read(buffer, 0, AppConfig.HTTP_TRANSPORT_BUFFER_SIZE);
                if (numCharRead == -1) {
                    break;
                }
                String line = new String(buffer, 0, numCharRead);
                response += line;
            }
            in.close();
            this.content = response;
        }

        /**
         * Returns the http status code
         * @return The status code
         */
        @Override public int getResponseCode() {
            return this.status;
        }

        /**
         * Returns the content of the http
         * @return The content
         */
        @Override public String getContent() {
            return this.content;
        }
    }

}

