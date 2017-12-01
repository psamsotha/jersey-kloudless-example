package com.example.jersey.auth;


import org.glassfish.jersey.client.oauth2.OAuth2CodeGrantFlow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class OAuthFlowManager {

    private final Map<FlowKey, OAuth2CodeGrantFlow> oauthFlows = new ConcurrentHashMap<>();


    public OAuth2CodeGrantFlow getFlow(String sessionId, String scope) {
        return this.getFlow(new FlowKey(sessionId, scope));
    }

    public OAuth2CodeGrantFlow getFlow(FlowKey flowKey) {
        return this.oauthFlows.get(flowKey);
    }


    public void addFlow(String sessionId, String scope, OAuth2CodeGrantFlow oauthFlow) {
        this.addFlow(new FlowKey(sessionId, scope), oauthFlow);
    }

    public void addFlow(FlowKey flowKey, OAuth2CodeGrantFlow oauthFlow) {
        this.oauthFlows.put(flowKey, oauthFlow);
    }

    public OAuth2CodeGrantFlow removeFlow(String sessionId, String scope) {
        return oauthFlows.remove(new FlowKey(sessionId, scope));
    }


    public static class FlowKey {

        private String sessionId;
        private String scope;

        public FlowKey(String sessionId, String scope) {
            this.sessionId = sessionId;
            this.scope = scope;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getScope() {
            return scope;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FlowKey flowKey = (FlowKey) o;

            if (sessionId != null ? !sessionId.equals(flowKey.sessionId) : flowKey.sessionId != null) return false;
            return scope != null ? scope.equals(flowKey.scope) : flowKey.scope == null;
        }

        @Override
        public int hashCode() {
            int result = sessionId != null ? sessionId.hashCode() : 0;
            result = 31 * result + (scope != null ? scope.hashCode() : 0);
            return result;
        }
    }
}
