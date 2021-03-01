package internet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpConnect
{
    public String sendPostRequest(String requestURL, HashMap<String, String> postDataParams)
    {
        URL url;
        String response = "0";
        try {
            TrustManager[] trustAllCerts=
                    {
                            new X509TrustManager()
                            {
                                @Override
                                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
                                {

                                }

                                @Override
                                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
                                {

                                }

                                @Override
                                public X509Certificate[] getAcceptedIssuers()
                                {
                                    return new X509Certificate[0];
                                }
                            }
                    };


            SSLContext sslContext =SSLContext.getInstance("TLS");
            sslContext.init(null,trustAllCerts,null);

            url = new URL(requestURL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();/**設定connection物件**/
            conn.setSSLSocketFactory(sslContext.getSocketFactory());
            //conn.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOST);

            final HostnameVerifier hostnameVerifier=new HostnameVerifier()
            {
                @Override
                public boolean verify(String hostname, SSLSession session)
                {
                    return true;
                }
            };
            conn.setHostnameVerifier(hostnameVerifier);
            //InputStream in=conn.getInputStream();
            //copyInputStreamToOutputStream(in, System.out);

            /**HttpURLConnection是基於HTTP協定的，其底層通過socket通信實現。如果不設置超時（timeout），在網路異常的情況下，可能會導致程式卡住而不繼續往下執行。**/
            conn.setReadTimeout(15000);/**單位：毫秒**/
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");/**選用傳遞方法 >> POST (這樣表單資料傳送過程中不會被明文顯示)**/
            //conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));
            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                response = br.readLine();
            }
            else
            {
                response="Error Create"+ conn.getResponseCode();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            response=e.toString();
        }
        return response;
    }

    private void copyInputStreamToOutputStream(InputStream in, PrintStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int count;
        while ((count = in.read(buffer)) > 0)
        {
            out.write(buffer, 0, count);
        }
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet())
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }
}
