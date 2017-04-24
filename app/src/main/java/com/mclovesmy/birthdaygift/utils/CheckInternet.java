package com.mclovesmy.birthdaygift.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckInternet {

    public Boolean CheckInternetConnection() throws IOException {
        HttpURLConnection urlc = null;
        try {
            urlc = (HttpURLConnection) (new URL("http://www.danielvd.tk").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
