import exception.ManagerLoadException;
import model.Status;
import model.Task;
import service.FileBackedTaskManager;
import service.HistoryManager;
import service.InMemoryHistoryManager;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        HistoryManager historyManager = new InMemoryHistoryManager();
        FileBackedTaskManager taskManager = null;
        try {
            taskManager = new FileBackedTaskManager(historyManager,"src/resources/tasks.csv");
        } catch (ManagerLoadException e) {
            System.err.println("Ошибка при загрузке данных: " + e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Меню:");
            System.out.println("1. Добавить задачу");
            System.out.println("2. Просмотр задач");
            System.out.println("3. Выход");
            int choice = scanner.nextInt();
            scanner.nextLine();  // Поглощаем символ новой строки

            switch (choice) {
                case 1:
                    System.out.println("Введите название задачи:");
                    String name = scanner.nextLine();
                    System.out.println("Введите описание задачи:");
                    String description = scanner.nextLine();
                    taskManager.addNewTask(new Task(1, name, description, Status.NEW));
                    break;
                case 2:
                    taskManager.getTasks().forEach(task -> System.out.println(task));
                    break;
                case 3:
                    System.out.println("Выход...");
                    return;
                default:
                    System.out.println("Неверный выбор.");
            }
        }
    }
}
