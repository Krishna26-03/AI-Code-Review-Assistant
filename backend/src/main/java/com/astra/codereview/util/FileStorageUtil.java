package com.astra.codereview.util;

import com.astra.codereview.exception.ApiExceptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class FileStorageUtil {

    @Value("${app.storage.upload-dir}")
    private String uploadDir;

    @Value("${app.storage.report-dir}")
    private String reportDir;

    /**
     * Persists an uploaded file under uploads/{userId}/{uuid}/ and, if it's a zip,
     * extracts it in place (guarding against zip-slip). Returns the folder that
     * should be treated as the project's source root.
     */
    public StoredProject store(Long userId, MultipartFile file) throws IOException {
        String original = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
        boolean isZip = original.toLowerCase().endsWith(".zip");
        boolean isJava = original.toLowerCase().endsWith(".java");

        if (!isZip && !isJava) {
            throw new ApiExceptions.UnsupportedUploadException("Only .java files or .zip project archives are supported");
        }

        String folderId = UUID.randomUUID().toString();
        Path destRoot = Paths.get(uploadDir, String.valueOf(userId), folderId).toAbsolutePath().normalize();
        Files.createDirectories(destRoot);

        if (isJava) {
            Path target = destRoot.resolve(sanitizeFileName(original));
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredProject(destRoot.toAbsolutePath().toString(), original, false);
        }

        // ZIP: extract with zip-slip protection
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path resolved = destRoot.resolve(entry.getName()).normalize();
                if (!resolved.startsWith(destRoot)) {
                    throw new IOException("Zip entry escapes target directory: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(resolved);
                } else {
                    Files.createDirectories(resolved.getParent());
                    Files.copy(zis, resolved, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
        return new StoredProject(destRoot.toAbsolutePath().toString(), original, true);
    }

    public Path reportPath(String fileName) throws IOException {
        Path dir = Paths.get(reportDir);
        Files.createDirectories(dir);
        return dir.resolve(sanitizeFileName(fileName));
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public record StoredProject(String rootPath, String originalName, boolean wasZip) {}
}
