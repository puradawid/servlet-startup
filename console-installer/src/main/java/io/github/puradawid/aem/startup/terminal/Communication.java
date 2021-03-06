package io.github.puradawid.aem.startup.terminal;

import com.google.gson.JsonParser;

import org.apache.http.client.HttpClient;
import org.apache.log4j.LogManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.github.puradawid.aem.startup.terminal.log.ModernLogFacade;

class Communication {

    private static final ModernLogFacade LOGGER =
        new ModernLogFacade(LogManager.getLogger(Communication.class));

    private final ApacheHttpClientFacade httpClientFacade;

    Communication(Instance instance, User user, HttpClient client) {
        LOGGER.debug(() ->
            String.format(
                "Creating ApacheHttpClientFacade with params: URL: %s; name: %s, password: %s",
                instance.translateToUri(),
                user.name,
                user.password));
        this.httpClientFacade = new ApacheHttpClientFacade(client, instance.translateToUri(), user.name, user.password);
    }

     boolean established() {
         LOGGER.debug(() -> "Calling established function");
         Optional<String> response = httpClientFacade.makeGetRequest("/");
         LOGGER.debug(() -> "Established: " + response.get());
         return response.isPresent();
    }

     int pendingPackages() {
         Optional<String> packages = httpClientFacade.makeGetRequest("/crx/packmgr/installstatus.jsp");
         LOGGER.debug(() -> "Response of pendingPackages: " + packages.get());
         return new JsonParser()
             .parse(packages.get())
             .getAsJsonObject()
             .getAsJsonObject("status")
             .getAsJsonPrimitive("itemCount")
             .getAsInt();
    }

     void install(ZipPackage zipPackage) {
        Map<String, Object> params = new HashMap<>();
        params.put("force", "true");
        params.put("install", "true");
        if (zipPackage.validate()) {
            params.put("file", zipPackage.file());
            params.put("name", zipPackage.name());
        }
        httpClientFacade.makePostRequest("/crx/packmgr/service.jsp", params);
    }

    boolean startedUp() {
        return httpClientFacade.makeGetRequest("/bin/startup").map(x -> x.equals("Done")).orElse(false);
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