package service;

import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class FileBackedTaskManagerTest {

    private static final String TEMP_FILE_PATH = "src/resources/test_Task.csv"; // Новый путь файла
    private FileBackedTaskManager taskManager = new FileBackedTaskManager(new InMemoryHistoryManager());

    // Метод для создания файла перед каждым тестом
    @BeforeEach
    void setUp() throws IOException {
        // Удаляем файл, если он существует, перед каждым тестом
        Files.deleteIfExists(Paths.get(TEMP_FILE_PATH));

        // Создаем новый файл
        Files.createFile(Paths.get(TEMP_FILE_PATH));
    }

    @Test
    void testAddTask() {
        // Указываем путь к файлу, который используется в методе saveToFile
        Path filePath = Paths.get("src/resources/tasks.csv");

        // Удаляем файл перед началом теста (если существует), чтобы проверить запись с нуля
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            fail("Не удалось удалить старый файл: " + e.getMessage());
        }

        // Создаем новую задачу
        Task task = new Task(1, "Test Task", "Test Description", Status.NEW);

        // Добавляем задачу в менеджер
        taskManager.addNewTask(task);

        // Проверяем, что задача добавлена в менеджер
        Task loadedTask = taskManager.getTask(1);
        assertNotNull(loadedTask, "Задача должна быть добавлена в менеджер");
        assertEquals("Test Task", loadedTask.getTitle(), "Название задачи должно совпадать");

        // Вызываем метод сохранения
        taskManager.saveToFile();

        // Проверяем содержимое файла
        List<String> fileLines = null;
        try {
            fileLines = Files.readAllLines(filePath);
        } catch (IOException e) {
            fail("Ошибка чтения файла: " + e.getMessage());
        }

        // Проверяем, что файл не пустой
        assertNotNull(fileLines, "Файл не должен быть пустым");
        assertFalse(fileLines.isEmpty(), "Файл не содержит строк");

        // Проверяем содержимое файла
        boolean taskFound = fileLines.stream().anyMatch(line -> line.contains("Test Task"));
        assertTrue(taskFound, "Файл должен содержать запись о задаче");

        // Вывод содержимого файла для отладки
        System.out.println("File content after saving: " + fileLines);
    }


    @Test
    void testFileCreationOnLoad() {
        // Убедитесь, что файл создается, если он не существует
        FileBackedTaskManager newManager = new FileBackedTaskManager(new InMemoryHistoryManager());
        assertTrue(Files.exists(Paths.get(TEMP_FILE_PATH)), "Файл должен быть создан при загрузке.");
    }
}




