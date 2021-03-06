package com.wonjin.dolphin.http;

import com.wonjin.dolphin.http.protocol.Protocol;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;

import java.util.Map;

public class HTTPManager {
    private static Logger logger = Logger.getLogger(HTTPManager.class);

    private int maxConnectionsPerRoute;
    private int maxConnectionsTotal;

    private int connectionTimeout;
    private int connectionRequestTimeout;
    private int socketTimeout;

    private Protocol protocol;

    private String url;

    private Map<String, String> headerMap;
    private Map<String, String> parameterMap;

    /**
     * Create HTTP/HTTPS Client
     */
    private CloseableHttpClient getClient() {
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
    private CloseableHttpClient createSSLHttpClient() {
        return null;
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

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
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
