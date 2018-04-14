package io.github.puradawid.aem.startup.terminal;

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

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Map;

class ApacheHttpClientFacade {

    HttpClient client;
    private String host;
    private String userName;
    private String userPassword;

    public ApacheHttpClientFacade(HttpClient client, String host, String userName,
                                  String userPassword) {
        this.client = client;
        this.host = host;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    Optional<String> makeGetRequest(String path) throws IOException {
        HttpGet get = new HttpGet(host + path);
        HttpResponse response = client.execute(get, buildContextBasedOnUser());

        if (response.getStatusLine().getStatusCode() == 200) {
            return Optional.ofNullable(EntityUtils.toString(response.getEntity()));
        } else {
            return Optional.empty();
        }
    }

    Optional<String> makePostRequest(String path, Map<String, Object> params) throws IOException {
        HttpPost post = new HttpPost(host + path);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (param.getValue() instanceof String) {
                builder.addTextBody(param.getKey(), (String) param.getValue());
            } else if (param.getValue() instanceof File) {
                builder.addBinaryBody(param.getKey(), (File) param.getValue());
            }
        }

        post.setEntity(builder.build());

        HttpResponse response = client.execute(post, buildContextBasedOnUser());

        if (response.getStatusLine().getStatusCode() == 200) {
            return Optional.ofNullable(response.getEntity().toString());
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