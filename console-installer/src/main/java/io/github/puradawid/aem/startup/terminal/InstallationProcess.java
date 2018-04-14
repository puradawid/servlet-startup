package io.github.puradawid.aem.startup.terminal;

import java.util.function.Supplier;

class InstallationProcess implements Supplier<Boolean> {

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
            connection.install(zip);

            while (connection.pendingPackages() > number) {
                waitUntilInstanceIsWarm(10);
                try {
                    Thread.sleep(500);
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
                Thread.sleep(500);
                if (counter == maxAttempts) {
                    throw new RuntimeException("Server is not responding");
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}