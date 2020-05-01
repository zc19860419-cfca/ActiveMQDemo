package com.activemq.client.transport;

import org.apache.activemq.broker.SslContext;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhangchong
 * @CodeReviewer zhangqingan
 * @Description
 */
public class SM2SslContext extends SslContext {

    private static final ThreadLocal<SslContext> current = new ThreadLocal<SslContext>();

    private final SSLSocketFactory sslSocketFactory;

    private SSLContext sslContext;


    public SM2SslContext(KeyManager[] km, TrustManager[] tm, SecureRandom random, SSLSocketFactory sslSocketFactory) {
        if( km!=null ) {
            setKeyManagers(Arrays.asList(km));
        }
        if( tm!=null ) {
            setTrustManagers(Arrays.asList(tm));
        }
        setSecureRandom(random);

        this.sslSocketFactory = sslSocketFactory;
    }

    static public void setCurrentSslContext(SslContext bs) {
        current.set(bs);
    }
    static public SslContext getCurrentSslContext() {
        return current.get();
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    @Override
    public KeyManager[] getKeyManagersAsArray() {
        KeyManager rc[] = new KeyManager[keyManagers.size()];
        return keyManagers.toArray(rc);
    }

    @Override
    public TrustManager[] getTrustManagersAsArray() {
        TrustManager rc[] = new TrustManager[trustManagers.size()];
        return trustManagers.toArray(rc);
    }

    @Override
    public void addKeyManager(KeyManager km) {
        keyManagers.add(km);
    }

    @Override
    public boolean removeKeyManager(KeyManager km) {
        return keyManagers.remove(km);
    }

    @Override
    public void addTrustManager(TrustManager tm) {
        trustManagers.add(tm);
    }

    @Override
    public boolean removeTrustManager(TrustManager tm) {
        return trustManagers.remove(tm);
    }

    @Override
    public List<KeyManager> getKeyManagers() {
        return keyManagers;
    }

    @Override
    public void setKeyManagers(List<KeyManager> keyManagers) {
        this.keyManagers = keyManagers;
    }

    @Override
    public List<TrustManager> getTrustManagers() {
        return trustManagers;
    }

    @Override
    public void setTrustManagers(List<TrustManager> trustManagers) {
        this.trustManagers = trustManagers;
    }

    @Override
    public SecureRandom getSecureRandom() {
        return secureRandom;
    }

    @Override
    public void setSecureRandom(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getProvider() {
        return provider;
    }

    @Override
    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public SSLContext getSSLContext() throws NoSuchProviderException, NoSuchAlgorithmException, KeyManagementException {
        if( sslContext == null ) {
            if( provider == null ) {
                sslContext = SSLContext.getInstance(protocol);
            } else {
                sslContext = SSLContext.getInstance(protocol, provider);
            }
            sslContext.init(getKeyManagersAsArray(), getTrustManagersAsArray(), getSecureRandom());
        }
        return sslContext;
    }

    @Override
    public void setSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }
}
