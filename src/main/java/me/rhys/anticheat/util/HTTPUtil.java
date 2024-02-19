package me.rhys.anticheat.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HTTPUtil {

    public static String getResponse(String URL) {
        try {
            URLConnection connection = new URL(URL).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.setConnectTimeout(20000);
            connection.connect();

            BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (IOException ex) {
            return null;
        }
    }

    public static String getResponse(String URL, HashMap<String, String> header) {
        try {
            URLConnection connection = new URL(URL).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.setConnectTimeout(20000);
            header.forEach(connection::setRequestProperty);
            connection.connect();

            BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (IOException ex) {
            return null;
        }
    }

    public static String getResponse(String URL, Map<String, String> header) {
        try {
            URLConnection connection = new URL(URL).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.setConnectTimeout(20000);
            header.forEach(connection::setRequestProperty);
            connection.connect();

            BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (IOException ex) {
            return null;
        }
    }

    public static String getResponse(String URL, HashMap<String, String> header, boolean ignoreExepection) {
        try {
            URLConnection connection = new URL(URL).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.setConnectTimeout(10000);
            header.forEach(connection::setRequestProperty);
            connection.connect();

            BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (IOException ex) {
            if (!ignoreExepection) {
                ex.printStackTrace();
            }
            return null;
        }
    }


    public static String getUUID(String name) {
        return getResponse(String.format("http://data.stitch.best/sparky/getUUID.php?name=%s", name));
    }
}
