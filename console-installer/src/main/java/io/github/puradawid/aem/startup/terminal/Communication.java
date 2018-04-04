package io.github.puradawid.aem.startup.terminal;

import com.google.gson.JsonParser;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class Communication {

    private final Instance instance;
    private final HttpClient client;
    private final User user;

     Communication(Instance instance, User user, HttpClient client) {
        this.instance = instance;
        this.client = client;
        this.user = user;
    }

     boolean established() {
        try {
            Optional<String> response = makeGetRequest("/");
            return response.isPresent();
        } catch (IOException ex) {
            return false;
        }
    }

     int pendingPackages() {
        try {
            Optional<String> packages = makeGetRequest("/crx/packmgr/installstatus.jsp");
            return new JsonParser()
                .parse(packages.get())
                .getAsJsonObject()
                .getAsJsonObject("status")
                .getAsJsonPrimitive("itemCount")
                .getAsInt();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

     void install(ZipPackage zipPackage) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("force", "true");
            params.put("install", "true");
            if (zipPackage.validate()) {
                params.put("file", zipPackage.file());
                params.put("name", zipPackage.name());
            }
            makePostRequest("/crx/packmgr/service.jsp", params);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Optional<String> makeGetRequest(String path) throws IOException {
        HttpGet get = new HttpGet(instance.translateToUri() + path);
        HttpResponse response = client.execute(get, buildContextBasedOnUser());

        if (response.getStatusLine().getStatusCode() == 200) {
            return Optional.ofNullable(EntityUtils.toString(response.getEntity()));
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> makePostRequest(String path, Map<String, Object> params) throws IOException {
        HttpPost post = new HttpPost(instance.translateToUri() + path);

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
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user.name, user.password));

        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(provider);

        return context;
    }

    static class Instance {

        private final String host;
        private final int port;
        private final boolean secure;

        Instance(String host, int port, boolean secure) {
            this.host = host;
            this.port = port;
            this.secure = secure;
        }

        String translateToUri() {
            return (secure ? "https://" : "http://") + host + ":" + port;
        }
    }

    static class User {
        private final String name;
        private final String password;

         User(String name, String password) {
            this.name = name;
            this.password = password;
        }
    }
}