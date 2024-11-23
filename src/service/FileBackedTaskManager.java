package service;

import exception.ManagerLoadException;
import exception.ManagerSaveException;
import model.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private String filePath;

    public FileBackedTaskManager(HistoryManager historyManager,String filePath) {
        super(historyManager); // Инициализируем родительский класс с historyManager
        this.filePath = filePath;
    }


    // Метод для загрузки данных из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        // Проверяем, существует ли файл
        if (!file.exists()) {
            System.err.println("Файл не найден: " + file.getPath());
            return null;
        }

        // Получаем дефолтный historyManager
        HistoryManager historyManager = Managers.getDefaultHistory();

        // Создаём менеджер с пустым historyManager
        FileBackedTaskManager manager = new FileBackedTaskManager(historyManager, file.getPath());

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            // Пропускаем первую строку (заголовок)
            reader.readLine();

            // Чтение остальных строк из файла
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // Пропускаем пустые строки
                }

                String[] fields = line.split(",");
                System.out.println("Чтение строки: " + line);
                System.out.println("Разделённые данные: " + Arrays.toString(fields));

                if (fields.length < 5) {
                    System.err.println("Неверное количество данных в строке: " + line);
                    continue;
                }

                try {
                    // Парсим данные с учётом вашей очередности
                    int id = Integer.parseInt(fields[0]);          // ID задачи
                    TaskType taskType = TaskType.valueOf(fields[1]); // Тип задачи
                    String title = fields[2];                     // Название задачи
                    Status status = Status.valueOf(fields[3]);    // Статус задачи
                    String description = fields[4];              // Описание задачи

                    switch (taskType) {
                        case TASK:
                            Task task = new Task(id, title, description, status);
                            manager.tasks.put(task.getId(), task);
                            break;

                        case EPIC:
                            Epic epic = new Epic(id, title, description, status);
                            manager.epics.put(epic.getId(), epic);
                            break;

                        case SUBTASK:
                            if (fields.length < 6) {
                                System.err.println("Подзадача должна содержать ID эпика, пропуск строки.");
                                continue;
                            }
                            int epicId = Integer.parseInt(fields[5]); // ID эпика
                            Subtask subtask = new Subtask(id, title, description, status, epicId);
                            manager.subtasks.put(subtask.getId(), subtask);

                            Epic parentEpic = manager.epics.get(subtask.getEpicId());
                            if (parentEpic != null) {
                                parentEpic.addSubtask(subtask);
                            } else {
                                System.err.println("Не найден эпик с ID: " + subtask.getEpicId());
                            }
                            break;

                        default:
                            System.err.println("Неизвестный тип задачи: " + taskType);
                            break;
                    }

                } catch (NumberFormatException e) {
                    System.err.println("Ошибка формата числа для ID: " + line + " — " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.err.println("Ошибка обработки строки: " + line + " — " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка при загрузке данных из файла", e);
        }

        System.out.println("Данные успешно загружены из файла.");
        return manager;
    }

    // Метод для сохранения данных в файл
    private void saveToFile() {
        // Используем try-with-resources для записи в файл
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            // Записываем заголовок
            writer.write("ID,TYPE,NAME,STATUS,DESCRIPTION,EPIC");
            writer.newLine();

            // Сохраняем все задачи
            for (Task task : super.getTasks()) { // Используем метод родителя для получения задач
                writer.write(task.toCSV());
                writer.newLine();
            }

            // Сохраняем все эпики
            for (Epic epic : super.getEpics()) { // Используем метод родителя для получения эпиков
                writer.write(epic.toCSV());
                writer.newLine();
            }

            // Сохраняем все подзадачи
            for (Subtask subtask : super.getSubtasks()) { // Используем метод родителя для получения подзадач
                writer.write(subtask.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при записи данных в файл: " + filePath, e);
        }
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        saveToFile(); // Убираем обработку исключений
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        saveToFile(); // Убираем обработку исключений
        return id;
    }


    @Override
    public int addNewSubtask(Subtask subtask) {
        int id = super.addNewSubtask(subtask); // Вызываем родительский метод
        saveToFile(); // Убираем обработку исключений
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        try {
            saveToFile();
        } catch (ManagerSaveException e) {
            // Обрабатываем ошибку при сохранении
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        try {
            saveToFile();
        } catch (ManagerSaveException e) {
            // Обрабатываем ошибку при сохранении данных
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        try {
            super.updateSubtask(subtask);
            saveToFile();  // Сохраняем данные в файл
        } catch (ManagerSaveException e) {
            // Обработка исключения при сохранении данных
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Обработка других исключений, которые могут возникнуть при обновлении подзадачи
            System.err.println("Ошибка при обновлении подзадачи: " + e.getMessage());
            e.printStackTrace();  // Выводим стек-трейс для диагностики
        }
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);  // Вызываем родительский метод для удаления задачи
        saveToFile();  // Сохраняем данные в файл
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);  // Вызываем родительский метод для удаления эпика
        saveToFile();  // Сохраняем данные в файл
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);  // Вызываем родительский метод
        saveToFile();  // Сохраняем данные в файл
    }


    // Дополнительные методы для получения задач
    public List<Task> getTasks() {
        return super.getTasks();
    }

    public List<Epic> getEpics() {
        return super.getEpics();
    }

    public List<Subtask> getSubtasks() {
        return super.getSubtasks();
    }

    public Task getTask(int id) {
        return super.getTask(id);
    }

    public Subtask getSubtask(int id) {
        return super.getSubtask(id);
    }

    public Epic getEpic(int id) {
        return super.getEpic(id);
    }

    public List<Task> getHistory() {
        return super.getHistory();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();  // Вызываем родительский метод для удаления всех задач
        saveToFile();  // Сохраняем данные в файл
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();  // Вызываем родительский метод для удаления всех эпиков
        saveToFile();  // Сохраняем данные в файл
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        saveToFile();  // Сохраняем данные в файл
    }

}
