package com.activemq.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author zhangchong
 * @CodeReviewer zhangqingan
 * @Description
 */
public class KeyStoreUtils {


    public static KeyManager buildKeyManager(String jksKeyFilePath, String password, String keystoreType, String algorithm) throws Exception {
        Args.notEmpty(jksKeyFilePath,"buildKeyManager#jksKeyFilePath");
        Args.notEmpty(password,"buildKeyManager#password");
        Args.notEmpty(keystoreType,"buildKeyManager#keystoreType");
        Args.notEmpty(algorithm,"buildKeyManager#algorithm");
        FileInputStream fis = null;
        try {
            fis = FileUtils.openInputStream(new File(jksKeyFilePath));
            KeyStore keyStore = KeyStore.getInstance(keystoreType);
            keyStore.load(fis, password.toCharArray());
            KeyManagerFactory factory = KeyManagerFactory.getInstance(algorithm);
            factory.init(keyStore, password.toCharArray());
            final KeyManager[] keyManagers = factory.getKeyManagers();
            if (keyManagers.length != 1 || !(keyManagers[0] instanceof KeyManager)) {
                throw new IllegalStateException("Unexpected default key managers:"
                        + Arrays.toString(keyManagers));
            }
            return keyManagers[0];
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    /**
     * 获去信任自签证书的trustManager
     *
     * @param trustCertPath 自签证书文件路径
     * @param type 证书类型
     * @param keyManagerAlgorithm
     * @param trustManagerAlgorithm
     * @return 信任自签证书的trustManager
     * @throws GeneralSecurityException
     */
    public static TrustManager buildTrustManager(String trustCertPath, String type, String keyManagerAlgorithm, String trustManagerAlgorithm) throws Exception {
        Args.notEmpty(trustCertPath,"buildTrustManager#trustCertPath");
        Args.notEmpty(type,"buildTrustManager#type");
        FileInputStream in = null;
        try {
            in = FileUtils.openInputStream(new File(trustCertPath));
            CertificateFactory certificateFactory = CertificateFactory.getInstance(type);
            //通过证书工厂得到自签证书对象集合
            Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
            if (certificates.isEmpty()) {
                throw new Exception("buildTrustManager : " +
                        "expected non-empty set of trusted certificates");
            }
            //为证书设置一个keyStore,就是trustStore
            final char[] password = "password".toCharArray();
            KeyStore keyStore = newEmptyKeyStore(password);
            int index = 0;
            //将证书放入keystore中
            for (Certificate certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificate);
            }
            //使用包含自签证书信息的keyStore去构建一个X509TrustManager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    keyManagerAlgorithm);
            keyManagerFactory.init(keyStore, password);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    trustManagerAlgorithm);
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            return trustManagers[0];
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private static KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
