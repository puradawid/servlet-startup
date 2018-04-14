package io.github.puradawid.aem.startup.terminal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.function.Supplier;

class InstallationProcess implements Supplier<Boolean> {

    private final Log LOGGER = LogFactory.getLog(InstallationProcess.class);

    private final ZipPackage zip;
    private final Communication connection;

    public InstallationProcess(ZipPackage zip, Communication connection) {
        this.zip = zip;
        this.connection = connection;
    }

    @Override
    public Boolean get() {
        if (zip.validate() && connection.established()) {
            waitUntilInstanceIsWarm(10);
            int number = connection.pendingPackages();
            LOGGER.debug("There are " + number + " installing now");
            connection.install(zip);
            while (connection.pendingPackages() > number) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private void waitUntilInstanceIsWarm(int maxAttempts) {
        int counter = 0;
        while (!connection.established() && !connection.startedUp()) {
            try {
                counter++;
                Thread.sleep(1000);
                if (counter == maxAttempts) {
                    throw new RuntimeException("Server is not responding");
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}