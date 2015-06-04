package com.plus.blakesroad.pluck;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 * Created by ben on 04/06/15.
 */
public class CopyWalker extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher;
    private final File sourceDirectory;
    private final File destinationDirectory;
    private final boolean force;
    private final boolean replace;
    private long bytes = 0;


    public CopyWalker(boolean force, File sourceDirectory, File destinationDirectory, String type, boolean replace) {
        matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + type);
        this.sourceDirectory = sourceDirectory;
        this.destinationDirectory = destinationDirectory;
        this.force = force;
        this.replace = replace;
    }

    void matchAndCopy(Path filePath) {
        Path name = filePath.getFileName();
        if (name != null && matcher.matches(name)) {

            if (!this.force) {
                System.out.println(filePath);
                try {
                    bytes += Files.size(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ArrayList<CopyOption> copyOptions = new ArrayList<CopyOption>();
            copyOptions.add(StandardCopyOption.COPY_ATTRIBUTES);
            final Path newPath = sourceDirectory.toPath().relativize(filePath);
            final Path destinationPath = destinationDirectory.toPath().resolve(newPath);
            if (this.replace) {
                copyOptions.add(StandardCopyOption.REPLACE_EXISTING);
            }

            if (this.force && (!Files.exists(destinationPath) || this.replace)) {
                try {
                    Files.createDirectories(destinationPath.getParent());
                    Files.copy(filePath, destinationPath, copyOptions.toArray(new CopyOption[copyOptions.size()]));
                    System.out.println("Copied " + destinationPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (this.force) {
                System.out.println("File exists " + destinationPath);
            }
        }
    }

    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attr) {
        if (attr.isRegularFile()) {
            matchAndCopy(file);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir,
                                              IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,
                                           IOException exc) {
        System.err.println(exc);
        return FileVisitResult.CONTINUE;
    }

    public long getBytes() {
        return bytes;
    }


}
