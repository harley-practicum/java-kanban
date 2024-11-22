package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;


class FileBackedTaskManagerTest {

    private static final String TEMP_FILE_PATH = "src/resources/temp_tasks.csv"; // Новый путь файла
    private FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(new InMemoryHistoryManager(),TEMP_FILE_PATH);

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
        // Создаем временный файл для хранения данных
        File tempFile = new File(TEMP_FILE_PATH);
        if (tempFile.exists()) {
            tempFile.delete();
        }

        // Операции с файлами с использованием Stream
        try (Stream<Path> paths = Files.walk(tempFile.toPath())) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> path.toFile().delete());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






























    @Test
    void testFileCreationOnLoad() {
        // Убедитесь, что файл создается, если он не существует
        FileBackedTaskManager newManager = new FileBackedTaskManager(new InMemoryHistoryManager(),TEMP_FILE_PATH);
        assertTrue(Files.exists(Paths.get(TEMP_FILE_PATH)), "Файл должен быть создан при загрузке.");
    }
}




