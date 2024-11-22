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
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }

    public String toCSV() {
        return id + "," +
                type + "," +
                title + "," +
                status + "," +
                description ;
    }

}
