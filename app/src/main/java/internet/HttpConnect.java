package internet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpConnect
{
    public String sendPostRequest(String requestURL, HashMap<String, String> postDataParams)
    {
        URL url;
        String response = "0";
        try {
            url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();/**設定connection物件**/
            /**HttpURLConnection是基於HTTP協定的，其底層通過socket通信實現。如果不設置超時（timeout），在網路異常的情況下，可能會導致程式卡住而不繼續往下執行。**/
            conn.setReadTimeout(15000);/**單位：毫秒**/
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");/**選用傳遞方法 >> POST (這樣表單資料傳送過程中不會被明文顯示)**/
            conn.setDoInput(true);
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
            response=e.getMessage();
        }
        return response;
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
