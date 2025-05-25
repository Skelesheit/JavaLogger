import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class LoggerParser {

    public static LinkedList<LogEntry> getOperations(Path rootDir){
        // получаем всю информацию и сериализуем
        try{
            List<Path> paths = getLogFiles(rootDir); // получили все пути
            return readLogs(paths); // прочитали и сериализовали файлы
        } catch (IOException e) {
            e.printStackTrace(); // стек трейс
            return null;
        }

    }

    private static LinkedList<LogEntry>  readLogs(List<Path> paths){
        // теперь все операции сереализуем в record
        LinkedList<LogEntry> userOperations = new LinkedList<>();
        for (Path path : paths) {
            try {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    userOperations.add(parseOperation(line));
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }
        return userOperations;
    }

    private static LogEntry parseOperation(String line){
        double amount;
        OperationType operation;
        String[] items = line.split(" ");
        LocalDateTime dateTime = LocalDateTime.parse(items[0] + items[0]);
        String user = items[2];
        switch (items[3]) {
            case "balance" -> {
                operation = OperationType.BALANCE_INQUIRY;
                amount = Integer.parseInt(items[5]);
                return new LogEntry(line, dateTime, user, operation, amount, null);
            }
            case "transferred" -> {
                operation = OperationType.TRANSFERRED;
                amount = Integer.parseInt(items[4]);
                String userTo = items[6];
                return new LogEntry(line, dateTime, user, operation, amount, userTo);
            }
            case "withdrew" -> {
                operation = OperationType.WITHDREW;
                amount = Integer.parseInt(items[4]);
                return new LogEntry(line, dateTime, user, operation, amount, null);
            }
            case null, default -> throw new LogParseException("Неверный формат строки: " + line);
        }
    }

    private static List<Path> getLogFiles(Path rootDir) throws IOException {
        return Files.walk(rootDir) // 🔥 автоматическая вложенность
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".log"))
                .collect(Collectors.toList());
    }


}
