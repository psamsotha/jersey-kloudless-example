package com.example.jersey.resource;

import com.example.jersey.auth.AccessTokenManager;
import org.glassfish.jersey.client.oauth2.TokenResult;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;


@Path("tokens")
public class TokensResource {

    @Inject
    private AccessTokenManager tokenManager;

    @GET
    @Path("dump")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Map<String, TokenResult>> dumpTokens() {
        return tokenManager.getTokens();
    }


    @POST
    @Path("clear")
    public void clearTokens() {
        tokenManager.clearTokens();
    }

}
