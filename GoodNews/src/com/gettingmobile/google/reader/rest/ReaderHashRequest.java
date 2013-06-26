package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.rest.HashRequest;

public class ReaderHashRequest extends HashRequest {
    public static final String SERVICE_NAME = "reader";

    public ReaderHashRequest(String email, String password) {
        super(email, password, SERVICE_NAME);
    }

}
