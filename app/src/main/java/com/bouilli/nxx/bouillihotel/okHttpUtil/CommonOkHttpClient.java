package com.bouilli.nxx.bouillihotel.okHttpUtil;

import android.annotation.SuppressLint;

import com.bouilli.nxx.bouillihotel.okHttpUtil.listener.DisposeDataHandle;
import com.bouilli.nxx.bouillihotel.okHttpUtil.response.CommonFileCallback;
import com.bouilli.nxx.bouillihotel.okHttpUtil.response.CommonJsonCallback;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by 18230 on 2017/3/26.
 */

public class CommonOkHttpClient {
    private static final int TIME_OUT = 10;
    private static OkHttpClient mOkHttpClient = null;
    // private static CommonOkHttpClient mClient = null;

    static{
        if(mOkHttpClient == null){
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
            okHttpClientBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            //okHttpClientBuilder.cookieJar(new SimpleCookieJar());
            okHttpClientBuilder.connectTimeout(TIME_OUT, TimeUnit.SECONDS);
            okHttpClientBuilder.readTimeout(TIME_OUT, TimeUnit.SECONDS);
            okHttpClientBuilder.writeTimeout(TIME_OUT, TimeUnit.SECONDS);
            okHttpClientBuilder.followRedirects(true);
            /**
             * trust all the https point
             */
            okHttpClientBuilder.sslSocketFactory(createSSLSocketFactory());
            mOkHttpClient = okHttpClientBuilder.build();
        }
    }

    /**
     * 默认信任所有的证书
     * TODO 最好加上证书认证，主流App都有自己的证书
     *
     * @return
     */
    @SuppressLint("TrulyRandom")
    private static SSLSocketFactory createSSLSocketFactory() {

        SSLSocketFactory sSLSocketFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllManager()},
                    new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return sSLSocketFactory;
    }

    private static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)

                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    /**
     * 通过构造好的Request,Callback去发送请求
     *
     * @param request
     * @param handle
     */
    public static Call get(Request request, DisposeDataHandle handle) {
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonJsonCallback(handle));
        return call;
    }

    public static Call post(Request request, DisposeDataHandle handle) {
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonJsonCallback(handle));
        return call;
    }

    public static Call downloadFile(Request request, DisposeDataHandle handle) {
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonFileCallback(handle));
        return call;
    }
}
