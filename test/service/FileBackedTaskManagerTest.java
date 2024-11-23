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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private static final String TEMP_FILE_PATH = "src/resources/temp_tasks.csv"; // Путь к временному файлу
    private FileBackedTaskManager fileBackedTaskManager;

    // Метод для подготовки тестовой среды перед каждым тестом
    @BeforeEach
    void setUp() throws IOException {
        // Удаляем файл, если он существует
        Files.deleteIfExists(Paths.get(TEMP_FILE_PATH));

        // Инициализируем FileBackedTaskManager с новым файлом
        fileBackedTaskManager = new FileBackedTaskManager(new InMemoryHistoryManager(), TEMP_FILE_PATH);
    }

    @Test // прошу скажите что еще не исправил? или как развернуть их на гитхабе и посмотреть все...?
    void testSaveAndLoadFromFile() throws IOException {
        // Указываем путь к тестовому файлу
        Path filePath = Paths.get("src/resources/test_tasks.csv");
        File file = filePath.toFile(); // Преобразуем Path в File

        // Удаляем файл перед началом теста (если он существует)
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            fail("Не удалось удалить старый файл: " + e.getMessage());
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

        // Создаем новый менеджер и загружаем данные из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем, что все задачи восстановились корректно
        assert loadedManager != null;
        Task loadedTask = loadedManager.getTask(1);
        Epic loadedEpic = loadedManager.getEpic(2);
        Subtask loadedSubtask = loadedManager.getSubtask(3);

        assertNotNull(loadedTask, "Задача должна быть восстановлена");
        assertNotNull(loadedEpic, "Эпик должен быть восстановлен");
        assertNotNull(loadedSubtask, "Подзадача должна быть восстановлена");

        // Проверяем, что все поля объектов совпадают
        assertEquals(task.getTitle(), loadedTask.getTitle(), "Названия задач должны совпадать");
        assertEquals(task.getDescription(), loadedTask.getDescription(), "Описания задач должны совпадать");
        assertEquals(task.getStatus(), loadedTask.getStatus(), "Статусы задач должны совпадать");

        assertEquals(epic.getTitle(), loadedEpic.getTitle(), "Названия эпиков должны совпадать");
        assertEquals(epic.getDescription(), loadedEpic.getDescription(), "Описания эпиков должны совпадать");
        assertEquals(epic.getStatus(), loadedEpic.getStatus(), "Статусы эпиков должны совпадать");

        assertEquals(subtask.getTitle(), loadedSubtask.getTitle(), "Названия подзадач должны совпадать");
        assertEquals(subtask.getDescription(), loadedSubtask.getDescription(), "Описания подзадач должны совпадать");
        assertEquals(subtask.getStatus(), loadedSubtask.getStatus(), "Статусы подзадач должны совпадать");
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId(), "Связи подзадач с эпиком должны совпадать");

        // Дополнительно можно вывести содержимое файла для отладки
        try {
            List<String> fileContent = Files.readAllLines(filePath);
            System.out.println("File content:\n" + String.join("\n", fileContent));
        } catch (IOException e) {
            fail("Ошибка чтения файла: " + e.getMessage());
        }
    }
}