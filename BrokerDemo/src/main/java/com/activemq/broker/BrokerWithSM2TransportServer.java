package com.activemq.broker;

import cfca.sadk.org.bouncycastle.jce.provider.BouncyCastleProvider;
import cfca.sadk.tls.sun.security.ssl.JSSEProvider;
import com.activemq.utils.KeyStoreUtils;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.SslContext;
import org.apache.activemq.transport.tcp.SslTransportFactory;
import org.apache.activemq.transport.tcp.SslTransportServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

/**
 * Hello world!
 */
public class BrokerWithSM2TransportServer {
    private final Provider cfcaTLS = new JSSEProvider();

    static {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new JSSEProvider());
    }

    public static void main(String[] args)  throws Exception{
        final BrokerWithSM2TransportServer broker = new BrokerWithSM2TransportServer();
        broker.run();
    }

    private void run() throws Exception{
        InputStream keystore = null;
        try {
            BrokerService broker = new BrokerService();
            //启用broker的JMX监控功能
            broker.setUseJmx(true);
            //设置broker名字
            broker.setBrokerName("MyBroker");
            //是否使用持久化
            broker.setPersistent(false);

            SslContext sslContext = buildSM2SslContext();
            broker.setSslContext(sslContext);
            //添加连接协议，地址
			//SM2SslTransportFactory transportFactory = new SM2SslTransportFactory(broker);
            SslTransportFactory transportFactory = new SslTransportFactory();
            final String location = "https://192.168.184.134:9088?transport.verifyHostName=false";
            SslTransportServer server = (SslTransportServer)transportFactory.doBind(new URI(location));
            server.setNeedClientAuth(true);

            broker.addConnector(server);
            broker.start();
        } finally {
            IOUtils.closeQuietly(keystore);
        }
    }

    private SslContext initSslContext() throws Exception {
        final String password = "cfca1234";
        final String keystoreType = "JKS";
        final String algorithm = "SunX509";
        final String keyStoreFile = "TestData/broker_keyStore.jks";
        final String trustCertPath = "TestData/broker_trust.cer";

        KeyManager km = KeyStoreUtils.buildKeyManager(keyStoreFile, password, keystoreType, algorithm);
        KeyManager[] keyManagers = new KeyManager[]{km};
        final TrustManager tm = KeyStoreUtils.buildTrustManager(trustCertPath, "X.509", KeyManagerFactory.getDefaultAlgorithm(), TrustManagerFactory.getDefaultAlgorithm());
        TrustManager[] trustManagers = new TrustManager[]{tm};
        SecureRandom random = new SecureRandom();
        return new SslContext(keyManagers, trustManagers, random);
    }

    private SslContext buildSM2SslContext() throws Exception {

        final String keyStoreType = "BKS";
        final String algorithm = "GMTX509";
        final String keyStoreFile = "TestData/gateway/SM2ClientYang-cfca1234.keystore";
        final String keyStorePass = "cfca1234";
        final String trustStoreFile = "TestData/gateway/SM2TrustYang_11111111.keystore";
        final String trustStorePass = "11111111";
        final String protocol = "GMTLSv1.1";

        FileInputStream fis = null;
        try {
            fis = FileUtils.openInputStream(new File(keyStoreFile));
            KeyManagerFactory factory = KeyManagerFactory.getInstance(algorithm, cfcaTLS);
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(fis, keyStorePass.toCharArray());
            factory.init(keyStore, keyStorePass.toCharArray());
            final KeyManager[] keyManagers = factory.getKeyManagers();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm, cfcaTLS);
            KeyStore tks = KeyStore.getInstance(keyStoreType);
            tks.load(new FileInputStream(trustStoreFile), trustStorePass.toCharArray());
            trustManagerFactory.init(tks);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            SecureRandom random = new SecureRandom();
            final SslContext sslContext = new SslContext(keyManagers, trustManagers, random);
            sslContext.setProvider("CfcaJSSE");
            sslContext.setProtocol(protocol);
            return sslContext;

        } finally {
            IOUtils.closeQuietly(fis);
        }
    }


}
