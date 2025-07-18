
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TaskManagerRefactored {
    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final List<String> VALID_PRIORITIES = Arrays.asList("Thấp", "Trung bình", "Cao");

private boolean validateInputs(String title, String dueDateStr, String priorityLevel) {
    if (title == null || title.trim().isEmpty()) {
        System.out.println("Lỗi: Tiêu đề không được để trống.");
        return false;
    }
    if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
        System.out.println("Lỗi: Ngày đến hạn không được để trống.");
        return false;
    }
    if (!VALID_PRIORITIES.contains(priorityLevel)) {
        System.out.println("Lỗi: Mức độ ưu tiên không hợp lệ. Vui lòng chọn từ: Thấp, Trung bình, Cao.");
        return false;
    }
    return true;
}

private LocalDate parseDate(String dateStr) {
    try {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
        System.out.println("Lỗi: Ngày đến hạn không hợp lệ. Định dạng phải là YYYY-MM-DD.");
        return null;
    }
}

private boolean isDuplicate(JSONArray tasks, String title, String dueDateStr) {
    for (Object obj : tasks) {
        JSONObject task = (JSONObject) obj;
        if (task.get("title").toString().equalsIgnoreCase(title) &&
            task.get("due_date").toString().equals(dueDateStr)) {
            System.out.printf("Lỗi: Nhiệm vụ '%s' đã tồn tại với cùng ngày đến hạn.\n", title);
            return true;
        }
    }
    return false;
}

private JSONObject createTask(String title, String description, LocalDate dueDate, String priorityLevel, boolean isRecurring) {
    JSONObject task = new JSONObject();
    task.put("id", UUID.randomUUID().toString());
    task.put("title", title);
    task.put("description", description);
    task.put("due_date", dueDate.format(DATE_FORMATTER));
    task.put("priority", priorityLevel);
    task.put("status", "Chưa hoàn thành");
    task.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
    task.put("last_updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
    if (isRecurring) {
        task.put("is_recurring", true);
        task.put("recurrence_pattern", "Chưa xác định");
    }
    return task;
}

public JSONObject addNewTask(String title, String description, String dueDateStr, String priorityLevel, boolean isRecurring) {
    if (!validateInputs(title, dueDateStr, priorityLevel)) return null;

    LocalDate dueDate = parseDate(dueDateStr);
    if (dueDate == null) return null;

    JSONArray tasks = loadTasksFromDb();
    if (isDuplicate(tasks, title, dueDateStr)) return null;

    JSONObject newTask = createTask(title, description, dueDate, priorityLevel, isRecurring);
    tasks.add(newTask);
    saveTasksToDb(tasks);

    System.out.println("Đã thêm nhiệm vụ mới thành công với ID: " + newTask.get("id"));
    return newTask;
}


    private JSONArray loadTasksFromDb() {
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = new JSONParser().parse(reader);
            return obj instanceof JSONArray ? (JSONArray) obj : new JSONArray();
        } catch (IOException | ParseException e) {
            System.err.println("Lỗi khi đọc file database: " + e.getMessage());
            return new JSONArray();
        }
    }

    private void saveTasksToDb(JSONArray tasks) {
        try (FileWriter file = new FileWriter(DB_FILE_PATH)) {
            file.write(tasks.toJSONString());
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi vào file database: " + e.getMessage());
        }
    }
}
