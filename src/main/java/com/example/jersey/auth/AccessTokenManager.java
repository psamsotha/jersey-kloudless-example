package com.example.jersey.auth;


import org.glassfish.jersey.client.oauth2.TokenResult;

import java.util.HashMap;
import java.util.Map;


public class AccessTokenManager {

    private final TokenPersistenceHelper persistence;
    private Map<String, Map<String, TokenResult>> accessTokens;


    public AccessTokenManager() {
        this.persistence = new TokenPersistenceHelper();
        this.accessTokens = persistence.loadTokens();
    }

    public Map<String, TokenResult> getTokensForSession(String sessionId) {
        return this.accessTokens.get(sessionId);
    }

    public void addToken(String sessionId, String scope, TokenResult token) {
        if (this.accessTokens.get(sessionId) == null) {
            Map<String, TokenResult> tokens = new HashMap<>();
            tokens.put(scope, token);
            this.accessTokens.put(sessionId, tokens);
        } else {
            Map<String, TokenResult> tokens = this.accessTokens.get(sessionId);
            tokens.put(scope, token);
        }
        persistence.persistTokens(this.accessTokens);
    }

    public void clearTokens() {
        this.accessTokens = new HashMap<>();
        this.persistence.persistTokens(this.accessTokens);
    }

    public void startNewSession(String sessionId) {
        this.accessTokens.put(sessionId, new HashMap<>());
    }

    public void startNewSession(String sessionId, Map<String, TokenResult> tokens) {
        this.accessTokens.put(sessionId, tokens);
    }

    public Map<String, Map<String,TokenResult>> getTokens() {
        return this.accessTokens;
    }
}
