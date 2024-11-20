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
        // Форматируем строку в соответствии с порядком полей в конструкторе (CSV)
        return id + "," +
                title + "," +
                description + "," +
                status + "," +
                epicId + "," +
                type;
    }

    // Метод для создания объекта Subtask из строки
    public static Subtask fromString(String value) {
        // Разбиваем строку на части
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        String title = fields[1];
        String description = fields[2];
        Status status = Status.valueOf(fields[3]);
        int epicId = Integer.parseInt(fields[4]);
        TaskType type = TaskType.valueOf(fields[5]);

        return new Subtask(id, title, description, status, epicId);
    }
}

