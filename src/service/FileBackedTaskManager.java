package service;

import exception.ManagerLoadException;
import exception.ManagerSaveException;
import model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
            writer.write("ID,TYPE,NAME,STATUS,DESCRIPTION,EPIC");
            writer.newLine();

            // Сохраняем все задачи
            for (Task task : super.getTasks()) {  // Используем метод родителя для получения задач
                if (task != null) {
                    writer.write(task.toCSV());
                    writer.newLine();
                }
            }

            // Сохраняем все эпики
            for (Epic epic : super.getEpics()) {  // Используем метод родителя для получения эпиков
                if (epic != null) {
                    writer.write(epic.toCSV());
                    writer.newLine();
                }
            }

            // Сохраняем подзадачи, привязанные к эпику
            for (Subtask subtask : super.getSubtasks()) {  // Используем метод родителя для получения подзадач
                if (subtask != null) {
                    // Проверка существования эпика с данным ID
                    boolean epicExists = super.getEpics().stream()
                            .anyMatch(epic -> epic.getId() == subtask.getEpicId());

                    if (epicExists) {
                        writer.write(subtask.toCSV());
                        writer.newLine();
                    } else {
                        // Если эпик не найден, выбрасываем исключение
                        throw new IllegalArgumentException("Эпик с ID " + subtask.getEpicId() + " не существует!");
                    }
                }
            }

        } catch (IOException e) {
            // Выбрасываем исключение ManagerSaveException при ошибке записи в файл
            throw new ManagerSaveException("Ошибка записи в файл: " + e.getMessage(), e);
        }
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        try {
            saveToFile();
        } catch (ManagerSaveException e) {
            // Обрабатываем ошибку при сохранении
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
            e.printStackTrace();
        }
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        try {
            saveToFile();  // Сохраняем данные в файл
        } catch (ManagerSaveException e) {
            // Обрабатываем ошибку при сохранении
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
            e.printStackTrace();
        }
        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int id = super.addNewSubtask(subtask); // Вызываем родительский метод
        try {
            saveToFile();  // Сохраняем данные в файл
        } catch (ManagerSaveException e) {
            // Обрабатываем ошибку при сохранении
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
            e.printStackTrace();
        }
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
        try {
            super.deleteTask(id);  // Вызываем родительский метод для удаления задачи
            saveToFile();  // Сохраняем данные в файл
        } catch (ManagerSaveException e) {
            // Обработка исключения
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
            e.printStackTrace();  // Выводим стек-трейс для диагностики
        } catch (Exception e) {
            // Обработка других исключений
            System.err.println("Ошибка при удалении задачи: " + e.getMessage());
            e.printStackTrace();  // Выводим стек-трейс для диагностики
        }
    }

    @Override
    public void deleteEpic(int id) {
        try {
            super.deleteEpic(id);
            saveToFile();  // Сохраняем данные в файл
        } catch (ManagerSaveException e) {
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Обработка других исключений
            System.err.println("Ошибка при удалении эпика: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSubtask(int id) {
        try {
            super.deleteSubtask(id);
            saveToFile();
        } catch (ManagerSaveException e) {
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Обработка других исключений
            System.err.println("Ошибка при удалении подзадачи: " + e.getMessage());
            e.printStackTrace();  // Выводим стек-трейс для диагностики
        }
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
        try {
            super.deleteAllTasks();
            saveToFile();  // Сохраняем данные
        } catch (ManagerSaveException e) {
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Обработка других исключений
            System.err.println("Ошибка при удалении всех задач: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAllEpics() {
        try {
            super.deleteAllEpics();
            saveToFile();
        } catch (ManagerSaveException e) {
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Обработка других исключений
            System.err.println("Ошибка при удалении всех эпиков: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void deleteAllSubtasks() {
        try {
            super.deleteAllSubtasks();
            saveToFile();
        } catch (ManagerSaveException e) {
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Обработка других исключений
            System.err.println("Ошибка при удалении всех подзадач: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
