// CRASHXL-VERSION: 1.1.0

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrashXL {

    /**
     * Aby uruchomić program, należy użyć następujących poleceń:
     *  - javac CrashXL.java Config.java Log.java
     *  - java CrashXL --init
     *  - java CrashXL
     * **/

    static final String VERSION = "1.1.0";

    public static void main(String[] args) {

        Map<String, String> cfg = Config.defaults();

        String configFile = cfg.get("C");
        boolean init = false;

        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("--init")) {
                init = true;
            }

            if (args[i].equals("-c") && i + 1 < args.length) {
                configFile = args[i + 1];
                i++;
            }
        }

        try {

            if (init) {
                cfg = Config.ask();
                configFile = cfg.get("C");

                Config.save(cfg, configFile);

                System.out.println("Zapisano konfiguracje.");
                return;
            }

            cfg = Config.load(configFile);

            Log.init(cfg.get("L"));

            Log.action("START CRASHXL " + VERSION);

            copyProgram(cfg.get("X"));

            downloadFiles(cfg);

            Log.action("KONIEC PROGRAMU");

        } catch (IOException e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }

    static byte[] fetch(String address) {

        HttpURLConnection connection = null;

        try {

            URL url = new URL(address);

            connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            connection.setRequestProperty("User-Agent", "CRASHXL/" + VERSION);

            int code = connection.getResponseCode();

            if (code != 200) {
                Log.action("BŁĄD HTTP " + code + " dla " + address);
                return null;
            }

            InputStream input = connection.getInputStream();

            byte[] data = input.readAllBytes();

            input.close();

            return data;

        } catch (IOException e) {

            Log.action("BŁĄD SIECI: " + address);

            return null;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    static String makeUrl(String server, String file) {

        if (!server.startsWith("http://") && !server.startsWith("https://")) {

            server = "http://" + server;
        }

        if (!server.endsWith("/")) {
            server = server + "/";
        }

        return server + file;
    }

    static void downloadFiles(Map<String, String> cfg) throws IOException {

        String serverR = cfg.get("R");
        String serverA = cfg.get("A");
        String listFile = cfg.get("S");

        Log.action("POBIERANIE LISTY PLIKÓW");

        byte[] listData = fetch(makeUrl(serverR, listFile));

        String usedServer = serverR;

        if (listData == null) {

            Log.action("PRZEŁĄCZENIE NA SERWER A");

            listData = fetch(makeUrl(serverA, listFile));

            usedServer = serverA;
        }

        if (listData == null) {

            Log.action("BŁĄD: NIE POBRANO LISTY");

            return;
        }

        String list = new String(listData, "UTF-8");

        List<String> files = new ArrayList<>();

        for (String line : list.split("\\r?\\n")) {

            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.startsWith("file=")) {
                line = line.substring(5).trim();
            }

            if (line.contains("/") || line.contains("\\") || line.contains("..")) {

                Log.action("POMINIĘTO NIEPOPRAWNĄ NAZWĘ: " + line);

                continue;
            }

            files.add(line);
        }

        Path destination = Config.expand(cfg.get("H"));

        Files.createDirectories(destination);

        Log.action("LISTA ZAWIERA " + files.size() + " PLIKÓW");

        for (String file : files) {

            Log.action("POBIERANIE " + file);

            byte[] data = fetch(makeUrl(usedServer, file));

            if (data == null) {

                String otherServer;

                if (usedServer.equals(serverR)) {
                    otherServer = serverA;
                } else {
                    otherServer = serverR;
                }

                Log.action("PRÓBA Z DRUGIEGO SERWERA");

                data = fetch(makeUrl(otherServer, file));
            }

            if (data == null) {

                Log.action("BŁĄD: NIE POBRANO " + file);

                continue;
            }

            Path target = destination.resolve(file);

            Files.write(target, data);

            Log.action("ZAPISANO " + file + " (" + data.length + " bajtow)");
        }
    }

    static void copyProgram(String directory) {

        Path source = Paths.get(".").toAbsolutePath();

        Path destination = Config.expand(directory);

        try {

            Files.createDirectories(destination);

            Path mainFile = source.resolve("CrashXL.java");

            Path copyFile = destination.resolve("CrashXL.java");

            if (!Files.exists(mainFile)) {

                Log.action("NIE ZNALEZIONO PLIKU CrashXL.java");

                return;
            }

            String currentVersion = getVersion(mainFile);

            String copiedVersion = "0";

            if (Files.exists(copyFile)) {
                copiedVersion = getVersion(copyFile);
            }

            if (currentVersion.equals(copiedVersion)) {

                Log.action("KOPIA PROGRAMU JEST AKTUALNA");

                return;
            }

            Files.copy(mainFile, copyFile, StandardCopyOption.REPLACE_EXISTING);

            String[] files = {
                    "Config.java",
                    "Log.java"
            };

            for (String file : files) {

                Path src = source.resolve(file);

                if (Files.exists(src)) {

                    Files.copy(src, destination.resolve(file), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            Log.action("ZAKTUALIZOWANO KOPIĘ PROGRAMU");

        } catch (IOException e) {

            Log.action("BŁĄD KOPIOWANIA PROGRAMU: " + e.getMessage());
        }
    }

    static String getVersion(Path file) {

        try {

            for (String line : Files.readAllLines(file)) {

                if (line.contains("CRASHXL-VERSION:")) {

                    return line.substring(line.indexOf(":") + 1).trim();
                }
            }

        } catch (IOException e) {

            Log.action("BŁĄD ODCZYTU WERSJI");
        }

        return "0";
    }
}