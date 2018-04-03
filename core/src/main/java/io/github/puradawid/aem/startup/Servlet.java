package io.github.puradawid.aem.startup;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

@SlingServlet(paths = "/bin/startup")
public class Servlet extends SlingSafeMethodsServlet {

    private static final Map<InstallationStatusProvider.Status, String> STATUS_TO_RESPONSE = new HashMap<>();

    static {
        STATUS_TO_RESPONSE.put(InstallationStatusProvider.Status.DONE, "Done");
        STATUS_TO_RESPONSE.put(InstallationStatusProvider.Status.PENDING, "Pending");
    }

    @Reference
    private InstallationStatusProvider listener;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
        throws ServletException, IOException {
        response.getOutputStream().print(STATUS_TO_RESPONSE.get(listener.getStatus()));
        response.setStatus(200);
    }
}