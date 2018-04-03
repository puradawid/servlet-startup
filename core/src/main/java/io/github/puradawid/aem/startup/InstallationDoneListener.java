package io.github.puradawid.aem.startup;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.launchpad.api.StartupListener;
import org.apache.sling.launchpad.api.StartupMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Service
public class InstallationDoneListener implements StartupListener, InstallationStatusProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstallationDoneListener.class);

    private boolean installed = false;

    @Override
    public void inform(StartupMode startupMode, boolean finished) {
        LOGGER.info("Startup Mode:" + startupMode.toString() + " boolean: " + finished);
        if (finished) {
            installed = true;
        }
    }

    @Override
    public void startupFinished(StartupMode startupMode) {
        LOGGER.info("Startup finished: " + startupMode.toString());
        installed = true;
    }

    @Override
    public void startupProgress(float v) {
        LOGGER.info("Startup progress: " + v);
    }

    @Override
    public Status getStatus() {
        return installed ? Status.DONE : Status.PENDING;
    }
}