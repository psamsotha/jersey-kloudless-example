package com.example.jersey.auth;

import org.glassfish.hk2.utilities.binding.AbstractBinder;


public class AuthBinder extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new OAuthFlowManager()).to(OAuthFlowManager.class);
        bind(new AccessTokenManager()).to(AccessTokenManager.class);
    }
}
