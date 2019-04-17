/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.function.Supplier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author CyberManic
 */
public class SSLContextFactory implements Supplier<SSLContext>{

    private final SSLContext context;
    
    public static class Builder{
        private String protocol;
        private String keyStoreResourceFile;
        private InputStream keyStore;
        private String trustStoreResourceFile;
        private InputStream trustStore;
        private String keyStorePassPhrase = "";
        private String trustStorePassPhrase = "";
        
        public Builder(){}
        
        public SSLContextFactory build() throws NoSuchAlgorithmException{
            
            if (keyStore==null){
                if(keyStoreResourceFile!=null){
                    keyStore = Builder.class.getClassLoader().getResourceAsStream(keyStoreResourceFile);                    
                }
            }
            
            if (trustStore==null){
                if(trustStoreResourceFile!=null){
                    trustStore = Builder.class.getClassLoader().getResourceAsStream(trustStoreResourceFile);                    
                }
            }                        
            
            return new SSLContextFactory(protocol, keyStore, keyStorePassPhrase.toCharArray(), trustStore, trustStorePassPhrase.toCharArray());
        }

        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setKeyStoreResourceFile(String keyStoreResourceFile) {
            this.keyStoreResourceFile = keyStoreResourceFile;
            return this;
        }

        public Builder setKeyStore(InputStream keyStore) {
            this.keyStore = keyStore;
            return this;
        }

        public Builder setTrustStoreResourceFile(String trustStoreResourceFile) {
            this.trustStoreResourceFile = trustStoreResourceFile;
            return this;
        }

        public Builder setTrustStore(InputStream trustStore) {
            this.trustStore = trustStore;
            return this;
        }

        public Builder setKeyStorePassPhrase(String passPhrase) {
            this.keyStorePassPhrase = passPhrase;
            return this;
        }

        public Builder setTrustStorePassPhrase(String passPhrase) {
            this.trustStorePassPhrase = passPhrase;
            return this;
        }
                
    }    
    
    private SSLContextFactory(String protocol, InputStream keyStore, final char[] keyStorePassphrase, InputStream trustStore, final char[] trustStorePassphrase) throws NoSuchAlgorithmException{
        context = SSLContext.getInstance(protocol);

        try{
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(keyStore, keyStorePassphrase);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keyStorePassphrase);

            KeyStore ts = null;
            TrustManagerFactory tmf = null;

            if (trustStore!=null){
                ts = KeyStore.getInstance("JKS");
                ts.load(trustStore, trustStorePassphrase);
                tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(ts);                
                context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            }else{                
                context.init(kmf.getKeyManagers(), new TrustManager[]{getTrustAllManager()}, null);
            }

        }catch(KeyStoreException|KeyManagementException|IOException|CertificateException|UnrecoverableKeyException ex){
            throw new RuntimeException("SSLContextFactory() error: " + ex.getMessage());
        } 
    }

    private TrustManager getTrustAllManager(){        
        return new X509TrustManager() {
            @Override public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            @Override public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
        };
    }
    
    @Override
    public SSLContext get(){        
        return context;
    }        
    
}
