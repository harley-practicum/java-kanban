package model;

import java.util.Objects;

public class Subtask extends Task {

    private int epicId; // ID эпика, к которому относится подзадача

    // Конструктор
    public Subtask(int id, String title, String description, Status status, int epicId) {
        super(id, title, description, status);
        this.epicId = epicId; // Установка ID эпика
        this.type = TaskType.SUBTASK; // Устанавливаем тип как SUBTASK
    }

    public int getEpicId() {
        return epicId; // Получение ID эпика
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subtask)) return false; // Проверка типа объекта
        Subtask subtask = (Subtask) o;
        return id == subtask.id; // Сравниваем только по id
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Генерируем hashCode только по id
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", epicId=" + epicId +
                '}';
    }

    @Override
    public String toCSV() {
        return id + "," +
                type + "," +
                title + "," +
                status + "," +
                description + "," +
                epicId + ",subtask";
    }
}

