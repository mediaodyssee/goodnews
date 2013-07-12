/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile.google.rest;

import android.util.Log;

import com.gettingmobile.ApplicationException;
import com.gettingmobile.google.AuthorizationException;
import com.gettingmobile.io.CharacterSet;
import com.gettingmobile.net.UriFactory;
import com.gettingmobile.rest.AbstractRequest;
import com.gettingmobile.rest.ContentIOException;
import com.gettingmobile.rest.entity.ContentLinesExtractor;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author sven.wiegand
 */
public class HashRequest extends AbstractRequest<String> {
    private final String saltKey = "salt";
    private final HttpGet request;
    public static String client_salt;
    public static String server_salt;
    public static String final_hash;
    private String email;
    private String password;
    public HashRequest(String email, String password, String service) {
        super();
        this.email = email;
        this.password = password;
        request = new HttpGet(UriFactory.createUri("http://main.front.testing.dev.mo-si.eu/rest/resource/salt"));
    }

    @Override
    public HttpUriRequest getRequest() {
        return request;
    }


    @Override
	public void throwExceptionIfApplicable(HttpResponse response)
			throws ApplicationException {
    	if (response.getStatusLine().getStatusCode() == 403) {
    		throw new AuthorizationException();
    	}
	}

	@Override
    public String processResponse(HttpResponse response) throws ContentIOException {
        Log.w("HashRequest", "start");
        final List<String> lines = new ContentLinesExtractor().extract(response.getEntity());
        for (final String line : lines) {
            if (line.startsWith("{"))
            {
                try {
                    JSONObject jsonObject = new JSONObject(line);
                    server_salt = jsonObject.getString(saltKey);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        client_salt = UUID.randomUUID().toString();
        SecureRandom random = new SecureRandom();
        if(server_salt != null)
        {
            byte[] hash = new byte[0];
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");

                byte[] loginPasswordClientSalt = (email+':'+password+':'+client_salt).getBytes();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write(server_salt.getBytes());
                outputStream.write(':');
                outputStream.write(hashToString(md.digest(loginPasswordClientSalt)).getBytes());
                hash = md.digest(outputStream.toByteArray());

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            final_hash = hashToString(hash);
        }
        Log.w("HashRequest", "end");
        return final_hash;
    }

    String hashToString(byte[] hash)
    {
        StringBuffer result = new StringBuffer();
        for (byte byt : hash)
            result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }
}
