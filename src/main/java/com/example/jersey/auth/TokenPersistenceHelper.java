package com.example.jersey.auth;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.glassfish.jersey.client.oauth2.TokenResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class TokenPersistenceHelper {

    // save tokens to ~/.kloudlessapp/tokens.json
    private static final String DIR = System.getProperty("user.home")
            + File.separator + ".kloudlessapp";
    private static final String FILE = DIR + File.separator + "tokens.json";

    private final ObjectMapper mapper = new ObjectMapper();


    public TokenPersistenceHelper() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(TokenResult.class, new TokenResultDeserializer());
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        try {
            Files.createDirectories(Paths.get(DIR));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void persistTokens(Map<String, Map<String, TokenResult>> tokens) {
        try {
            OutputStream out = new FileOutputStream(FILE);
            mapper.writeValue(out, tokens);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Map<String, TokenResult>> loadTokens() {
        File file = new File(FILE);
        if (file.exists()) {
            try {
                TypeReference<Map<String, Map<String, TokenResult>>> type
                        = new TypeReference<Map<String, Map<String, TokenResult>>>() {};
                return mapper.readValue(file, type);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new HashMap<>();
    }


    private static class TokenResultDeserializer extends JsonDeserializer<TokenResult> {

        @Override
        public TokenResult deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            Map<String, Object> properties = new HashMap<>();

            JsonNode node = (JsonNode) parser.getCodec().readTree(parser).get("allProperties");

            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                JsonNode curr = node.get(field);

                switch (field) {
                    case "scope":
                    case "account_id":
                    case "access_token":
                    case "refresh_token":
                    case "token_type":
                        properties.put(field, curr.asText());
                        break;
                    case "expires_in":
                        properties.put(field, curr.asInt());
                    default:
                        break;
                }
            }

            return new TokenResult(properties);
        }
    }
}
