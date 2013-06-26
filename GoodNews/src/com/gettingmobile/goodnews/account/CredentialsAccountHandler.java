package com.gettingmobile.goodnews.account;

import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.google.reader.rest.ReaderAuthorizationRequest;
import com.gettingmobile.google.reader.rest.ReaderHashRequest;
import com.gettingmobile.google.reader.rest.ReaderTokenRequest;
import com.gettingmobile.rest.RequestCallback;

public final class CredentialsAccountHandler extends AbstractAccountHandler {
    protected static final String ACCOUNT_TYPE = CredentialsAccountHandler.class.getSimpleName();
	public CredentialsAccountHandler(Application app) {
		super(app);
	}

    /*
     * login handling
     */

    private Credentials getCredentials() {
        final String userName = getSettings().getUserName();
        final String password = getSettings().getPassword();
        return userName != null && userName.length() > 0 && password != null && password.length() > 0 ?
                new Credentials(userName, password) : null;
    }

    @Override
    public boolean hasAccount() {
        return getCredentials() != null;
    }

    @Override
	public void login(LoginCallback callback) throws IllegalStateException {
        final Credentials c = getCredentials();
        if (c == null)
            throw new IllegalStateException("Login not possible because no user credentials are available.");

        authenticate(callback, c.userName, c.password);
	}

    protected void authenticate(final LoginCallback callback, final String userName, final String password) {
        if (callback != null) {
            callback.onLoginStarted();
        }
        try {
            if (!userName.equals(getSettings().getUserName())) {
                invalidateCache();
            }
            getApp().authenticate(null);
            getSettings().setAccountType(ACCOUNT_TYPE);
            getSettings().setUserName(userName);
            getSettings().setPassword(password);


            final RequestCallback<ReaderHashRequest, String> a =
                    new RequestCallback<ReaderHashRequest, String>() {
                        @Override
                public void onRequestProcessed(ReaderHashRequest request, String result, Throwable error) {
                    final RequestCallback<ReaderTokenRequest, String> c =
                            new RequestCallback<ReaderTokenRequest, String>() {
                                @Override
                                public void onRequestProcessed(ReaderTokenRequest request, String result, Throwable error) {
                                    onAuthenticationFinished(callback, error, result);
                                }
                            };
                    try {
                        getApp().getRequestHandler().send(new ReaderTokenRequest(userName, password), c);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            getApp().getRequestHandler().send(new ReaderHashRequest(userName, password), a);
        } catch (Throwable t) {
            fireOnLoginFailed(callback, t);
        }
    }

    /*
     * prompt user credentials
     */

	@Override
	public void promptAccount(ActionContext actionContext, final LoginCallback callback) {
        super.promptAccount(actionContext, callback);
        
        final LoginDialog.Listener l = new LoginDialog.Listener() {
            @Override
            public void onLoginClicked(String userName, String password) {
                authenticate(callback, userName, password);
            }
        };
        promptDialog = new LoginDialog(actionContext.getActivity(), l, getSettings().getUserName(), getSettings().getPassword());
        promptDialog.show();
	}

    /*
     * inner classes
     */

    static class Credentials {
        public final String userName;
        public final String password;

        public Credentials(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }
    }
}
