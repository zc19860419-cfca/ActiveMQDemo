package com.activemq.broker;

import com.activemq.utils.KeyStoreUtils;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.SslContext;
import org.apache.activemq.transport.tcp.SslTransportFactory;
import org.apache.activemq.transport.tcp.SslTransportServer;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.URI;
import java.security.SecureRandom;

/**
 * @author zhangchong
 * @CodeReviewer zhangqingan
 * @Description
 */
public class BrokerWithSslTransportServer {
    public static void main(String[] args)  throws Exception{
        final BrokerWithSslTransportServer broker = new BrokerWithSslTransportServer();
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

            SslContext sslContext = buildSslContext();
            broker.setSslContext(sslContext);
            //添加连接协议，地址
            SslTransportFactory transportFactory = new SslTransportFactory();
            final String location = "https://192.168.184.134:9088?transport.verifyHostName=false";
            SslTransportServer server = (SslTransportServer)transportFactory.doBind(new URI(location));
            broker.addConnector(server);
            broker.start();
        } finally {
            IOUtils.closeQuietly(keystore);
        }
    }

    private SslContext buildSslContext() throws Exception {

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


}
