package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUtils {
    private static final String ATTACHMENTS_DIR = "attachments";
    static {
        try {
            Files.createDirectories(Paths.get(ATTACHMENTS_DIR));
        } catch (IOException e) {
            System.err.println("Не удалось создать директорию вложений: " + e.getMessage());
        }
    }

    public static String saveFile(File sourceFile, int taskId) throws IOException {
        String taskDir = ATTACHMENTS_DIR + File.separator + taskId;
        Files.createDirectories(Paths.get(taskDir));
        String destPath = taskDir + File.separator + sourceFile.getName();
        Path destination = Paths.get(destPath);
        Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destPath;
    }

    public static void deleteFile(String filePath) throws IOException {
        Files.deleteIfExists(Paths.get(filePath));
    }

    public static File getFile(String filePath) {
        return new File(filePath);
    }
}