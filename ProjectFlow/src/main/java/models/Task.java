package models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private int id;
    private int columnId;
    private String title;
    private String description;
    private int position;
    private Priority priority;
    private LocalDate dueDate;
    private boolean isCompleted;
    private boolean isArchived;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> labels = new ArrayList<>();
    private String assignedMember;

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public Task() {
        this.isArchived = false;
        this.priority = Priority.MEDIUM;
        this.position = 0;
        this.isCompleted = false;
        this.labels = new ArrayList<>();
    }

    public Task(int columnId, String title, String description) {
        this.columnId = columnId;
        this.title = title;
        this.description = description;
        this.priority = Priority.MEDIUM;
        this.position = 0;
        this.isCompleted = false;
        this.isArchived = false;
        this.labels = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getColumnId() {
        return columnId;
    }

    public void setColumnId(int columnId) {
        this.columnId = columnId;
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void addLabel(String label) {
        if (label != null && !label.trim().isEmpty()) {
            String normalizedLabel = label.trim().toUpperCase();
            if (!labels.contains(normalizedLabel)) {
                labels.add(normalizedLabel);
            }
        }
    }

    public void removeLabel(String label) {
        labels.remove(label.trim().toUpperCase());
    }

    public boolean hasLabel(String label) {
        return labels.contains(label.trim().toUpperCase());
    }

    public boolean hasAnyLabel(List<String> searchLabels) {
        if (searchLabels == null || searchLabels.isEmpty()) {
            return false;
        }
        for (String searchLabel : searchLabels) {
            if (labels.contains(searchLabel.trim().toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public String getLabelsAsString() {
        if (labels.isEmpty()) {
            return "";
        }
        return String.join(", ", labels);
    }

    public String getAssignedMember() {
        return assignedMember;
    }

    public void setAssignedMember(String assignedMember) {
        this.assignedMember = assignedMember;
    }

    public boolean isAssigned() {
        return assignedMember != null && !assignedMember.trim().isEmpty();
    }

    public String getPriorityColor() {
        if (priority == null) return "#5e6c84";
        switch (priority) {
            case LOW: return "#61bd4f";
            case MEDIUM: return "#f2d600";
            case HIGH: return "#ff9f1a";
            case CRITICAL: return "#eb5a46";
            default: return "#5e6c84";
        }
    }

    public String getPriorityText() {
        if (priority == null) return "НЕТ";
        switch (priority) {
            case LOW: return "НИЗКИЙ";
            case MEDIUM: return "СРЕДНИЙ";
            case HIGH: return "ВЫСОКИЙ";
            case CRITICAL: return "КРИТИЧЕСКИЙ";
            default: return "НЕТ";
        }
    }

    public String getPriorityTextColor() {
        return priority == Priority.MEDIUM ? "#172b4d" : "white";
    }

    public boolean isOverdue() {
        if (dueDate == null || isCompleted) return false;
        return dueDate.isBefore(java.time.LocalDate.now());
    }

    public String getDueDateText() {
        if (dueDate == null) return "Без срока";
        java.time.LocalDate today = java.time.LocalDate.now();
        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate);
        if (daysUntil == 0) return "Сегодня";
        if (daysUntil == 1) return "Завтра";
        if (daysUntil == -1) return "Вчера";
        if (daysUntil < 0) return Math.abs(daysUntil) + "д назад";
        if (daysUntil <= 7) return "Через " + daysUntil + "д";
        return dueDate.toString();
    }

    public String getDueDateColor() {
        if (dueDate == null) return "#5e6c84";
        java.time.LocalDate today = java.time.LocalDate.now();
        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate);
        if (daysUntil < 0) return "#eb5a46";
        if (daysUntil == 0) return "#f2d600";
        if (daysUntil <= 2) return "#ff9f1a";
        return "#61bd4f";
    }

    public String getDueDateIcon() {
        if (dueDate == null) return "📅";
        java.time.LocalDate today = java.time.LocalDate.now();
        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate);
        if (daysUntil < 0) return "⚠️";
        if (daysUntil == 0) return "⚡";
        if (daysUntil == 1) return "⏰";
        return "📅";
    }

    public String getLabelColor(String label) {
        if (label == null) return "#5e6c84";
        String normalizedLabel = label.trim().toUpperCase();
        switch (normalizedLabel) {
            case "БАГ":
            case "BUG":
            case "ОШИБКА":
            case "СРОЧНО":
            case "ПРОСРОЧЕНО":
            case "БЛОКЕР":
                return "#eb5a46";
            case "ФИЧА":
            case "FEATURE":
            case "УЛУЧШЕНИЕ":
            case "НИЗКИЙ":
                return "#61bd4f";
            case "ДИЗАЙН":
            case "DESIGN":
            case "UI":
            case "UX":
                return "#00c2e0";
            case "ТЕСТ":
            case "TEST":
            case "QA":
                return "#f2d600";
            case "ДОКУМЕНТАЦИЯ":
            case "DOCS":
                return "#c377e0";
            case "СРЕДНИЙ":
                return "#f2d600";
            case "ВЫСОКИЙ":
                return "#ff9f1a";
            case "КРИТИЧЕСКИЙ":
                return "#eb5a46";
            default:
                int hash = normalizedLabel.hashCode();
                int r = Math.abs(hash % 200 + 55);
                int g = Math.abs((hash / 256) % 200 + 55);
                int b = Math.abs((hash / 65536) % 200 + 55);
                return String.format("#%02X%02X%02X", r, g, b);
        }
    }

    public String getPrimaryLabel() {
        if (labels.isEmpty()) {
            return null;
        }
        return labels.get(0);
    }

    public String getPrimaryLabelColor() {
        String primaryLabel = getPrimaryLabel();
        if (primaryLabel == null) {
            return "#5e6c84";
        }
        return getLabelColor(primaryLabel);
    }

    public void autoDetectLabels() {
        String text = (title + " " + description).toLowerCase();
        if (text.contains("баг") || text.contains("bug") || text.contains("ошибка")) {
            addLabel("БАГ");
        }
        if (text.contains("фича") || text.contains("feature") || text.contains("улучшение")) {
            addLabel("ФИЧА");
        }
        if (text.contains("дизайн") || text.contains("design") || text.contains("ui") || text.contains("ux")) {
            addLabel("ДИЗАЙН");
        }
        if (text.contains("тест") || text.contains("test") || text.contains("qa")) {
            addLabel("ТЕСТ");
        }
        if (text.contains("документ") || text.contains("docs")) {
            addLabel("ДОКУМЕНТАЦИЯ");
        }
        if (isOverdue()) {
            addLabel("ПРОСРОЧЕНО");
        }
        if (priority == Priority.CRITICAL || text.contains("срочно") || text.contains("urgent")) {
            addLabel("СРОЧНО");
        }
    }

    public String getMemberColor() {
        if (assignedMember == null) {
            return "#5e6c84";
        }
        int hash = assignedMember.hashCode();
        int r = Math.abs(hash % 200 + 55);
        int g = Math.abs((hash / 256) % 200 + 55);
        int b = Math.abs((hash / 65536) % 200 + 55);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    public boolean isNew() {
        if (createdAt == null) return false;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long hoursSinceCreation = java.time.temporal.ChronoUnit.HOURS.between(createdAt, now);
        return hoursSinceCreation < 24;
    }

    public long getDaysInProgress() {
        if (isCompleted || createdAt == null) return 0;
        java.time.LocalDate today = java.time.LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(createdAt.toLocalDate(), today);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        if (!labels.isEmpty()) {
            sb.append(" [").append(getLabelsAsString()).append("]");
        }
        if (isCompleted) {
            sb.append(" ✓");
        }
        return sb.toString();
    }
    public Task copy() {
        Task copy = new Task(this.columnId, this.title, this.description);
        copy.setPriority(this.priority);
        copy.setDueDate(this.dueDate);
        copy.setPosition(this.position);
        copy.setLabels(new ArrayList<>(this.labels));
        copy.setAssignedMember(this.assignedMember);
        return copy;
    }
    public void setColumnIdSilent(int columnId) {
        this.columnId = columnId;
    }
    public boolean containsText(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return false;
        }
        String lowerSearch = searchText.toLowerCase();
        return (title != null && title.toLowerCase().contains(lowerSearch)) ||
                (description != null && description.toLowerCase().contains(lowerSearch)) ||
                labels.stream().anyMatch(label -> label.toLowerCase().contains(lowerSearch)) ||
                (assignedMember != null && assignedMember.toLowerCase().contains(lowerSearch));
    }
}