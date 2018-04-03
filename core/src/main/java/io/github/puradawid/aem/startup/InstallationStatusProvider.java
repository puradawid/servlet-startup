package io.github.puradawid.aem.startup;

public interface InstallationStatusProvider {
    Status getStatus();

    enum Status {
        PENDING, DONE;
    }
}