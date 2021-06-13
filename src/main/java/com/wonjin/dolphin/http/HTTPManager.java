package com.wonjin.dolphin.http;

import com.wonjin.dolphin.constants.HTTPConstants;
import com.wonjin.dolphin.http.protocol.Protocol;
import com.wonjin.dolphin.util.LogUtil;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

public class HTTPManager {
    private static Logger logger = Logger.getLogger(HTTPManager.class);

    private int maxConnectionsPerRoute = 50;
    private int maxConnectionsTotal = 100;

    private int connectionTimeout = 5000;
    private int connectionRequestTimeout = 5000;
    private int socketTimeout = 5000;

    private int retryCount = 2;

    // Default : HTTPS
    private Protocol protocol = Protocol.HTTPS;

    private String tlsVersion;
    private String[] supportedProtocols;
    private String[] supportedCipherSuites;

    private KeyStore keyStore;
    private String keyStorePassword = "";

    private String url = "";

    private Map<String, String> headerMap;
    
    private String parameterString;
    private String charset = "UTF-8";

    private CloseableHttpClient httpClient;
    private CloseableHttpResponse httpResponse;

    private String responseBodyText = "";


    /**
     * GET 방식을 사용하여 HTTP 요청 보내는 메서드
     */
    public String get() throws IOException, GeneralSecurityException {
        try {
            httpClient = getClient();

            HttpGet httpGet = new HttpGet(url + "?" + parameterString);

            RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT).setConnectionRequestTimeout(connectionRequestTimeout)
                    .setConnectTimeout(connectionTimeout).setSocketTimeout(socketTimeout).build();

            httpGet.setConfig(requestConfig);

            setHeader(httpGet);

            httpResponse = httpClient.execute(httpGet);

            StringBuilder responseBuilder = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), charset))) {
                String inputLine;

                while ((inputLine = reader.readLine()) != null) {
                    responseBuilder.append(inputLine);
                }
            }

            responseBodyText = responseBuilder.toString();

            return responseBodyText;
        } finally {
            printOneLineLog();

            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }

    /**
     * POST 방식을 사용하여 HTTP 요청 보내는 메서드
     */
    public String post() throws IOException, GeneralSecurityException {
        try {
            httpClient = getClient();

            HttpPost httpPost = new HttpPost(url);

            RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT).setConnectionRequestTimeout(connectionRequestTimeout)
                    .setConnectTimeout(connectionTimeout).setSocketTimeout(socketTimeout).build();

            httpPost.setConfig(requestConfig);

            setHeader(httpPost);

            httpPost.setEntity(new StringEntity(parameterString));

            httpResponse = httpClient.execute(httpPost);

            StringBuilder responseBuilder = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), charset))) {
                String inputLine;

                while ((inputLine = reader.readLine()) != null) {
                    responseBuilder.append(inputLine);
                }
            }

            responseBodyText = responseBuilder.toString();

            return responseBodyText;
        } finally {
            printOneLineLog();

            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }

    /**
     * Header Setting
     */
    private void setHeader(HttpRequestBase httpRequestBase) {
        if (headerMap != null) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpRequestBase.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private HttpRequestRetryHandler createRetryHandler(final int retryCount) {
        return new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount >= retryCount) {
                    return false;
                }

                if (exception instanceof InterruptedIOException) { // Interrupted
                    return false;
                }

                if (exception instanceof UnknownHostException) { // Unknown host
                    return false;
                }

                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);

                if (idempotent) {
                    return true;
                }

                return false;
            }
        };
    }

    /**
     * Create HTTP/HTTPS Client
     */
    private CloseableHttpClient getClient() throws GeneralSecurityException {
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

        return HttpClients.custom().setConnectionManager(connectionManager).setRetryHandler(createRetryHandler(retryCount))
                .setRedirectStrategy(new LaxRedirectStrategy()).build();
    }

    /**
     * SSL HTTP Client
     */
    private CloseableHttpClient createSSLHttpClient() throws GeneralSecurityException {
        TrustStrategy trustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authTypes) throws CertificateException {
                return true;
            }
        };

        if (tlsVersion == null) {
            tlsVersion = HTTPConstants.TLSv1_2;
        }

        SSLContext sslContext = new SSLContextBuilder().loadKeyMaterial(keyStore, keyStorePassword.toCharArray()).loadTrustMaterial(trustStrategy).useProtocol(tlsVersion).build();

        if (supportedProtocols == null) {
            supportedProtocols = new String[] {HTTPConstants.TLSv1_1, HTTPConstants.TLSv1_2};
        }

        SSLConnectionSocketFactory sslConnectionSocketFactory
                = new SSLConnectionSocketFactory(sslContext, supportedProtocols, supportedCipherSuites, new NoopHostnameVerifier());

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionSocketFactory)
                .register("http", PlainConnectionSocketFactory.getSocketFactory()).build();

        // Connection Pool
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        connectionManager.setMaxTotal(maxConnectionsTotal);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

        return HttpClients.custom().setSSLContext(sslContext).setConnectionManager(connectionManager).setRetryHandler(createRetryHandler(retryCount)).build();
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
        this.supportedProtocols = supportedProtocols;
        this.supportedCipherSuites = supportedCipherSuites;
    }

    public void setKeyStore(KeyStore keyStore, String keyStorePassword) {
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
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

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }

    public void setParameter(String parameterString) {
        this.parameterString = parameterString;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void printOneLineLog() {
        // Request Header
        String requestHeaderText = LogUtil.mapToLogString(headerMap);

        // Request Body
        String requestBodyText = parameterString;

        // Response Header
        String responseHeaderText = "";
        if (httpResponse != null) {
            responseHeaderText = LogUtil.headerToLogString(httpResponse.getAllHeaders());
        }

        logger.debug("[HTTP OneLineLog] <URL> " + url + " <Request Header> " + requestHeaderText + " <Request Body> " + requestBodyText
                + " <Response Header> " + responseHeaderText + " <Response Body> " + responseBodyText);
    }
}
