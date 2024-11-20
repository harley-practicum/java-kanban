package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {

    private final List<Subtask> subtasks; // Список подзадач

    public Epic(int id, String title, String description, Status status) {
        super(id, title, description, status);
        this.subtasks = new ArrayList<>(); // Инициализация списка подзадач
        this.type = TaskType.EPIC; // Устанавливаем тип как EPIC
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask); // Добавление подзадачи в список
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void removeSubtask(int id) {
        Subtask subtaskToRemove = null;
        for (Subtask subtask : subtasks) {
            if (subtask.getId() == id) {
                subtaskToRemove = subtask;
                break;
            }
        }
        if (subtaskToRemove != null) {
            subtasks.remove(subtaskToRemove); // Удаляем подзадачу из списка
        }
    }

    public void updateSubtask(Subtask updatedSubtask) {
        // Находим подзадачу по ID и удаляем её из списка
        removeSubtask(updatedSubtask.getId());

        // Добавляем обновлённую версию подзадачи в список
        addSubtask(updatedSubtask);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Epic)) return false;
        Epic epic = (Epic) o;
        return id == epic.id; // Сравниваем только по id
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // hashCode только по id
    }

    @Override
    public String toString() {
        // Возвращаем строку в формате CSV в порядке: id, title, description, status, type, subtasks.size()
        return id + "," +
                title + "," +
                description + "," +
                status + "," +
                type + "," +
                subtasks.size(); // Добавляем количество подзадач
    }

    // Метод для создания объекта Epic из строки
    public static Epic fromString(String value) {
        // Разбиваем строку на части
        String[] fields = value.split(",");

        // Получаем значения из строки
        int id = Integer.parseInt(fields[0]);
        String title = fields[1];
        String description = fields[2];
        Status status = Status.valueOf(fields[3]);

        // Создаём объект Epic
        Epic epic = new Epic(id, title, description, status);

        // Возвращаем созданный объект Epic
        return epic;
    }
}

