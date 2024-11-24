package service;

import model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private String filePath;

    public FileBackedTaskManager(HistoryManager historyManager,String filePath) {
        super(historyManager); // Инициализируем родительский класс с historyManager
        this.filePath = filePath;
    }


    // Метод для загрузки данных из файла

    public static FileBackedTaskManager loadFromFile(File file) {
        HistoryManager historyManager = Managers.getDefaultHistory();
        FileBackedTaskManager manager = new FileBackedTaskManager(historyManager, file.getPath());

        int maxId = 0;  // Переменная для отслеживания максимального ID из файла

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            // Пропускаем первую строку (заголовок)
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields = line.split(",");

                int id = Integer.parseInt(fields[0]);  // Получаем ID из файла
                maxId = Math.max(maxId, id);  // Обновляем максимальный ID

                TaskType taskType = TaskType.valueOf(fields[1]); // Тип задачи
                String title = fields[2];                       // Название задачи
                Status status = Status.valueOf(fields[3]);      // Статус задачи
                String description = fields[4];                 // Описание задачи

                switch (taskType) {
                    case TASK:
                        // Используем getNextId() для задания ID
                        Task task = new Task(manager.getNextId(), title, description, status);
                        manager.addNewTask(task);

                        break;

                    case EPIC:
                        // Используем getNextId() для задания ID
                        Epic epic = new Epic(manager.getNextId(), title, description, status);
                        manager.addNewTask(epic);
                        break;

                    case SUBTASK:
                        int epicId = Integer.parseInt(fields[5]); // ID эпика
                        // Используем getNextId() для задания ID
                        Subtask subtask = new Subtask(manager.getNextId(), title, description, status, epicId);
                        manager.addNewTask(subtask);  // Добавляем подзадачу


                        Epic parentEpic = manager.epics.get(subtask.getEpicId());
                        if (parentEpic != null) {
                            parentEpic.addSubtask(subtask);
                        }
                        break;

                    default:
                        System.err.println("Неизвестный тип задачи: " + taskType);
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке данных из файла: " + e.getMessage());
        }

        // Устанавливаем nextId в качестве maxId + 1
        manager.nextId = maxId + 1;  // Следующий ID будет maxId + 1

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
        saveToFile();
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
        int id = super.addNewSubtask(subtask);
        saveToFile();
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
