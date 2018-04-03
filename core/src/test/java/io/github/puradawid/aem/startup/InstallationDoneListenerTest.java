package io.github.puradawid.aem.startup;

import org.apache.sling.launchpad.api.StartupMode;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class InstallationDoneListenerTest {

    private InstallationDoneListener cut;

    @Before
    public void init() {
        cut = new InstallationDoneListener();
    }

    @Test
    public void shouldReturnNotDoneWhenNoMethodInvoked() {
        assertThat(cut.getStatus(), is(InstallationStatusProvider.Status.PENDING));
    }

    @Test
    public void shouldReturnDoneWhenFinishedInvoked() {
        cut.startupFinished(StartupMode.INSTALL);
        assertThat(cut.getStatus(), is(InstallationStatusProvider.Status.DONE));
    }

    @Test
    public void shouldReturnDoneWhenInformedAboutRightStatus() {
        cut.inform(StartupMode.INSTALL, true);
        assertThat(cut.getStatus(), is(InstallationStatusProvider.Status.DONE));
    }

    @Test
    public void shouldReturnDoneWhenInformedAboutNotDoneStatus() {
        cut.inform(StartupMode.INSTALL, false);
        assertThat(cut.getStatus(), is(InstallationStatusProvider.Status.PENDING));
    }
}