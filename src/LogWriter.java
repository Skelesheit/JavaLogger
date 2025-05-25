import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class LogWriter {

    /**
     * Сохраняет логи по каждому пользователю в виде отдельных файлов.
     * @param usersSortedByData Map: пользователь -> список логов, отсортированных по дате и содержащих финальный баланс
     * @param inputDirectory директория, в которой находятся оригинальные логи (используется для вычисления output)
     * @throws IOException при ошибке записи файлов
     */
    public static void saveUserLogsToFiles(Map<String, List<LogEntry>> usersSortedByData, Path inputDirectory) throws IOException {
        // Создаём директорию transactions_by_users рядом с inputDirectory
        Path outputDir = inputDirectory.resolve(Constants.OUTPUT_DIRECTORY);
        Files.createDirectories(outputDir); // безопасно, не кидает если уже существует

        for (Map.Entry<String, List<LogEntry>> entry : usersSortedByData.entrySet()) {
            String username = entry.getKey();
            List<LogEntry> logs = entry.getValue();

            // Путь к файлу: transactions_by_users/userXXX.log
            Path outputFile = outputDir.resolve(username + ".log");

            // Конвертируем все записи в строки
            List<String> lines = logs.stream()
                    .map(LogEntry::toString)
                    .collect(Collectors.toList());

            // Записываем в файл, перезаписывая его при необходимости
            Files.write(outputFile, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

}