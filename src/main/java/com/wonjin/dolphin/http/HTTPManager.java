package com.wonjin.dolphin.http;

import com.wonjin.dolphin.http.protocol.Protocol;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

public class HTTPManager {
    private static Logger logger = Logger.getLogger(HTTPManager.class);

    private int maxConnectionsPerRoute;
    private int maxConnectionsTotal;

    private int connectionTimeout;
    private int connectionRequestTimeout;
    private int socketTimeout;

    private Protocol protocol;

    private String tlsVersion;
    private String[] supportedProtocols;
    private String[] supportedCipherSuites;

    private String url;

    private Map<String, String> headerMap;
    private Map<String, String> parameterMap;

    /**
     * Create HTTP/HTTPS Client
     */
    private CloseableHttpClient getClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        if (Protocol.HTTP == protocol) {
            return createHttpClient();
        }
        return createSSLHttpClient();
    }

    /**
     * HTTP Client
     */
    private CloseableHttpClient createHttpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        connectionManager.setMaxTotal(maxConnectionsTotal);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

        return HttpClients.custom().setConnectionManager(connectionManager)
                .setRedirectStrategy(new LaxRedirectStrategy()).build();
    }

    /**
     * SSL HTTP Client
     */
    private CloseableHttpClient createSSLHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy trustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authTypes) throws CertificateException {
                return true;
            }
        };

        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, trustStrategy).useProtocol(tlsVersion).build();

        SSLConnectionSocketFactory sslConnectionSocketFactory
                = new SSLConnectionSocketFactory(sslContext, supportedProtocols, supportedCipherSuites, new NoopHostnameVerifier());

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionSocketFactory)
                .register("http", PlainConnectionSocketFactory.getSocketFactory()).build();

        // Connection Pool
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        connectionManager.setMaxTotal(maxConnectionsTotal);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

        return HttpClients.custom().setSSLContext(sslContext).setConnectionManager(connectionManager).build();
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public void setProtocol(Protocol protocol, String tlsVersion) {
        this.protocol = protocol;
        this.tlsVersion = tlsVersion;
    }

    public void setProtocol(Protocol protocol, String tlsVersion, String[] supportedProtocols) {
        this.protocol = protocol;
        this.tlsVersion = tlsVersion;
        this.supportedProtocols = supportedProtocols;
    }

    public void setProtocol(Protocol protocol, String tlsVersion, String[] supportedProtocols, String[] supportedCipherSuites) {
        this.protocol = protocol;
        this.tlsVersion = tlsVersion;
        this.supportedProtocols = supportedProtocols
        this.supportedCipherSuites = supportedCipherSuites;
    }

    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    public void setMaxConnectionsTotal(int maxConnectionsTotal) {
        this.maxConnectionsTotal = maxConnectionsTotal;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }

    public void setParameterMap(Map<String, String> parameterMap) {
        this.parameterMap = parameterMap;
    }
}
