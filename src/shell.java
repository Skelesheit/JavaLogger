import java.util.Scanner;
import java.util.Arrays;

class Shell {
    public static void main(String[] args) {
        printInfo();
        Scanner scanner = new Scanner(System.in);
        String request = scanner.nextLine();
        while (!request.equals("exit") ) {
            Command command = new Command(request);
            boolean status;
            if (Arrays.asList(Constants.Commands).contains(command)){
                status = command.execute();
                if (status){
                    System.out.println("Команда успешно выполнена");
                }
                else{
                    System.out.println("Команда не выполнена из-за ошибки");
                }
            }


        }
        System.out.println(Constants.EXIT_INFO);

        // Command command = new Command();
    }

    private static void printInfo(){
        System.out.println(Constants.INFO);
    }


}