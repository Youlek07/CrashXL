import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Config {

    public static Map<String, String> defaults() {
        Map<String, String> cfg = new LinkedHashMap<>();

        cfg.put("C", "~/.config/crashxl/conf.ext");
        cfg.put("R", "https://edu.gplweb.pl/sh/");
        cfg.put("A", "10.4.0.117/sh/");
        cfg.put("S", "scripts.lst");
        cfg.put("H", "~/Pobrane/");
        cfg.put("X", "/opt/crashxl");
        cfg.put("L", "/var/log/crashxl");

        return cfg;
    }

    public static Map<String, String> load(String file) throws IOException {
        Map<String, String> cfg = defaults();

        Path path = expand(file);

        if (Files.exists(path)) {
            for (String line : Files.readAllLines(path)) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int pos = line.indexOf("=");

                if (pos > 0) {
                    String key = line.substring(0, pos).trim();
                    String value = line.substring(pos + 1).trim();

                    if (cfg.containsKey(key)) {
                        cfg.put(key, value);
                    }
                }
            }
        }

        return cfg;
    }

    public static void save(Map<String, String> cfg, String file)
            throws IOException {

        Path path = expand(file);

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        StringBuilder text = new StringBuilder();

        for (String key : cfg.keySet()) {
            text.append(key).append("=").append(cfg.get(key)).append("\n");
        }

        Files.write(path, text.toString().getBytes("UTF-8"));
    }

    public static Map<String, String> ask() {
        Scanner scanner = new Scanner(System.in);
        Map<String, String> cfg = defaults();

        System.out.println("Tworzenie konfiguracji CRASHXL");
        System.out.println("Enter - wartosc domyslna");

        for (String key : cfg.keySet()) {
            System.out.print(key + " [" + cfg.get(key) + "]: ");

            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                cfg.put(key, value);
            }
        }

        return cfg;
    }

    public static Path expand(String value) {
        String home = System.getProperty("user.home");

        if (value.startsWith("~/")) {
            value = home + value.substring(1);
        }

        return Paths.get(value);
    }
}