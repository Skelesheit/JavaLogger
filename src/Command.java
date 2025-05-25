import java.util.*;
import java.util.function.Function;

public class Command {

    public String command;
    public String[] args;

    private static final Map<String, Function<String, Boolean>> RequestToCommand = new HashMap<>();

    static {
        RequestToCommand.put("analyze", Command::runAnalyzer);
    }

    public Command(String request) {
        // парсим строку: первое — команда, остальное — аргументы
        args = request.trim().split(" ");
        command = args[0].toLowerCase();
        args = Arrays.copyOfRange(args, 1, args.length);
    }

    public boolean execute() {
        Function<String, Boolean> action = RequestToCommand.get(command);
        if (action != null) {
            String param = args.length > 0 ? args[0] : "";  // передаём путь
            return action.apply(param);
        } else {
            System.err.println("Неизвестная команда: " + command);
            return false;
        }
    }

    private static boolean runAnalyzer(String path) {
        return Analyzer.analyze(path);
    }
}
