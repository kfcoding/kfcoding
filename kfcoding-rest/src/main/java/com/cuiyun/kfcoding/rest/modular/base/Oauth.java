package com.cuiyun.kfcoding.rest.modular.base;

import com.cuiyun.kfcoding.core.support.HttpKit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Map;

/**
 * @program: kfcoding
 * @description: Oath基础类
 * @author: maple
 * @create: 2018-05-07 10:41
 **/
public class Oauth {

    private String clientId;
    private String clientSecret;
    private String redirectUri;

    public Oauth() {}

    protected String getAuthorizeUrl(String authorize, Map<String, String> params) throws UnsupportedEncodingException {
        return HttpKit.initParams(authorize, params);
    }

    protected String doPost(String url, Map<String, String> params) throws IOException, KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException {
        return HttpKit.post(url, params);
    }

    protected String doGet(String url, Map<String, String> params) throws IOException, KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException {
        return HttpKit.get(url, params);
    }

    protected String doGetWithHeaders(String url, Map<String, String> headers) throws IOException, KeyManagementException, NoSuchAlgorithmException, NoSuchProviderException {
        return HttpKit.get(url, null, headers);
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}

