package org.pfred.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Scanner;

public class RestServiceResult {
  private int responseCode;
  private InputStream responseStream;

  public RestServiceResult(int responseCode, InputStream responseStream) {
    this.responseCode = responseCode;
    this.responseStream = responseStream;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }

  public InputStream getResponseStream() {
    return responseStream;
  }

  public void setResponseStream(InputStream responseStream) {
    this.responseStream = responseStream;
  }

  public String getResultString() throws IOException {
    if (HttpURLConnection.HTTP_OK == getResponseCode()) {
      return getResponseAsString(getResponseStream());
    }
    return null;
  }

  public String getErrorString() throws IOException {
    if (HttpURLConnection.HTTP_OK != getResponseCode()) {
      return getResponseAsString(getResponseStream());
    }
    return null;
  }

  private String getResponseAsString(InputStream is) throws IOException {
    Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");
    String result =  s.hasNext() ? s.next() : "";
    s.close();
    return result;
  }
}
