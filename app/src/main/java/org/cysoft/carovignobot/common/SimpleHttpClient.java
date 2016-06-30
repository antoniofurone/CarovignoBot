package org.cysoft.carovignobot.common;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

/**
 * Created by NS293854 on 13/05/2016.
 */
public class SimpleHttpClient {

    private final String LOG_TAG=this.getClass().getName();

    private String aUrl="";
    public SimpleHttpClient(String url){
        aUrl=url;
    }

    public String getResult(String language)
            throws CyBssException
    {
        String result="";

        /*
        final String authUser = "ns293854";
        final String authPassword = "--------";

        System.setProperty("http.proxyHost", "10.29.176.1");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);

        Authenticator.setDefault(
                new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(authUser, authPassword.toCharArray());
                    }
                }
        );
        */

        URL url= null;
        try {
            url = new URL(aUrl);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            Log.e(getClass().getName(),e1.getMessage());
            throw new CyBssException(e1);
        }

        HttpURLConnection urlConnection= null;
        try {
            urlConnection = (HttpURLConnection)url.openConnection();
            if (language!=null && !language.equals(""))
                urlConnection.addRequestProperty("language",
                        language);
        } catch (IOException e1) {
            e1.printStackTrace();
            Log.e(getClass().getName(), e1.getMessage());
            throw new CyBssException(e1);
        }

        try {
            Log.i(LOG_TAG,"ResponseCode="+urlConnection.getResponseCode());

            if (urlConnection.getResponseCode()==200){

                InputStreamReader isr=new InputStreamReader(urlConnection.getInputStream());
                BufferedReader br=new BufferedReader(isr);
                String tmp="";
                while((tmp=br.readLine())!=null){
                    result+=tmp;
                }
            }
            else
            {
                throw new CyBssException("Error:Response code <> 200");
            }

        } catch (IOException e1) {
            Log.e(getClass().getName(), e1.getMessage());
            throw new CyBssException(e1);
        }

        return result;

    }

    public String getResult()
            throws CyBssException
    {
        return getResult("");
    }
}
