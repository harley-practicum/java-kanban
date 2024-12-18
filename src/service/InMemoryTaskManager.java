package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.*;


public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>(); // Задачи
    private final Map<Integer, Epic> epics = new HashMap<>(); // Эпики
    private final Map<Integer, Subtask> subtasks = new HashMap<>(); // Подзадачи
    protected HistoryManager historyManager; // Менеджер истории
    private int nextId = 1; // Счетчик для ID

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public int addNewTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task не может быть null.");
        }
        int id = nextId++; // Получаем следующий уникальный ID
        task.setId(id); // Устанавливаем уникальный ID для задачи
        tasks.put(id, task); // Сохраняем задачу в коллекции
        return id; // Возвращаем ID добавленной задачи
    }

    @Override
    public int addNewEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Эпик не может быть null.");
        }

        int id = nextId++; // Получаем следующий уникальный ID
        epic.setId(id); // Устанавливаем уникальный ID для эпика
        epics.put(id, epic); // Сохраняем эпик в коллекции
        return id; // Возвращаем ID добавленного эпика
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Подзадача не может быть null.");
        }

        // Проверяем, что epicId задан верно и такой эпик есть
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new NoSuchElementException("Epic с ID " + subtask.getEpicId() + " не найден.");
        }

        // Проверяем, что ID подзадачи уникален
        if (subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Подзадача с ID " + subtask.getId() + " уже существует.");
        }

        int id = nextId++; // Устанавливаем id и добавляем подзадачу в хранилище
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addSubtask(subtask); // Добавляем ID подзадачи в эпик
        updateEpicStatus(epic.getId()); // Обновляем статус эпика, так как была добавлена новая подзадача
        return id; // Возвращаем ID добавленной подзадачи
    }

    @Override
    public List<Task> getTasks() {
        // Проверяем, что карта задач не null
        if (tasks == null) {
            System.out.println("Задач нет");
            return new ArrayList<>(); // Возвращаем пустой список, если задач нет
        }
        // Возвращаем список всех задач
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        if (epics == null) {
            System.out.println("Эпиков нет.");
        }
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        // Проверяем, что карта подзадач не null
        if (subtasks == null) {
            System.out.println("Подзадач нет.");
        }
        // Возвращаем список всех подзадач
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        // Находим эпик по его ID
        Epic epic = epics.get(epicId);

        // Проверяем, существует ли эпик
        if (epic == null) {
            throw new NoSuchElementException("Эпик с ID " + epicId + " не существует.");
        }

        // Получаем список подзадач этого эпика
        List<Subtask> epicSubtasks = new ArrayList<>();

        // Проходим по всем подзадачам, связанным с эпиком
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epicId) {
                epicSubtasks.add(subtask);
            }
        }

        // Если подзадач нет, можно вернуть пустой список или выбросить исключение
        if (epicSubtasks.isEmpty()) {
            System.out.println("У эпика с ID " + epicId + " нет подзадач.");
        }

        // Возвращаем список подзадач
        return epicSubtasks;
    }

    @Override
    public Task getTask(int id) {
        // Проверка на null перед доступом к коллекции
        if (tasks == null) {
            throw new IllegalStateException("Map задач не инициализирована.");
        }

        Task task = tasks.get(id); // Получаем задачу по ID

        if (task == null) {
            // Если задача не найдена, выбрасываем исключение
            throw new NoSuchElementException("Task с таким id " + id + " не существует.");
        }

        historyManager.add(task); // Добавляем задачу в историю
        return task; // Возвращаем найденную задачу
    }

    @Override
    public Epic getEpic(int id) {
        // Проверяем, существует ли эпик с заданным ID
        Epic epic = epics.get(id); // Получаем эпик по ID
        if (epic == null) {
            // Если эпика нет, выбрасываем исключение
            throw new NoSuchElementException("Epic with id " + id + " does not exist."); // Изменено на английский текст
        }
        historyManager.add(epic); // Добавляем эпик в историю
        return epic; // Возвращаем найденный эпик
    }

    @Override
    public Subtask getSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            throw new NoSuchElementException("Подзадача с ID " + id + " не существует.");
        }
        return subtasks.get(id);
    }

    @Override
    public void updateTask(Task task) {
        // Проверяем, существует ли задача с заданным ID
        if (task == null || !tasks.containsKey(task.getId())) {
            throw new NoSuchElementException("Task c id " + task.getId() + " не существует.");
        }
        tasks.put(task.getId(), task);// Обновляем задачу целиком
    }

    @Override
    public void updateEpic(Epic epic) {
        // Проверяем, существует ли эпик с заданным ID
        if (epic == null || !epics.containsKey(epic.getId())) {
            throw new NoSuchElementException("Эпик с ID "  + epic.getId() +  "не существует.");
        }

        Epic existingEpic = epics.get(epic.getId());
        // Обновляем параметры эпика, кроме статуса
        existingEpic.setTitle(epic.getTitle());  // Обновляем название
        existingEpic.setDescription(epic.getDescription());  // Обновляем описание
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            throw new NoSuchElementException("Подзадача с ID " + subtask.getId() + " не существует.");
        }

        // Находим эпик, к которому относится подзадача, используя epicId
        Epic epic = (Epic) tasks.get(subtask.getEpicId());
        if (epic != null) {
            // Обновляем подзадачу в эпике
            epic.updateSubtask(subtask);

            // Пересчитываем статус эпика
            updateEpicStatus(epic.getId());
        }

        // Обновляем подзадачу в общем списке подзадач
        subtasks.put(subtask.getId(), subtask);
    }


    @Override
    public void deleteTask(int id) {
        if (!tasks.containsKey(id)) {
            throw new NoSuchElementException("Задача с ID " + id + " не существует.");
        }
        historyManager.remove(id);
        tasks.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            throw new NoSuchElementException("Подзадача с id " + id + " не существует.");
        }
        Subtask subtask = subtasks.get(id); // Получаем подзадачу
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.removeSubtask(id); // Удаляем подзадачу из эпика
            updateEpicStatus(epicId); // Обновляем статус эпика после удаления подзадачи
        }
        subtasks.remove(id); // Удаляем подзадачу из глобального списка подзадач
        historyManager.remove(id); // Удаляем подзадачу из истории
    }

    @Override
    public void deleteEpic(int id) {
        // Получаем эпик по ID
        Epic epic = epics.get(id);
        if (epic == null) { // Проверяем, существует ли эпик
            throw new NoSuchElementException("Эпик с ID " + id + " не существует.");
        }

        List<Subtask> subtasksToRemove = epic.getSubtasks();
        for (Subtask subtask : subtasksToRemove) { // Удаляем все подзадачи, связанные с эпиком
            int subtaskId = subtask.getId();
            subtasks.remove(subtaskId); // Удаляем подзадачу из глобального списка подзадач
            historyManager.remove(subtaskId); // Удаляем подзадачу из истории
        }

        epics.remove(id); // Удаляем сам эпик
        historyManager.remove(id); // Удаляем эпик из истории
    }

    @Override
    public void deleteAllTasks() {
        if (tasks.isEmpty()) { // Проверяем, есть ли задачи
            System.out.println("Нет задач для удаления.");
            return;
        }

        // Удаляем каждую задачу из истории
        for (Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId); // Удаляем задачу из истории
        }

        tasks.clear(); // Очищаем коллекцию задач
        System.out.println("Все задачи удалены.");
    }



    @Override
    public void deleteAllEpics() {
        if (epics.isEmpty()) { // Проверяем, есть ли эпики в коллекции
            System.out.println("Нет эпиков для удаления.");
            return;
        }

        // Удаляем подзадачи из истории
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }

        // Удаляем эпики из истории
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }

        // Очищаем коллекции подзадач и эпиков
        subtasks.clear();
        epics.clear();
        System.out.println("Все эпики и связанные с ними подзадачи удалены.");
    }


    @Override
    public void deleteAllSubtasks() {
        if (subtasks.isEmpty()) {
            System.out.println("Нет подзадач для удаления."); // Сообщаем, если подзадач нет
            return;
        }

        // Удаляем все подзадачи из истории
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId()); // Удаляем подзадачу из истории
        }

        // Проходим по всем эпикам, удаляем все подзадачи и сразу устанавливаем статус NEW
        for (Epic epic : epics.values()) {
            // Очищаем все подзадачи у эпика
            epic.getSubtasks().clear();

            // Устанавливаем статус эпика в NEW
            epic.setStatus(Status.NEW);
        }

        // Очистка коллекции подзадач
        subtasks.clear();
        System.out.println("Все подзадачи удалены.");
    }


    public List<Task> getHistory() {
        return historyManager.getHistory(); // Возвращаем историю задач из менеджера истории
    }


    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId); // Получаем эпик по ID
        if (epic == null) {
            return; // Если эпик не найден, выходим
        }
        List<Subtask> subtasks = epic.getSubtasks(); // Получаем список подзадач
        if (subtasks.isEmpty()) {
            epic.setStatus(Status.NEW); // Если подзадач нет, устанавливаем статус NEW
            return;
        }
        int newCount = 0;
        int inProgressCount = 0;
        int doneCount = 0;
        // Перебираем все подзадачи и считаем их статусы
        for (Subtask subtask : subtasks) {
            switch (subtask.getStatus()) {
                case NEW:
                    newCount++;
                    break;
                case IN_PROGRESS:
                    inProgressCount++;
                    break;
                case DONE:
                    doneCount++;
                    break;
            }
        }

        // Обновляем статус эпика на основе подсчитанных значений
        if (doneCount == subtasks.size()) {
            epic.setStatus(Status.DONE); // Все подзадачи выполнены
        } else if (inProgressCount > 0) {
            epic.setStatus(Status.IN_PROGRESS); // Есть хотя бы одна подзадача в процессе
        } else {
            epic.setStatus(Status.NEW);
        }

    }

}