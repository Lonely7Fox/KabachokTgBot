package io.project.KabachokTgBot.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {

    public static boolean deleteDirectoryIfExists(Path dir) throws IOException {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return false;
        }

        IOException[] exception = new IOException[]{null};
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    if (exception[0] == null) exception[0] = e;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                if (exc != null && exception[0] == null) {
                    exception[0] = exc;
                }

                try {
                    Files.delete(dir);
                } catch (IOException e) {
                    if (exception[0] == null) exception[0] = e;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        if (exception[0] != null) {
            throw exception[0];
        }
        return true;
    }

    public static void ensureDirectory(Path dir) {
        try {
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                Files.createDirectory(dir);
            }
        } catch (IOException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static void ensureDirectories(Path dir) {
        try {
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                ensureDirectories(dir.getParent());
                Files.createDirectory(dir);
            }
        } catch (IOException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveToFile(byte[] data, Path path) {
        if (data == null) throw new IllegalArgumentException();
        try (OutputStream outputStream = Files.newOutputStream(path,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            outputStream.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeToFile(byte[] data, Path path) {
        File cacheFile = new File(path.toAbsolutePath().toString());
        if (!cacheFile.exists()) {
            try {
                cacheFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(cacheFile)){
            fileOutputStream.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readBytesFromFile(Path path) {
        try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            inputStream.transferTo(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
