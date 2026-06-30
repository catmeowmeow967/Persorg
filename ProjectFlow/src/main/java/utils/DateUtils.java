package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final DateTimeFormatter SQL_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATETIME_FORMATTER);
    }

    public static String formatDisplayDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_DATE_FORMATTER);
    }

    public static String formatDisplayDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DISPLAY_DATETIME_FORMATTER);
    }

    public static String formatSqlDate(LocalDate date) {
        if (date == null) return "";
        return date.format(SQL_DATE_FORMATTER);
    }

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr.trim(), DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String getDueDateColor(LocalDate dueDate) {
        if (dueDate == null) return "#95a5a6";
        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, dueDate);
        if (daysUntil < 0) {
            return "#e74c3c";
        } else if (daysUntil == 0) {
            return "#e67e22";
        } else if (daysUntil <= 2) {
            return "#f1c40f";
        } else {
            return "#27ae60";
        }
    }

    public static String getDueDateText(LocalDate dueDate) {
        if (dueDate == null) return "Без срока";
        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, dueDate);
        if (daysUntil < 0) {
            return Math.abs(daysUntil) + " дней просрочено";
        } else if (daysUntil == 0) {
            return "Сегодня";
        } else if (daysUntil == 1) {
            return "Завтра";
        } else if (daysUntil <= 7) {
            return "Через " + daysUntil + " дней";
        } else {
            return formatDisplayDate(dueDate);
        }
    }

    public static boolean isOverdue(LocalDate dueDate) {
        if (dueDate == null) return false;
        return dueDate.isBefore(LocalDate.now());
    }

    public static boolean isToday(LocalDate date) {
        if (date == null) return false;
        return date.isEqual(LocalDate.now());
    }

    public static boolean isTomorrow(LocalDate date) {
        if (date == null) return false;
        return date.isEqual(LocalDate.now().plusDays(1));
    }

    public static long getDaysUntil(LocalDate date) {
        if (date == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (minutes < 1) {
            return "только что";
        } else if (minutes < 60) {
            return minutes + " мин. назад";
        } else if (hours < 24) {
            return hours + " час. назад";
        } else if (days < 30) {
            return days + " дн. назад";
        } else {
            return formatDisplayDate(dateTime.toLocalDate());
        }
    }

    public static boolean isValidDate(String dateStr) {
        try {
            parseDate(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(23, 59, 59, 999_999_999);
    }

    public static String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " сек.";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + " мин. " + remainingSeconds + " сек.";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + " час. " + minutes + " мин.";
        }
    }
}