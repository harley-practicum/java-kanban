package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private static final String TEMP_FILE_PATH = "src/resources/temp_tasks.csv"; // Путь к временному файлу
    private FileBackedTaskManager fileBackedTaskManager;

    // Метод для подготовки тестовой среды перед каждым тестом
    @BeforeEach
    void setUp() throws IOException {
        // Удаляем файл, если он существует
        Path filePath = Paths.get(TEMP_FILE_PATH);
        if (Files.exists(filePath)) {
            Files.delete(filePath); // Удаление файла перед каждым тестом
        }

        // Инициализируем FileBackedTaskManager с новым файлом
        fileBackedTaskManager = new FileBackedTaskManager(new InMemoryHistoryManager(), TEMP_FILE_PATH);
    }

    @Test
    void testSaveAndLoadFromFile() throws IOException {
        // Указываем путь к тестовому файлу
        Path filePath = Paths.get("src/resources/test_tasks.csv");
        File file = filePath.toFile(); // Преобразуем Path в File

        // Удаляем файл перед началом теста (если он существует)
        if (file.exists()) {
            Files.delete(filePath);
        }

        // Создаем менеджер с использованием тестового файла
        HistoryManager historyManager = new InMemoryHistoryManager();
        FileBackedTaskManager originalManager = new FileBackedTaskManager(historyManager, file.getPath());

        // Добавляем задачи, эпики и подзадачи
        Task task = new Task(1, "Task 1", "Task 1 description", Status.NEW);
        Epic epic = new Epic(2, "Epic 1", "Epic 1 description", Status.NEW);
        Subtask subtask = new Subtask(3, "Subtask 1", "Subtask 1 description", Status.NEW, 2);

        originalManager.addNewTask(task);
        originalManager.addNewEpic(epic);
        originalManager.addNewSubtask(subtask);

        // Загружаем данные из файла в новый менеджер
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем, что все задачи восстановились корректно
        assertNotNull(loadedManager, "Загруженный менеджер не должен быть null");

        // Печатаем все задачи, эпики и подзадачи после загрузки
        System.out.println("Задачи после загрузки:");
        System.out.println(loadedManager.getTasks());  // Выводим все задачи

        System.out.println("Эпики после загрузки:");
        System.out.println(loadedManager.getEpics());  // Выводим все эпики

        System.out.println("Подзадачи после загрузки:");
        System.out.println(loadedManager.getSubtasks());  // Выводим все подзадачи


        Files.delete(filePath);
    }

}
