package com.example.jersey.resource;

import com.example.jersey.Constants;
import com.example.jersey.auth.AccessTokenManager;
import com.example.jersey.auth.OAuthFlowManager;
import org.glassfish.jersey.client.oauth2.ClientIdentifier;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;
import org.glassfish.jersey.client.oauth2.OAuth2CodeGrantFlow;
import org.glassfish.jersey.client.oauth2.TokenResult;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;


@Path("oauth2")
public class AuthorizationResource {


    @Context
    private HttpServletRequest request;

    @Context
    private Configuration config;

    @Inject
    private OAuthFlowManager flowManager;

    @Inject
    private AccessTokenManager tokenManager;


    @POST
    @Path("flow")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response startFlow(@FormParam("scope") String scope) {
        if (scope == null || scope.trim().isEmpty()) {
            return Response.noContent().build();
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            session = request.getSession();
        }

        final String sessionId = session.getId();
        session.setAttribute("scope", scope);

        final OAuth2CodeGrantFlow oauthFlow = getOAuthFlow(scope);
        this.flowManager.addFlow(sessionId, scope, oauthFlow);

        final String redirectUri = oauthFlow.start();
        return Response.created(URI.create(redirectUri)).build();
    }

    private OAuth2CodeGrantFlow getOAuthFlow(String scope) {
        final OAuth2CodeGrantFlow flow = OAuth2ClientSupport
                .authorizationCodeGrantFlowBuilder(
                        (ClientIdentifier) config.getProperty(Constants.KEY_CLIENT_IDENTIFIER),
                        (String) config.getProperty(Constants.KEY_KLOUDLESS_AUTHORIZATION_URI),
                        (String) config.getProperty(Constants.KEY_KLOUDLESS_TOKEN_URI))
                .redirectUri((String) config.getProperty(Constants.KEY_APP_REDIRECT_URI))
                .property(OAuth2CodeGrantFlow.Phase.AUTHORIZATION, "scope", scope)
                .build();
        return flow;
    }

    @GET
    @Path("authorize")
    public Response ok(@QueryParam("code") String code,
                       @QueryParam("state") String state) {

        final HttpSession session = request.getSession(false);
        if (session == null) {
            throwProblemException();
        }

        final String sessionId = session.getId();
        final String scope = (String) session.getAttribute("scope");
        session.removeAttribute("scope");

        final OAuth2CodeGrantFlow flow = this.flowManager.getFlow(sessionId, scope);
        if (flow == null) {
            throwProblemException();
        }

        final TokenResult tokenResult = flow.finish(code, state);
        this.tokenManager.addToken(sessionId, scope, tokenResult);

        // Not using flow for resource requests. We will use
        // the Kloudless SDK.
        this.flowManager.removeFlow(sessionId, scope);

        return Response.seeOther(URI.create("/")).build();
    }

    private void throwProblemException() {
        throw new IllegalStateException("Problem occurred during authorization");
    }
}
