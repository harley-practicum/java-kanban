package service;

public class Managers {

    // Метод для получения экземпляра TaskManager
    public static TaskManager getDefault() {
        HistoryManager historyManager = getDefaultHistory();
        return new FileBackedTaskManager(historyManager); // Используем FileBackedTaskManager
    }

    // Метод для получения экземпляра HistoryManager
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager(); // Создаем и возвращаем InMemoryHistoryManager
    }
}


