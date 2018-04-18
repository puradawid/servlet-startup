package io.github.puradawid.aem.startup.terminal;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Map;

import io.github.puradawid.aem.startup.terminal.log.ModernLogFacade;

class ApacheHttpClientFacade {

    private static final ModernLogFacade LOGGER = new ModernLogFacade(Logger.getLogger(ApacheHttpClientFacade.class));

    private final HttpClient client;
    private final String host;
    private final String userName;
    private final String userPassword;

    public ApacheHttpClientFacade(
        HttpClient client, String host, String userName, String userPassword) {
        this.client = client;
        this.host = host;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    Optional<String> makeGetRequest(String path) {
        HttpGet get = new HttpGet(host + path);

        HttpResponse response;
        try {
            response = client.execute(get, buildContextBasedOnUser());
            LOGGER.debug(() -> String.format("GET %s%s", host, path));
        } catch (IOException ex) {
            LOGGER.error("Executing GET request failed", ex);
            return Optional.empty();
        }

        if (response.getStatusLine().getStatusCode() == 200) {
            try {
                Optional<String> result =
                    Optional.ofNullable(EntityUtils.toString(response.getEntity()));
                LOGGER.debug(() -> "Response: " + result.orElse(""));
                return result;
            } catch (IOException ex) {
                LOGGER.error("Reading response entity failed", ex);
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    Optional<String> makePostRequest(String path, Map<String, Object> params) {
        HttpPost post = new HttpPost(host + path);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (param.getValue() instanceof String) {
                builder.addTextBody(param.getKey(), (String) param.getValue());
            } else if (param.getValue() instanceof File) {
                builder.addBinaryBody(param.getKey(), (File) param.getValue());
            }
        }

        LOGGER.debug(() -> String.format("POST %s%s", host, path));
        post.setEntity(builder.build());

        HttpResponse response;
        String responseText;
        try {
            response = client.execute(post, buildContextBasedOnUser());
            HttpEntity entity = response.getEntity();
            responseText = EntityUtils.toString(entity);
            EntityUtils.consumeQuietly(entity);
        } catch (IOException ex) {
            LOGGER.error("Executing POST failed", ex);
            return Optional.empty();
        }

        int statusCode = response.getStatusLine().getStatusCode();
        LOGGER.debug(() -> String.format("Response %s with %d code", responseText, statusCode));

        if (statusCode == 200) {
            return Optional.ofNullable(responseText);
        } else {
            return Optional.empty();
        }
    }

    private HttpContext buildContextBasedOnUser() {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, userPassword));

        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(provider);

        return context;
    }
}