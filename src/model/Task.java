package model;

public class Task {

    protected int id;
    protected String title; // Название задач
    protected String description;
    protected Status status; // Статус задачи
    protected TaskType type; // Тип задачи

    // Конструктор
    public Task(int id, String title, String description, Status status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.type = TaskType.TASK; // Тип задачи по умолчанию
    }

    // Геттеры и сеттеры
    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TaskType getType() {
        return type; // Возвращаем тип задачи
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Сравнение ссылок
        if (!(o instanceof Task)) return false; // Проверка на тип
        Task task = (Task) o; // Приведение типа
        return id == task.id; // Сравнение по id
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id); // Хеш-код зависит только от id
    }

    @Override
    public String toString() {
        // Форматируем объект в строку с полями в порядке: id, title, description, status, type
        return id + "," +
                title + "," +
                description + "," +
                status + "," +
                type;
    }

    public static Task fromString(String value) {
        // Разбиваем строку на части
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        String title = fields[1];
        String description = fields[2];
        Status status = Status.valueOf(fields[3]);
        TaskType type = TaskType.valueOf(fields[4]);

        // Создаем объект задачи
        return new Task(id, title, description, status);
    }
}
