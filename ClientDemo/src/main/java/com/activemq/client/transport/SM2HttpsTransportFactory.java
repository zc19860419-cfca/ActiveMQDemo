package com.activemq.client.transport;

import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.TransportLoggerFactory;
import org.apache.activemq.transport.TransportServer;
import org.apache.activemq.transport.http.HttpClientTransport;
import org.apache.activemq.transport.http.HttpInactivityMonitor;
import org.apache.activemq.transport.http.HttpTransportFactory;
import org.apache.activemq.transport.https.HttpsClientTransport;
import org.apache.activemq.util.IntrospectionSupport;
import org.apache.activemq.util.URISupport;
import org.apache.activemq.wireformat.WireFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangchong
 * @CodeReviewer zhangqingan
 * @Description
 */
public class SM2HttpsTransportFactory extends HttpTransportFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SM2HttpsTransportFactory.class);
    public TransportServer doBind(String brokerId, URI location) throws IOException {
        return doBind(location);
    }

    @Override
    protected Transport createTransport(URI location, WireFormat wf) throws IOException {
        // need to remove options from uri
        try {
            URI uri = URISupport.removeQuery(location);

            Map<String, String> options = new HashMap<>(URISupport.parseParameters(location));
            Map<String, Object> transportOptions = IntrospectionSupport.extractProperties(options, "transport.");
            boolean verifyHostName = true;
            if (transportOptions.containsKey("verifyHostName")) {
                verifyHostName = Boolean.parseBoolean(transportOptions.get("verifyHostName").toString());
            }

            HttpsClientTransport clientTransport = new HttpsClientTransport(asTextWireFormat(wf), uri);
            clientTransport.setVerifyHostName(verifyHostName);
            return clientTransport;
        } catch (URISyntaxException e) {
            MalformedURLException cause = new MalformedURLException("Error removing query on " + location);
            cause.initCause(e);
            throw cause;
        }
    }

    // TODO Not sure if there is a better way of removing transport.verifyHostName here?
    @Override
    public Transport compositeConfigure(Transport transport, WireFormat format, Map options) {
        options.remove("transport.verifyHostName");
        transport = super.compositeConfigure(transport, format, options);
        HttpClientTransport httpTransport = transport.narrow(HttpClientTransport.class);
        if (httpTransport != null && httpTransport.isTrace()) {
            try {
                transport = TransportLoggerFactory.getInstance().createTransportLogger(transport);
                ((HttpInactivityMonitor) transport).setConnectAttemptTimeout(200000);
            } catch (Throwable e) {
                LOG.error("Could not create TransportLogger object for: " + TransportLoggerFactory.defaultLogWriterName + ", reason: " + e, e);
            }
        }
        boolean useInactivityMonitor = "true".equals(getOption(options, "useInactivityMonitor", "true"));
        if (useInactivityMonitor) {
            transport = new HttpInactivityMonitor(transport);
            ((HttpInactivityMonitor) transport).setConnectAttemptTimeout(200000);
            IntrospectionSupport.setProperties(transport, options);
        }

        return transport;
    }

}
