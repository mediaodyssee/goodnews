package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.rest.TokenRequest;

public class ReaderTokenRequest extends TokenRequest {
    public static final String SERVICE_NAME = "reader";

    public ReaderTokenRequest(String email, String password) {
        super(email, password);
    }

}
