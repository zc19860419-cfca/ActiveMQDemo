package com.activemq.client;

import cfca.sadk.org.bouncycastle.jce.provider.BouncyCastleProvider;
import cfca.sadk.tls.sun.security.ssl.JSSEProvider;
import com.activemq.client.transport.CustomActiveMQSslConnectionFactory;
import com.activemq.utils.KeyStoreUtils;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.jms.Connection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

/**
 * @author zhangchong
 * @CodeReviewer zhangqingan
 * @Description
 */
public class ConnectionHelper {

    private static final Provider cfcaTLS = new JSSEProvider();

    public static Connection createConnection(String brokerURL) throws Exception {
        //1、创建工厂连接对象，需要制定ip和端口号

//        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.166.24:61616");
//        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("https://127.0.0.1:9088");
//        ActiveMQSslConnectionFactory connectionFactory = new ActiveMQSslConnectionFactory("https://127.0.0.1:9088");
        ActiveMQSslConnectionFactory connectionFactory = new ActiveMQSslConnectionFactory(brokerURL);
        final String password = "cfca1234";
        final String keystoreType = "JKS";
        final String algorithm = "SunX509";
        final String keyStoreFile = "TestData/client_keyStore.jks";
        final String trustCertPath = "TestData/broker_trust.cer";
        KeyManager km = KeyStoreUtils.buildKeyManager(keyStoreFile, password, keystoreType, algorithm);
        KeyManager[] keyManagers = new KeyManager[]{km};
        final TrustManager tm = KeyStoreUtils.buildTrustManager(trustCertPath, "X.509", KeyManagerFactory.getDefaultAlgorithm(), TrustManagerFactory.getDefaultAlgorithm());
        TrustManager[] trustManagers = new TrustManager[]{tm};
        SecureRandom random = new SecureRandom();
        connectionFactory.setKeyAndTrustManagers(keyManagers, trustManagers, random);
        //2、使用连接工厂创建一个连接对象
        Connection connection = connectionFactory.createConnection();
        //3、开启连接
        connection.start();
        return connection;
    }


    public static Connection createSM2Connection(String brokerUrl) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new JSSEProvider());
        //1、创建工厂连接对象，需要制定ip和端口号
        CustomActiveMQSslConnectionFactory connectionFactory = new CustomActiveMQSslConnectionFactory(brokerUrl);
        final String keyStoreType = "BKS";
        final String algorithm = "GMTX509";
        final String keyStoreFile = "TestData/gateway/SM2ClientYang-cfca1234.keystore";
        final String keyStorePass = "cfca1234";
        final String trustStoreFile = "TestData/gateway/SM2TrustYang_11111111.keystore";
        final String trustStorePass = "11111111";

        FileInputStream fis = null;
        try {
            fis = FileUtils.openInputStream(new File(keyStoreFile));
            KeyManagerFactory factory = KeyManagerFactory.getInstance(algorithm, cfcaTLS);
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(fis, keyStorePass.toCharArray());
            factory.init(keyStore, keyStorePass.toCharArray());
            final KeyManager[] keyManagers = factory.getKeyManagers();

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm, cfcaTLS);
            KeyStore tks = KeyStore.getInstance(keyStoreType);
            tks.load(new FileInputStream(trustStoreFile), trustStorePass.toCharArray());
            tmf.init(tks);
            final TrustManager[] trustManagers = tmf.getTrustManagers();

            SecureRandom random = new SecureRandom();

            connectionFactory.setKeyAndTrustManagers(keyManagers, trustManagers, random);

            //2、使用连接工厂创建一个连接对象
            Connection connection = connectionFactory.createConnection();
            //3、开启连接
            connection.start();
            return connection;
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }
}
