import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

    private static Path logFile;

    public static void init(String file) {
        logFile = Config.expand(file);

        try {
            if (Files.isDirectory(logFile)) {
                logFile = logFile.resolve("crashxl.log");
            }

            if (logFile.getParent() != null) {
                Files.createDirectories(logFile.getParent());
            }

        } catch (IOException e) {
            System.out.println("Nie mozna utworzyc dziennika.");
        }
    }

    public static void action(String message) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String text = date + " " + message;

        System.out.println(text);

        try {
            Files.write(logFile, (text + "\n").getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Blad zapisu dziennika.");
        }
    }
}