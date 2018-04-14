package io.github.puradawid.aem.startup.terminal;

import com.google.gson.JsonParser;

import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class Communication {

    private final ApacheHttpClientFacade httpClientFacade;

     Communication(Instance instance, User user, HttpClient client) {
        this.httpClientFacade = new ApacheHttpClientFacade(client, instance.translateToUri(), user.name, user.password);
    }

     boolean established() {
        try {
            Optional<String> response = httpClientFacade.makeGetRequest("/");
            return response.isPresent();
        } catch (IOException ex) {
            return false;
        }
    }

     int pendingPackages() {
        try {
            Optional<String> packages = httpClientFacade.makeGetRequest("/crx/packmgr/installstatus.jsp");
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
            httpClientFacade.makePostRequest("/crx/packmgr/service.jsp", params);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    boolean startedUp() {
        try {
            return httpClientFacade.makeGetRequest("/bin/startup").map(x -> x.equals("Done")).orElse(false);
        } catch (IOException ex) {
            return false;
        }
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

        private String translateToUri() {
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