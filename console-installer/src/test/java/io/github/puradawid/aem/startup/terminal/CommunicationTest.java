package io.github.puradawid.aem.startup.terminal;

import com.github.paweladamski.httpclientmock.HttpClientMock;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CommunicationTest {

    private static Communication.User user = new Communication.User("admin", "admin");

    @Test
    public void testCorrectReading() {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://mainframe:4502/").doReturn("OK");
        Communication communication = new Communication(
            new Communication.Instance("mainframe", 4502, false), user, httpClientMock);

        boolean result = communication.established();

        httpClientMock.verify().get("http://mainframe:4502/").called();
        assertTrue(result);
    }

    @Test
    public void testEstablishGoesWrong() {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://mainframe:4502/")
            .doThrowException(new IOException("Cannot connect to host"));
        Communication communication = new Communication(
            new Communication.Instance("mainframe", 4502, false), user, httpClientMock);

        boolean result = communication.established();

        httpClientMock.verify().get("http://mainframe:4502/").called();
        assertFalse(result);
    }

    @Test
    public void testEstablishWithWrongUser() {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://mainframe:4502/").doReturnStatus(403);
        Communication communication = new Communication(
            new Communication.Instance("mainframe", 4502, false), user, httpClientMock);

        boolean result = communication.established();

        httpClientMock.verify().get("http://mainframe:4502/").called();
        assertFalse(result);
    }

    @Test
    public void testInstallingFile() {
        HttpClientMock httpClientMock = new HttpClientMock();
        Communication communication = new Communication(
            new Communication.Instance("mainframe", 4502, false), user, httpClientMock);

        communication.install(new ZipPackage(""));

        httpClientMock.verify().post("http://mainframe:4502/crx/packmgr/service.jsp").withBody(
            CoreMatchers.containsString("force")).called();
    }


    @Test
    public void testPending() {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://mainframe:4502/crx/packmgr/installstatus.jsp")
            .doReturn("{ \"status\" : { \"itemCount\" : 1 } }").doReturnStatus(200);
        Communication communication = new Communication(
            new Communication.Instance("mainframe", 4502, false), user, httpClientMock);

        assertThat(communication.pendingPackages(), is(1));
    }

    @Test
    public void testServletStartup() {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://mainframe:4502/bin/startup").doReturn("Done").doReturnStatus(200);
        Communication communication = new Communication(
            new Communication.Instance("mainframe", 4502, false), user, httpClientMock);

        assertThat(communication.startedUp(), is(true));
    }
}