/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile.google.reader.rest;

import android.util.Log;

import com.gettingmobile.rest.AbstractRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author sven.wiegand
 */
public abstract class ReaderRequest<R extends HttpRequestBase, T> extends AbstractRequest<T> {
    protected static final String BASE_URI = "http://www.wamigo.com/rest/greader-api";
//    protected static final String BASE_URI = "http://api.back.testing.dev.mo-si.eu/greader-api";
    protected static final int MAX_URI_LENGTH = 2048;
    private final URI uri;
    private HttpUriRequest request = null;

    protected ReaderRequest(String relativeUri) throws URISyntaxException {
    	super();
		uri = new URI(BASE_URI + relativeUri);
        Log.i("API", uri.toString());
    }

    protected URI getUri() {
        return uri;
    }

    protected abstract R createRequest();
    
    protected void initRequest(R request) {
    	request.setURI(uri);
        request.setHeader("Accept-Encoding", "gzip");
    }
    
	@Override
	public HttpUriRequest getRequest() {
		if (request == null) {
			final R r = createRequest();
			initRequest(r);
			request = r;
		}
		return request;
	}

	@Override
	public String toString() {
		return uri.toString();
	}
}
