package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.io.IOException;
import java.util.List;

public interface TaskManager {

    List<Task> getTasks();

    List<Epic> getEpics();

    List<Subtask> getSubtasks();

    List<Subtask> getEpicSubtasks(int epicId);

    Task getTask(int id);

    Subtask getSubtask(int id);

    Epic getEpic(int id);

    int addNewTask(Task task) throws IOException;

    int addNewEpic(Epic epic) throws IOException;

    int addNewSubtask(Subtask subtask) throws IOException;

    void deleteEpic(int id) throws IOException;

    void updateTask(Task task) throws IOException;

    void updateEpic(Epic epic) throws IOException;

    void updateSubtask(Subtask subtask) throws IOException;

    void deleteTask(int id) throws IOException;

    void deleteSubtask(int id) throws IOException;

    List<Task> getHistory();

    void deleteAllTasks() throws IOException;

    void deleteAllEpics() throws IOException;

    void deleteAllSubtasks() throws IOException;

}