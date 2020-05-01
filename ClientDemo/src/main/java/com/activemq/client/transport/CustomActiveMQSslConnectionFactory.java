package com.activemq.client.transport;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.broker.SslContext;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.TransportFactory;
import org.apache.activemq.util.JMSExceptionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Properties;

/**
 * @author zhangchong
 * @CodeReviewer zhangqingan
 * @Description
 */
public class CustomActiveMQSslConnectionFactory extends ActiveMQSslConnectionFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CustomActiveMQSslConnectionFactory.class);
    private final String protocol = "GMTLSv1.1";
    private final String provider = "CfcaJSSE";

    public CustomActiveMQSslConnectionFactory() {
    }

    public CustomActiveMQSslConnectionFactory(String brokerURL) {
        super(brokerURL);
    }

    public CustomActiveMQSslConnectionFactory(URI brokerURL) {
        super(brokerURL);
    }


    /**
     * @return Returns the Connection.
     */
    @Override
    public Connection createConnection() throws JMSException {
        return createActiveMQConnection();
    }

    @Override
    protected ActiveMQConnection createActiveMQConnection() throws JMSException {
        return createActiveMQConnection(userName, password);
    }

    /**
     * Overriding to make special considerations for SSL connections. If we are
     * not using SSL, the superclass's method is called. If we are using SSL, an
     * SslConnectionFactory is used and it is given the needed key and trust
     * managers.
     *
     * @author sepandm@gmail.com
     */
    @Override
    protected Transport createTransport() throws JMSException {
        SslContext existing = SslContext.getCurrentSslContext();
        try {
            if (keyStore != null || trustStore != null) {
                keyManager = createKeyManager();
                trustManager = createTrustManager();
            }
            if (keyManager != null || trustManager != null) {
                SSLContext context = SSLContext.getInstance(protocol);
                context.init(keyManager, trustManager, new SecureRandom());
                PreferredCipherSuiteSSLSocketFactory ssf = new PreferredCipherSuiteSSLSocketFactory(context.getSocketFactory());
                ssf.setEnabledCipherSuites(new String[] { "TLS_SM2PKEA_SM2DSA_WITH_SM4_CBC_SM3" });

                final SM2SslContext sslContext = new SM2SslContext(keyManager, trustManager, secureRandom, ssf);
                sslContext.setProtocol(protocol);
                sslContext.setProvider(provider);
                SslContext.setCurrentSslContext(sslContext);
            }
            final Transport connect = connect();
            LOG.info("createTransport finish");
            return connect;
        } catch (Exception e) {
            throw JMSExceptionSupport.create("Could not create Transport. Reason: " + e, e);
        } finally {
            SslContext.setCurrentSslContext(existing);
        }
    }

    private Transport connect() throws JMSException{
        try {
            URI connectBrokerUL = brokerURL;
            String scheme = brokerURL.getScheme();
            if (scheme == null) {
                throw new IOException("Transport not scheme specified: [" + brokerURL + "]");
            }
            if (scheme.equals("auto")) {
                connectBrokerUL = new URI(brokerURL.toString().replace("auto", "tcp"));
            } else if (scheme.equals("auto+ssl")) {
                connectBrokerUL = new URI(brokerURL.toString().replace("auto+ssl", "ssl"));
            } else if (scheme.equals("auto+nio")) {
                connectBrokerUL = new URI(brokerURL.toString().replace("auto+nio", "nio"));
            } else if (scheme.equals("auto+nio+ssl")) {
                connectBrokerUL = new URI(brokerURL.toString().replace("auto+nio+ssl", "nio+ssl"));
            }

            TransportFactory tf = new SM2HttpsTransportFactory();
            return tf.doConnect(connectBrokerUL);
        } catch (Exception e) {
            throw JMSExceptionSupport.create("Could not create Transport. Reason: " + e, e);
        }
    }

    @Override
    public void populateProperties(Properties props) {
        super.populateProperties(props);
    }
}
