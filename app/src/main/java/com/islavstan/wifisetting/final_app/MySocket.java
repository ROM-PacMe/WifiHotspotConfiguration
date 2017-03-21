package com.islavstan.wifisetting.final_app;

import android.content.Context;
import android.util.Log;


import com.islavstan.wifisetting.R;

import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;



public class MySocket {
    public static final String URL_TO_API = "https://my.vomer.com.ua:3300";
    Context contextApp;
    Socket mSocket;

    public MySocket(Context context) {
        // конструктор суперкласса

        this.contextApp = context;
        getSocketIO();

    }
    public void getSocketIO() {
        final String uri = URL_TO_API;
        try {
            // Load CAs from an InputStream.
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate = certificateFactory.generateCertificate(
                    contextApp.getResources().openRawResource(R.raw.file)); // from file server.crt
            // Create a KeyStore containing the trusted CAs.
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", certificate);
            // Create a TrustManager that trusts the CAs in KeyStore.
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            // Create an SSLContext that uses the TrustManager.
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            //Log.d("VOMER_DATA", "sslContext created");
            //
            // Try to open url.
            final URL url = new URL(uri);
            final HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    Log.d("VOMER_DATA", "Approving certificate for " + hostname);
                    return true; // Do nothing.
                }
            });
            /*
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                httpsURLConnection.getInputStream()));
                        Log.d("VOMER_DATA", "url connected");
                        String line; //FIXME: readLine kicks in socket.io at least on gingerbread!?
                        while((line = in.readLine()) != null) {
                            Log.d("VOMER_DATA", line);
                        }
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }).start();
            */
            //
            // Try to use socket.io library.
            IO.setDefaultSSLContext(sslContext);
            IO.Options options = new IO.Options();
            options.secure = true;
            options.port = 3300;
            options.sslContext = sslContext;
            mSocket = IO.socket(uri, options);



            mSocket.connect();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean loadPage()
    {

        return true;
    }
    public Socket GetSocket()
    {
        return mSocket;
    }

}
