package service;

public class Managers {

    // Метод для получения экземпляра TaskManager
    public static TaskManager getDefault() {
        HistoryManager historyManager = getDefaultHistory();
        return new FileBackedTaskManager(historyManager, "src/resources/tasks.csv"); // Передаем historyManager и путь к файлу
    }

    // Метод для получения экземпляра HistoryManager
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager(); // Создаем и возвращаем InMemoryHistoryManager
    }
}



