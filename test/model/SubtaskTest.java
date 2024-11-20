package model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SubtaskTest {
    @Test
    void testSubtaskCreation() {
        Subtask subtask = new Subtask(1, "Subtask 1", "Description of subtask 1", Status.NEW, 100);

        // Проверяем, что ID эпика установлен правильно
        assertEquals(100, subtask.getEpicId());
        // Проверяем, что другие параметры установлены правильно
        assertEquals(1, subtask.getId());
        assertEquals("Subtask 1", subtask.getTitle());
        assertEquals("Description of subtask 1", subtask.getDescription());
        assertEquals(Status.NEW, subtask.getStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        Subtask subtask1 = new Subtask(1, "Subtask 1", "Description of subtask 1", Status.NEW, 100);
        Subtask subtask2 = new Subtask(1, "Subtask 1", "Description of subtask 1", Status.NEW, 100);
        Subtask subtask3 = new Subtask(2, "Subtask 2", "Description of subtask 2", Status.DONE, 101);

        // Проверяем, что два идентичных подзадачи равны
        assertEquals(subtask1, subtask2);
        assertEquals(subtask1.hashCode(), subtask2.hashCode());

        // Проверяем, что разные подзадачи не равны
        assertNotEquals(subtask1, subtask3);
    }

    @Test
    void testToString() {
        // Создаём объект Subtask
        Subtask subtask = new Subtask(1, "Subtask Title", "Subtask Description", Status.IN_PROGRESS, 101);

        // Ожидаемая строка в формате CSV
        String expected = "1,Subtask Title,Subtask Description,IN_PROGRESS,101,SUBTASK";

        // Проверяем результат метода toString
        assertEquals(expected, subtask.toString(), "Метод toString должен возвращать корректную строку в формате CSV.");
    }
}
