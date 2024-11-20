package service;

import exception.ManagerLoadException;
import exception.ManagerSaveException;
import model.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

    private final String filePath = "src/resources/tasks.csv"; // Путь к файлу

    public FileBackedTaskManager(HistoryManager historyManager) {
        super(historyManager); // Инициализируем родительский класс с historyManager
        loadFromFile(); // Загружаем данные из файла при создании
    }

    // Метод для загрузки данных из файла
    public void loadFromFile() {
        try {
            File file = new File("src/resources/tasks.csv");

            // Если файл не существует, создаем его
            if (!file.exists()) {
                if (file.createNewFile()) {
                    System.out.println("Файл tasks.csv был успешно создан в папке src/resources.");
                } else {
                    throw new IOException("Не удалось создать файл: tasks.csv");
                }
            }

            // Используем try-with-resources для чтения из файла
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;

                // Чтение строк из файла
                while ((line = reader.readLine()) != null) {
                    // Пропускаем пустые строки
                    if (line.trim().isEmpty()) continue;

                    // Разбиваем строку на части, разделённые запятой
                    String[] fields = line.split(",");
                    // Проверяем, что количество полей соответствует необходимому
                    if (fields.length < 5) {
                        System.err.println("Неверное количество данных в строке: " + line);
                        continue; // Пропускаем строку с ошибками
                    }

                    try {
                        // Считываем данные из строки
                        int id = Integer.parseInt(fields[0]);
                        String name = fields[1];
                        String description = fields[2];
                        Status status = Status.valueOf(fields[3]);

                        TaskType taskType = TaskType.valueOf(fields[4]);

                        // В зависимости от типа задачи создаем соответствующий объект
                        switch (taskType) {
                            case TASK:
                                Task task = new Task(id, name, description, status);
                                tasks.put(task.getId(), task); // Добавляем задачу в Map
                                break;

                            case EPIC:
                                Epic epic = new Epic(id, name, description, status);
                                epics.put(epic.getId(), epic); // Добавляем эпик в Map
                                break;

                            case SUBTASK:
                                if (fields.length < 6) {
                                    System.err.println("Подзадача должна содержать ID эпика, пропуск строки.");
                                    continue;
                                }
                                int epicId = Integer.parseInt(fields[5]);
                                Subtask subtask = new Subtask(id, name, description, status, epicId);
                                subtasks.put(subtask.getId(), subtask); // Добавляем подзадачу в Map

                                // Связываем подзадачу с эпиком
                                Epic epicSubtask = epics.get(subtask.getEpicId());
                                if (epicSubtask != null) {
                                    epicSubtask.addSubtask(subtask); // Добавляем подзадачу в список подзадач эпика
                                } else {
                                    System.err.println("Не найден эпик с ID: " + subtask.getEpicId());
                                }
                                break;

                            default:
                                System.err.println("Неизвестный тип задачи: " + taskType);
                                break;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Ошибка формата числа: " + line + " — " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        // Обработка исключения IllegalArgumentException
                        System.err.println("Ошибка обработки строки: " + line + " — " + e.getMessage());
                    }
                }
            }

            System.out.println("Данные успешно загружены из файла.");
        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка при загрузке данных из файла", e);
        }
    }


    // Метод для сохранения данных в файл
    public void saveToFile() {
        Path filePath = Paths.get("src/resources/tasks.csv");
        Path directoryPath = filePath.getParent();  // Получаем путь к родительской директории

        // Проверяем, существует ли директория, если нет — создаём её
        try {
            if (Files.notExists(directoryPath)) {
                Files.createDirectories(directoryPath);  // Создаём директорию, если она не существует
            }

            // Проверяем, существует ли файл, если нет — создаём его
            if (Files.notExists(filePath)) {
                Files.createFile(filePath);  // Создаём файл, если он не существует
            }

        } catch (IOException e) {
            // Выбрасываем исключение ManagerSaveException при ошибке создания файла или директории
            throw new ManagerSaveException("Ошибка при создании директории или файла: " + e.getMessage(), e);
        }

        // Используем try-with-resources для записи в файл
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            // Сохраняем все задачи в файл
            for (Task task : getTasks()) {
                if (task == null) {
                    throw new ManagerSaveException("Невалидные данные задачи при сохранении.");
                }
                writer.write(task.toString());
                writer.newLine();
            }
            for (Epic epic : getEpics()) {
                if (epic == null) {
                    throw new ManagerSaveException("Невалидные данные эпика при сохранении.");
                }
                writer.write(epic.toString());
                writer.newLine();
            }
            for (Subtask subtask : getSubtasks()) {
                if (subtask == null) {
                    throw new ManagerSaveException("Невалидные данные подзадачи при сохранении.");
                }
                writer.write(subtask.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            // Выбрасываем исключение ManagerSaveException при ошибке записи в файл
            throw new ManagerSaveException("Ошибка записи в файл: " + e.getMessage(), e);
        } catch (ManagerSaveException e) {
            // Логируем или перенаправляем исключение, если данные неверны
            System.err.println("Ошибка данных: " + e.getMessage());
            throw e;
        }
    }


    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        saveToFile(); // Сохраняем данные
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        saveToFile();
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        Integer id = super.addNewSubtask(subtask);
        saveToFile();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        saveToFile();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        saveToFile();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        saveToFile();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        saveToFile();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        saveToFile();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        saveToFile();
    }

    // Дополнительные методы для получения задач

    @Override
    public List<Task> getTasks() {
        return super.getTasks();
    }

    @Override
    public List<Epic> getEpics() {
        return super.getEpics();
    }

    @Override
    public List<Subtask> getSubtasks() {
        return super.getSubtasks();
    }

    @Override
    public Task getTask(int id) {
        return super.getTask(id);
    }

    @Override
    public Subtask getSubtask(int id) {
        return super.getSubtask(id);
    }

    @Override
    public Epic getEpic(int id) {
        return super.getEpic(id);
    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        saveToFile(); // Сохраняем данные в файл
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        saveToFile(); // Сохраняем данные в файл
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        saveToFile(); // Сохраняем данные в файл
    }
}





