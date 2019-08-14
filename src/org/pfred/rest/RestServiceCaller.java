package org.pfred.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import java.util.logging.Logger;

import org.pfred.rest.RestServiceResult;

public class RestServiceCaller {
    private static Logger log = Logger.getLogger(RestServiceCaller.class.getName());
    private static boolean httpsEnabled = true;

    public static void setHttpsEnabled(boolean enabled) {
        httpsEnabled = enabled;
    }

    public static boolean isHttpsEnabled() {
        return httpsEnabled;
    }

    /**
     * Perform a POST request, could be http or https
     *
     * @param uri
     *            The uri for the post request
     * @param postData
     *            The data string to be posted to the service
     * @param timeout
     *            HTTPS connection timeout threshold in seconds
     * @return the result from the rest request
     * @throws IOException
     *             when error streaming data
     *
     */

    public static RestServiceResult post(String uri, String postData, double timeout) throws IOException {
        log.info("POST: " + uri);

        // Create connection...
        HttpURLConnection connection;
        if (isHttpsEnabled()) {
            connection = createHttpsConnection(uri, "POST");
        } else {
            connection = createHttpConnection(uri, "POST");
        }

        connection.setDoOutput(true);
        connection.setConnectTimeout((int) (timeout * 1000));
        connection.setReadTimeout((int) (timeout * 1000));
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());

        // Start the POST operation...
        writer.write(postData);
        writer.close();
        int respCode = connection.getResponseCode();

        // Test result of POST and set output stream...
        InputStream rptnInputStream = null;
        if (HttpURLConnection.HTTP_OK == respCode) {
            rptnInputStream = connection.getInputStream();
        } else {
            rptnInputStream = connection.getErrorStream();
        }

        return new RestServiceResult(respCode, rptnInputStream);
    }

    /**
     * Perform a GET request, could be either http or https
     *
     * @param uri
     *            The uri for the get request
     * @param timeout
     *            HTTPS connection timeout threshold in seconds
     * @return the result from the rest request
     * @throws IOException
     *             when error streaming data
     *
     */

    public static RestServiceResult get(String uri, double timeout) throws IOException {
        log.info("GET: " + uri);

        // Create connection...
        HttpURLConnection connection;
        if (isHttpsEnabled()) {
            connection = createHttpsConnection(uri, "GET");
        } else {
            connection = createHttpConnection(uri, "GET");
        }

        connection.setConnectTimeout((int) (timeout * 1000));
        connection.setReadTimeout((int) (timeout * 1000));

        // Start the GET operation...
        int respCode = connection.getResponseCode();

        // Test result of GET and set output stream...
        InputStream rptnInputStream = null;
        if (HttpURLConnection.HTTP_OK == respCode) {
            rptnInputStream = connection.getInputStream();
        } else {
            rptnInputStream = connection.getErrorStream();
        }

        return new RestServiceResult(respCode, rptnInputStream);
    }

    private static HttpURLConnection createHttpConnection(String uri, String method) throws IOException {
        // Create connection...
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);

        // // Set authentication...

        return connection;
    }

    private static HttpsURLConnection createHttpsConnection(String uri, String method) throws IOException {
        URL url = new URL(uri);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod(method);

        // // Set authentication...

        return connection;
    }
}
