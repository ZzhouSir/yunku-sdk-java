package com.gokuai.base;

import com.gokuai.base.utils.Base64;
import com.gokuai.base.utils.Util;
import com.google.gson.Gson;
import okhttp3.*;
import org.apache.http.util.TextUtils;

import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * http请求
 */
public final class NetConnection {

    private static final String LOG_TAG = "NetConnection";
    public static final String USER_AGENT = "GK_ANDROID" + ";" + System.getProperties().getProperty("http.agent");
    public static final String ACCEPT_LANGUAGE = Locale.getDefault().toString().contains("zh") ? "zh-CN" : "en-US";
    public static final int TIMEOUT = 30000;
    public static final int CONNECT_TIMEOUT = 30000;

    public static Proxy proxy = null;

    /**
     * 发送请求
     *
     * @param url
     * @param method
     * @param params
     * @param headParams
     * @return
     */
    public static String sendRequest(String url, RequestMethod method,
                                     HashMap<String, String> params, HashMap<String, String> headParams) {
        LogPrint.info(LOG_TAG, " url is: " + url + " " + params);
        return returnOkHttpClientBundle(url, method, params, headParams);
    }

    private static final MediaType URL_ENCODE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private static String returnOkHttpClientBundle(String url, RequestMethod method,
                                                   HashMap<String, String> params, HashMap<String, String> headParams) {

        OkHttpClient client = getOkHttpClient();
        String paramsString = Util.getParamsStringFromHashMapParams(params);

        if (method.equals(RequestMethod.GET) && !TextUtils.isEmpty(paramsString)) {
            url += "?" + paramsString;
            LogPrint.info(LOG_TAG, method + ":" + url);
        }


        Headers.Builder headerBuilder = new Headers.Builder();
        if (headParams != null) {

            for (String key : headParams.keySet()) {
                headerBuilder.add(key, headParams.get(key));
            }
        }

        headerBuilder.add("User-Agent", USER_AGENT);

        headerBuilder.add("Accept-Language",ACCEPT_LANGUAGE);

        Request.Builder requestBuilder = new Request.Builder();
        Request request = null;
        switch (method) {
            case GET:
                request = requestBuilder.url(url).headers(headerBuilder.build()).get().build();
                break;
            case POST:
                RequestBody postBody = RequestBody.create(URL_ENCODE, paramsString);
                request = requestBuilder.url(url).post(postBody).headers(headerBuilder.build()).build();
                break;
            case DELETE:
                RequestBody deleteBody = RequestBody.create(URL_ENCODE, paramsString);
                request = requestBuilder.url(url).delete(deleteBody).headers(headerBuilder.build()).build();

                break;
            case PUT:
                RequestBody putBody = RequestBody.create(URL_ENCODE, paramsString);
                request = requestBuilder.url(url).put(putBody).headers(headerBuilder.build()).build();
                break;
        }

        if (request != null) {
            Response response = null;
            try {
                response = client.newCall(request).execute();

                if (response.header("X-GOKUAI-DEBUG") != null) {
                    LogPrint.error(LOG_TAG, "X-GOKUAI-DEBUG:" + new String(Base64.decode(response.header("X-GOKUAI-DEBUG").getBytes())));
                }
                ReturnResult returnResult = new ReturnResult(response.body().string(), response.code());
                Gson gosn = new Gson();
                return gosn.toJson(returnResult);
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static OkHttpClient getOkHttpClient() {

        ArrayList<Protocol> list = new ArrayList<>();
        list.add(Protocol.HTTP_1_1);
        list.add(Protocol.HTTP_2);
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        if(!(proxy == null)){
            builder.proxy(proxy);
        }

//        final TrustManager[] trustAllCerts = new TrustManager[]{
//                new X509TrustManager() {
//                    @Override
//                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
//                    }
//
//                    @Override
//                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
//                    }
//
//                    @Override
//                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                        return null;
//                    }
//                }
//        };
//
//
//        try {
//            SSLContext sslContext = SSLContext.getInstance("SSL");
//            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
//            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//            builder.protocols(list).socketFactory(sslSocketFactory).hostnameVerifier(new HostnameVerifier() {
//                @Override
//                public boolean verify(String s, SSLSession sslSession) {
//                    return true;
//                }
//            });
//
//        } catch (KeyManagementException | NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
        builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
        builder.readTimeout(TIMEOUT, TimeUnit.MILLISECONDS);
        OkHttpClient httpClient = builder.build();
        return httpClient;
    }
}
