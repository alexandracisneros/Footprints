package com.neversoft.smartwaiter.io;

import com.neversoft.smartwaiter.database.DBHelper;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by Usuario on 03/09/2015.
 */
public class RestConnector {
    private HttpURLConnection mConnection;
    private String mFormBody;

    public RestConnector(HttpURLConnection mConnection) {
        this.mConnection = mConnection;
    }

    public void setFormBody(List<NameValuePair> formData)
            throws UnsupportedEncodingException {
        if (formData == null) {
            mFormBody = null;
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < formData.size(); i++) {
            NameValuePair item = formData.get(i);
            sb.append(item.getValue());
        }
        mFormBody = sb.toString();
//        Log.d(DBHelper.TAG, sb.toString());
    }

    private void writeFormData(String charset, OutputStream output)
            throws UnsupportedEncodingException, IOException {
        try {
            output.write(mFormBody.getBytes(charset));
            output.flush();
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    public Object doRequest(String url) {
        // Generate random string for boundary
        String charset = Charset.defaultCharset().displayName();
        try {
            if (mFormBody != null) {
                // Form data to post
                mConnection
                        .setRequestProperty("Content-Type",
                                "application/json; charset="
                                        + charset);
                mConnection.setFixedLengthStreamingMode(mFormBody.length());
            }

            // This is the first call on URLConnection that actually
            // does Network IO. Even openConnection() is still just
            // doing local operations
            mConnection.connect();

            if (mFormBody != null) {
                OutputStream out = mConnection.getOutputStream();
                writeFormData(charset, out);
            }

            // Get response data
            int status = mConnection.getResponseCode();
            if (status >= 300) {
                String message = mConnection.getResponseMessage();
                return new HttpResponseException(status, message);
            }

            InputStream in = mConnection.getInputStream();
            String enconding = mConnection.getContentEncoding();
            if (enconding == null) {
                enconding = "UTF-8";
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    in, enconding));

            StringBuilder sb = new StringBuilder();

            String line = null;
            //int count=1;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString().trim();

        } catch (IOException ioe) {
            return new Exception("Se produjo un problema de conexión. Por favor vuelva a intentar.");
        } catch (Exception e) {
            return e;
        } finally {
            if (mConnection != null) {
                mConnection.disconnect();
            }
        }
    }
}
