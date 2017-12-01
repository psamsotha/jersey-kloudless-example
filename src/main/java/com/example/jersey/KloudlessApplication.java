package com.example.jersey;

import com.example.jersey.auth.AuthBinder;
import org.glassfish.jersey.client.oauth2.ClientIdentifier;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;

import java.util.Properties;


public class KloudlessApplication extends ResourceConfig {

    public KloudlessApplication() {
        packages("com.example.jersey");

        register(MultiPartFeature.class);

        register(new AuthBinder());

        loadPropertiesFile(this);
        loadSystemEnvVars(this);
        loadOauthClientIdentifier(this);
    }

    private void loadPropertiesFile(ResourceConfig config) {
        final Properties properties = new Properties();
        try {
            properties.load(KloudlessApplication.class.getClassLoader()
                    .getResourceAsStream("app.properties"));
        } catch (IOException ex) {
            throw new RuntimeException("Cannot load properties file app.properties");
        }
        properties.stringPropertyNames().forEach((key) -> {
            config.property(key, properties.getProperty(key));
        });
    }

    private void loadSystemEnvVars(ResourceConfig config) {
        System.getenv().forEach((key, value) -> {
            config.property(key, value);
        });
    }

    private void loadOauthClientIdentifier(ResourceConfig config) {
        Object clientId = config.getProperty("CLIENT_ID");
        if (clientId == null) {
            throw new IllegalStateException("CLIENT_ID env var must be set.");
        }
        Object clientSecret = config.getProperty("CLIENT_SECRET");
        if (clientSecret == null) {
            throw new IllegalStateException("CLIENT_SECRET env var must be set.");
        }
        config.property(Constants.KEY_CLIENT_IDENTIFIER,
                new ClientIdentifier((String) clientId, (String) clientSecret));
    }
}
