package de.flori.ezbanks.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class PluginUpdater {

    private final File pluginFile;
    private final Logger logger;
    private final String currentVersion;

    public PluginUpdater(File pluginFile, Logger logger, String currentVersion) {
        this.pluginFile = pluginFile;
        this.logger = logger;
        this.currentVersion = currentVersion;
    }

    public void checkAndUpdatePlugin() {
        try {
            logger.info("Checking for plugin updates...");
            String latestVersion = fetchLatestVersion();

            if (latestVersion == null) {
                logger.warning("Failed to fetch the latest version.");
                return;
            }

            if (isNewVersion(latestVersion)) {
                logger.info("New plugin version found: " + latestVersion + ". Downloading...");
                boolean success = downloadAndReplacePlugin();
                if (success) {
                    logger.info("Plugin updated successfully to version " + latestVersion + ".");
                    logger.info("The new plugin version will be used on the next server restart.");
                }
            } else {
                logger.info("You are already using the latest version: " + currentVersion);
            }
        } catch (Exception e) {
            logger.warning("Failed to update the plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String fetchLatestVersion() throws IOException {
        String versionUrl = "http://2.56.244.116:2061/version";
        HttpURLConnection connection = (HttpURLConnection) new URL(versionUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }


                String versionJson = response.toString();
                return parseVersionFromJson(versionJson);
            }
        } else {
            logger.warning("Failed to fetch the latest version. Server responded with code: " + responseCode);
            return null;
        }
    }

    private String parseVersionFromJson(String json) {
        if (json.contains("\"version\":\"")) {
            int start = json.indexOf("\"version\":\"") + 11;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }
        return null;
    }

    private boolean isNewVersion(String latestVersion) {
        return !currentVersion.equals(latestVersion);
    }

    private boolean downloadAndReplacePlugin() {
        try {
            if (pluginFile.exists() && !pluginFile.delete()) {
                logger.warning("Failed to delete old plugin file: " + pluginFile.getName());
                return false;
            }
            logger.info("Old plugin file deleted successfully.");

            String downloadUrl = "http://2.56.244.116:2061/download";
            HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream out = new FileOutputStream("plugins/EZBanks-" +  fetchLatestVersion() + ".jar")) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }

                    logger.info("New plugin file downloaded and saved: " + pluginFile.getName());
                    return true;
                }
            } else {
                logger.warning("Failed to download plugin. Server responded with code: " + connection.getResponseCode());
            }
        } catch (IOException e) {
            logger.warning("Failed to download and replace plugin: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}