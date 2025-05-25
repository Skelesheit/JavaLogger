import java.io.IOException;
import java.nio.file.Path;
import java.util.*;


public class Analyzer {
    public static boolean analyze(String directoryPath){
        // прочитали, отсортировали
        LinkedList<LogEntry> usersOperations =  LoggerParser.getOperations(Path.of(directoryPath));
        if (usersOperations.isEmpty()){
            return false;
        }
        Map<String, List<LogEntry>> operationsGroupedByUsers = groupByUsers(usersOperations);
        Map<String, List<LogEntry>> UsersSortedByData = SortedByData(operationsGroupedByUsers);

        // теперь анализируем баланс
        Map<String, LogEntry> resultRecord = calculateBalanceForUsers(UsersSortedByData);
        // добавляем финальный результат в нашу структуру
        for(String user : resultRecord.keySet()){
            UsersSortedByData.get(user).add(resultRecord.get(user));
        }

        // запись результатов в директорию
        try {
            LogWriter.saveUserLogsToFiles(UsersSortedByData, Path.of(directoryPath));
        } catch (IOException e) {
            System.err.println("Ошибка при записи логов: " + e.getMessage());
            e.printStackTrace();
            return false; // стек трейс
        }
        return true;
    }

    private static Map<String, List<LogEntry>> groupByUsers(LinkedList<LogEntry> userOperations){
        // теперь группируем по пользователю
        Map<String, List<LogEntry>> users = new Hashtable<>();
        for (LogEntry userOperation : userOperations) {
            if (users.containsKey(userOperation.user())){
                users.get(userOperation.user()).add(userOperation);
            }
            else {
                users.put(userOperation.user(), new LinkedList<>());
            }
        }
        return users;
    }

    private static Map<String, List<LogEntry>> SortedByData(Map<String, List<LogEntry>> usersOperation){
        for (String key : usersOperation.keySet()){
            List<LogEntry> userOperations = usersOperation.get(key);
            userOperations.sort(Comparator.comparing(LogEntry::timestamp));
            usersOperation.put(key, userOperations);
        }
        return usersOperation;
    }

    private static Map<String, LogEntry> calculateBalanceForUsers(Map<String, List<LogEntry>> usersOperation){
        /* а вообще буду честен - это чисто метод для бизнес логики, где не прописаны исключения
        например если я не найду счёт баланса, тогда мне сделаю его 0.0,
        так как ничего не прописано в ТЗ

        логика такова - проходимся по всем пользователям, у которых уже отсортированы операции по дате
        теперь мы берём последний баланс, и просто считаем после него операции: снятие да переводы
        */
        Map<String, Double> users = new HashMap<>();
        LinkedList<LogEntry> AllOperations = new LinkedList<>();
        for (String key : usersOperation.keySet()){
            List<LogEntry> Operations = usersOperation.get(key);
            calculateBalance(Operations);
            int index = findLastBalanceInquiryIndex(Operations);
            double balance = getBalanceFromOperations(Operations, index);
            // Отрежем все операции после последнего balance inquiry
            List<LogEntry> after = Operations.subList(index + 1, Operations.size());
            users.put(key, balance);
            AllOperations.addAll(after);
        }

        // теперь проведём просчёт по всем истории

        for (LogEntry operation : AllOperations){
            if (operation.operation() == OperationType.BALANCE_INQUIRY) {
                continue; // если такое произойдёт
            }
            if (operation.operation() == OperationType.WITHDREW){
                double minusMonies = operation.amount(); // средства, которые надо снять
                users.compute(operation.user(), (k, currentBalance)
                        -> currentBalance - minusMonies);  // снятие средств с пользователя
            }
            if (operation.operation() == OperationType.TRANSFERRED){
                double minusMonies = operation.amount(); // средства, которые надо снять
                // снимаем с пользователя, который переводит
                users.compute(operation.user(), (String K, Double currentBalance)
                        -> currentBalance - minusMonies);
                // а тут мы добавляем деньги целевому пользователю
                users.compute(operation.targetUser(), (String K, Double currentBalance)
                        -> currentBalance + minusMonies);
            }
        }
        return MakeResultRecord(users, usersOperation);
    }

    private static Map<String, LogEntry>  MakeResultRecord(Map<String, Double> users, Map<String, List<LogEntry>> usersOperation){
        Map<String, LogEntry> resultUserRecord = new Hashtable<>();
        for (String key : users.keySet()){
            LogEntry lastOperation = usersOperation.get(key).getLast();
            String query = "[%s] %s%s%s".formatted(
                    lastOperation.timestamp(),
                    key,
                    Constants.RESULT_BALANCE_INFO,
                    users.get(key)
            );
            resultUserRecord.put(key, new LogEntry(query, lastOperation.timestamp(),
                    key, OperationType.FINAL_BALANCE, users.get(key), null));
        }
        return resultUserRecord;
    }

    private static void calculateBalance(List<LogEntry> Operations){

        int index = findLastBalanceInquiryIndex(Operations);
        double balance = getBalanceFromOperations(Operations, index);

    }

    private static double getBalanceFromOperations(List<LogEntry> Operation, int index){
        double currentBalance;
        if (index != 0){
            return Operation.get(index).amount();
        }
        return 0.0;
    }

    private static int findLastBalanceInquiryIndex(List<LogEntry> operations) {
        for (int i = operations.size() - 1; i >= 0; i--) {
            LogEntry entry = operations.get(i);
            if (entry.operation() == OperationType.BALANCE_INQUIRY) {
                return i;
            }
        }
        return 0; // если не найден
    }

    private static void WriteResults(){

    }






}
