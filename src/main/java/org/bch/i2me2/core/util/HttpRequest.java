package org.bch.i2me2.core.util;

//import org.apache.commons.io.IOUtils;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
import org.bch.i2me2.core.config.AppConfig;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Abstract functionality for REST calls
 * Created by CH176656 on 3/20/2015.
 */
public class HttpRequest {

    /**
     * Performs a basic Post Request
     * @param url The end point url containing all the sent parameters
     * @param headerAuth The header string for the Authentication part. If is null, no Authorization header is placed
     * @return the HttpResponse
     * @throws IOException If problems with the http connection
     */
  /*  public Response doPostSimple(String url, String headerAuth) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);
        if (headerAuth!=null) {
            request.addHeader("Authorization", headerAuth);
        }
        HttpResponse httpResponse;
        httpResponse = client.execute(request);
        return new ResponseApache(httpResponse);
    }*/

    public Response doPostGeneric(String urlStr, String content, String headerAuth,
                                  String headerContentType) throws IOException {

        OutputStream out;
        HttpURLConnection con;

        URL url = new URL(urlStr);
        con = (HttpURLConnection) url.openConnection();

        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod("POST");
        if (content!=null) {
            con.setRequestProperty("Content-length", String.valueOf(content.length()));
        }
        if (headerContentType != null) {
            con.setRequestProperty("Content-type: ", headerContentType);
        }
        if (headerAuth!=null) {
            con.setRequestProperty("Authorization", headerAuth);
        }
        if (content!=null) {
            out = con.getOutputStream();
            out.write(content.getBytes());
            out.flush();
            out.close();
        }
        Response resp = new ResponseJava(con);
        con.disconnect();
        return resp;
    }

    public static class ResponseJava implements Response {
        private HttpURLConnection con;
        private String content;
        private int status;

        ResponseJava(HttpURLConnection con) throws IOException {
            OutputStream out;
            BufferedReader in;
            String response = "";
            this.con = con;
            this.status = con.getResponseCode();
            if (this.status > 203) {
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

        @Override public int getResponseCode() {
            return this.status;
        }

        @Override public String getContent() {
            return this.content;
        }
    }
/*
    public static class ResponseApache implements Response {
        private HttpResponse httpResponse;
        String content;

        ResponseApache(HttpResponse httpResponse) throws IOException {
            this.httpResponse = httpResponse;
            if (this.httpResponse.getEntity()==null) {
                this.content=null;
            } else {
                StringWriter buffer = new StringWriter();
                IOUtils.copy(this.httpResponse.getEntity().getContent(), buffer, "UTF-8");
                this.content = buffer.toString();
            }
        }

        @Override
        public int getResponseCode() {
            return this.httpResponse.getStatusLine().getStatusCode();
        }

        @Override
        public String getContent() {
            return this.content;
        }

    }
*/
    public static interface Response {
        public int getResponseCode();
        public String getContent();
    }

}

