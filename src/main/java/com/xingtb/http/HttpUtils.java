package com.xingtb.http;


import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class HttpUtils {

    private static RequestConfig requestConfig;

    static {
        requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).build();
    }

    /**
     * construct Bearer header
     *
     * @param token Bearer token
     * @return Bearer Auth Header
     */
    public static String getBearerAuthHeader(String token) {
        return "Bearer " + token;
    }

    /**
     * return json-formatted string
     */
    private static ResponseHandler<String> rh = response -> {
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        //exception throwing
        if (statusLine.getStatusCode() >= 300) {
            String reasonPhrase = statusLine.getReasonPhrase();
            String errorString = StringUtils.isNotEmpty(reasonPhrase) ? reasonPhrase : EntityUtils.toString(entity);
            throw new HttpResponseException(
                    statusLine.getStatusCode(),
                    errorString);
        }
        if (entity == null) {
            throw new ClientProtocolException("Response contains no content");
        }
        ContentType contentType = ContentType.getOrDefault(entity);
        Charset charset = contentType.getCharset() == null ? Charset.forName("UTF-8") : contentType.getCharset();
        return EntityUtils.toString(entity, charset);
    };

    /**
     *
     * @param url url
     * @param headers nullable
     * @param params nullable
     * @param isInsecureClient if true use sslClient,false use default httpClient
     * @return json string
     * @throws Exception
     */
    public static String get(String url, Map<String, String> headers, Map<String, String> params, boolean isInsecureClient) throws Exception {
        return urlRequest(url, headers, params, isInsecureClient, "GET");
    }

    /**
     *
     * @param url url
     * @param headers nullable
     * @param params nullable
     * @param isInsecureClient if true use sslClient,false use default httpClient
     * @return json string
     * @throws Exception
     */
    public static String delete(String url, Map<String, String> headers, Map<String, String> params, boolean isInsecureClient) throws Exception {
        return urlRequest(url, headers, params, isInsecureClient, "DELETE");
    }

    private static String urlRequest(String url, Map<String, String> headers, Map<String, String> params, boolean isInsecureClient, String type) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        if (isInsecureClient) httpclient = customHttpClient();
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> nvpList = params.entrySet().stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())).collect(Collectors.toList());
            url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(nvpList, Charset.forName("UTF-8")));
        }
        HttpRequestBase httpRequest;
        switch (type) {
            case "DELETE":
                httpRequest = new HttpDelete(url);
                break;
            case "PUT":
                httpRequest = new HttpPut(url);
                break;
            default:
                httpRequest = new HttpGet(url);
                break;
        }
        httpRequest.setConfig(requestConfig);
        if (headers != null && !headers.isEmpty()) headers.forEach(httpRequest::addHeader);
        String response;
        try {
            response = httpclient.execute(httpRequest, rh);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                log.error("Method get: ", e);
            }
        }
        return response;
    }

    /**
     *
     * @param url url
     * @param headers nullable
     * @param params not nullable
     * @param isInsecureClient if true use sslClient,false use default httpClient
     * @return json string
     * @throws Exception
     */
    public static String postForm(String url, Map<String, String> headers, Map<String, String> params, boolean isInsecureClient) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        if (isInsecureClient) httpclient = customHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        if (headers != null && !headers.isEmpty()) headers.forEach(httpPost::addHeader);
        List<NameValuePair> nvpList = params.entrySet().stream()
                .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        httpPost.setEntity(new UrlEncodedFormEntity(nvpList, Charset.forName("UTF-8")));
        String response;
        try {
            response = httpclient.execute(httpPost, rh);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                log.error("Method postForm: ", e);
            }
        }
        return response;
    }

    /**
     *
     * @param url url
     * @param headers nullable
     * @param params not nullable
     * @param isInsecureClient if true use sslClient,false use default httpClient
     * @return json string
     * @throws Exception
     */
    public static String postJson(String url, Map<String, String> headers, JSONObject params, boolean isInsecureClient) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        if (isInsecureClient) httpclient = customHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

        if (headers != null && !headers.isEmpty()) {
            headers.forEach(httpPost::addHeader);
        }
        ContentType contentType = ContentType.create("application/json", CharsetUtils.get("UTF-8"));
        httpPost.setEntity(new StringEntity(params.toString(), contentType));
        String response;
        try {
            response = httpclient.execute(httpPost, rh);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                log.error("Method postJson: ", e);
            }
        }

        return response;
    }

    private static CloseableHttpClient customHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
        X509TrustManager trustManager = new X509TrustManager() {
            @Override public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            @Override public void checkClientTrusted(X509Certificate[] xcs, String str) {}
            @Override public void checkServerTrusted(X509Certificate[] xcs, String str) {}
        };
        SSLContext sslcontext = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
        sslcontext.init(null, new TrustManager[] { trustManager }, null);
        SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE);
        // Set the protocol http and https corresponding processing socket link factory object
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", factory).build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // Create a custom httpclient object
        return HttpClients.custom().setConnectionManager(connManager).setSSLSocketFactory(factory).build();
    }

}

