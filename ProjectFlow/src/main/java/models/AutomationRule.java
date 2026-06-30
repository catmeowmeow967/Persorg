package models;

public class AutomationRule {
    private String description;
    private String trigger;
    private String action;
    private String parameters;
    private String status;
    private int executionCount;

    public AutomationRule(String trigger, String action, String parameters, String status) {
        this.trigger = trigger;
        this.action = action;
        this.parameters = parameters;
        this.status = status;
        this.executionCount = 0;
        updateDescription();
    }

    private void updateDescription() {
        this.description = trigger + " → " + action + ": " + parameters;
    }

    public String getDescription() {
        return description;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getAction() {
        return action;
    }

    public String getParameters() {
        return parameters;
    }

    public String getStatus() {
        return status;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
        updateDescription();
    }

    public void incrementExecution() {
        executionCount++;
    }

    public String getStatusColor() {
        return status.equals("Активно") ? "#61bd4f" : "#5e6c84";
    }

    public String getTriggerIcon() {
        switch (trigger) {
            case "Задача создана": return "➕";
            case "Задача перемещена": return "🔄";
            case "Срок истек": return "⏰";
            case "Изменен приоритет": return "⚠️";
            case "Задача выполнена": return "✅";
            default: return "📌";
        }
    }

    public String getActionIcon() {
        switch (action) {
            case "Добавить метку": return "🏷️";
            case "Изменить приоритет": return "⚠️";
            case "Переместить в колонку": return "➡️";
            case "Назначить участника": return "👤";
            case "Установить срок": return "📅";
            default: return "⚡";
        }
    }
}