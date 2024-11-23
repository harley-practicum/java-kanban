package service;

import exception.ManagerLoadException;
import model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

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
    private void saveToFile() throws IOException {
        // Используем try-with-resources для записи в файл
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath));

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
    }



    @Override
    public int addNewTask(Task task) throws IOException {
        int id = super.addNewTask(task);
        saveToFile(); // Убираем обработку исключений
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) throws IOException {
        int id = super.addNewEpic(epic);
        saveToFile(); // Убираем обработку исключений
        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) throws IOException {
        int id = super.addNewSubtask(subtask); // Вызываем родительский метод
        saveToFile(); // Убираем обработку исключений
        return id;
    }

    @Override
    public void updateTask(Task task) throws IOException {
        super.updateTask(task);
        saveToFile();
    }

    @Override
    public void updateEpic(Epic epic) throws IOException {
        super.updateEpic(epic);
        saveToFile();
    }
    
    @Override
    public void updateSubtask(Subtask subtask) throws IOException {
        super.updateSubtask(subtask);
        saveToFile(); // Сохраняем данные в файл
    }

    @Override
    public void deleteTask(int id) throws IOException {
        super.deleteTask(id);  // Вызываем родительский метод для удаления задачи
        saveToFile();  // Сохраняем данные в файл
    }

    @Override
    public void deleteEpic(int id) throws IOException {
        super.deleteEpic(id);  // Вызываем родительский метод для удаления эпика
        saveToFile();  // Сохраняем данные в файл
    }

    @Override
    public void deleteSubtask(int id) throws IOException {
        super.deleteSubtask(id);  // Вызываем родительский метод
        saveToFile();  // Сохраняем данные в файл
    }

    @Override
    public void deleteAllTasks() throws IOException {
        super.deleteAllTasks();  // Вызываем родительский метод для удаления всех задач
        saveToFile();  // Сохраняем данные в файл
    }

    @Override
    public void deleteAllEpics() throws IOException {
        super.deleteAllEpics();  // Вызываем родительский метод для удаления всех эпиков
        saveToFile();  // Сохраняем данные в файл
    }

    @Override
    public void deleteAllSubtasks() throws IOException {
        super.deleteAllSubtasks();
        saveToFile();  // Сохраняем данные в файл
    }
}
