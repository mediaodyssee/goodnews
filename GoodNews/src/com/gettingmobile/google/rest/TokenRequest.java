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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sven.wiegand
 */
public class TokenRequest extends AbstractRequest<String> {
    private final String authTokenKey = "token";
    private final String TTLKey = "TTL";
    private final HttpPost request;

    public TokenRequest(String email, String password) {
        super();
        request = new HttpPost(UriFactory.createUri("http://www.wamigo.com/rest/resource/token"));

        Log.w("TokenRequest", "start");
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("client_salt", HashRequest.client_salt);
            jsonObject.put("login", email);
            jsonObject.put("hash", HashRequest.final_hash);
//            request.setEntity(new UrlEncodedFormEntity(formValues, CharacterSet.UTF8));
            StringEntity se = new StringEntity(jsonObject.toString(), HTTP.UTF_8);
            se.setContentType("application/json");
            request.setEntity(se);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException ex) {
        	// will never happen
            throw new RuntimeException(ex);
        }
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
        Log.w("TokenRequest", "end");

        final List<String> lines = new ContentLinesExtractor().extract(response.getEntity());
        for (final String line : lines) {
            String token = null;
            if (line.startsWith("{")) {
                try {
                    JSONObject jsonObject = new JSONObject(line);
                    token = jsonObject.getString(authTokenKey);
                    String ttl = jsonObject.getString(TTLKey);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return token;
        }
        return null;
    }

}
