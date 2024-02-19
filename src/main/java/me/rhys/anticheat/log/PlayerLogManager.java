package me.rhys.anticheat.log;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.rhys.anticheat.Plugin;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.bukkit.Bukkit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.GZIPOutputStream;

@Getter
public class PlayerLogManager {
    private final List<LogEntry> logEntries = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();


    public void start() {
        this.executorService.scheduleAtFixedRate(() -> {

            final StringBuilder stringBuilder = new StringBuilder();

            this.logEntries.forEach(logEntry -> stringBuilder.append(logEntry.toString()).append("<LINE>"));

            if (stringBuilder.length() > 1) {

                final HttpClient httpClient = HttpClients.createDefault();
                final HttpPost httpPost = new HttpPost(
                        "https://monolith.sparky.ac/service/96abd381-448b-40a5-be9c-e469172589d7");

                httpPost.setHeader("User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11" +
                                " (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

                httpPost.setHeader("authentication", Plugin.getInstance().getLicenseKey());
                httpPost.setHeader("mode", "add");

                try {
                    final List<NameValuePair> params = new ArrayList<>(2);

                    params.add(new BasicNameValuePair("data", Arrays.toString(
                            compress(stringBuilder.toString()))));

                    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    httpClient.execute(httpPost);
                } catch (IOException ignored) {
                }
            }

            this.logEntries.clear();
        }, 30L, 30L, TimeUnit.SECONDS);
    }

    public void shutdown() {
        this.executorService.shutdownNow();
    }

    private byte[] compress(String data) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(data.length());
        final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);

        gzipOutputStream.write(data.getBytes());
        gzipOutputStream.close();

        final byte[] compressed = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return compressed;
    }

    @Getter @AllArgsConstructor
    public static final class LogEntry {
        private final String name;
        private final String type;
        private final int violation;
        private final boolean experiential;
        private final String username;

        public String toString() {
            return this.name + ":" + this.type + ":" + this.violation + ":"
                    + this.experiential + ":" + this.username;
        }
    }
}
