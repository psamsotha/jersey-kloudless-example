package com.example.jersey.resource;


import com.example.jersey.auth.AccessTokenManager;
import org.glassfish.jersey.client.oauth2.TokenResult;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Path("init")
public class AppInitResource {

    @Context
    private HttpServletRequest request;

    @Inject
    private AccessTokenManager tokenManager;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AppInitialization initApp() {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            request.getSession();
            return new AppInitialization();
        }
        final String sessionId = session.getId();
        Map<String, TokenResult> tokens = tokenManager.getTokensForSession(sessionId);
        if (tokens == null) {
            tokenManager.startNewSession(sessionId);
            tokens = new HashMap<>();
            tokenManager.startNewSession(sessionId, tokens);
        }
        AppInitialization appInit = new AppInitialization();
        for (String scope: tokens.keySet()) {
            appInit.addAccessibleService(scope);
        }
        return appInit;
    }


    public static class AppInitialization {

        public List<String> accessibleServices = new ArrayList<>();


        public List<String> getAccessibleServices() {
            return accessibleServices;
        }

        public AppInitialization addAccessibleService(String service) {
            accessibleServices.add(service);
            return this;
        }
    }
}
