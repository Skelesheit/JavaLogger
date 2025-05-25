import java.time.LocalDateTime;

public record LogEntry(
        String allQuery,
        LocalDateTime timestamp,
        String user,
        OperationType operation,
        double amount,
        String targetUser // может быть null для balance и withdrew
) {}