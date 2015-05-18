package com.plus.blakesroad.pluck;

import de.dr1fter.cliparsec.CliParser;
import de.dr1fter.cliparsec.Converters;
import de.dr1fter.cliparsec.annotations.Command;
import de.dr1fter.cliparsec.annotations.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by pi on 06/04/15.
 */
public class Pluck {

    @Command(name = "copy")
    CopyArgs copyArgs;

    static public void main(String[] args) {
        Pluck pluck = new Pluck();
        try {
            CliParser.createCliParser().parse(pluck, args);
            pluck.showArgs(args);
            pluck.walkFileTree();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Walker extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;
        private final File sourceDirectory;
        private final File destinationDirectory;
        private final boolean force;


        public Walker( CopyArgs copyArgs) {
            matcher = FileSystems.getDefault()
                    .getPathMatcher("glob:" + copyArgs.type);
            this.sourceDirectory = copyArgs.sourceDirectory;
            this.destinationDirectory = copyArgs.destinationDirectory;
            this.force = copyArgs.force;
        }

        void matchAndCopy(Path filePath) {
            Path name = filePath.getFileName();
            if (name != null && matcher.matches(name)) {

                if (!this.force) {
                    System.out.println(filePath);
                }
                CopyOption[] copyOptions = new CopyOption[] {StandardCopyOption.COPY_ATTRIBUTES};
                final Path newPath = sourceDirectory.toPath().relativize(filePath);
                final Path destinationPath = destinationDirectory.toPath().resolve(newPath);

                if (this.force) {
                    try {
                        Files.createDirectories(destinationPath.getParent());
                        Files.copy(filePath, destinationPath, copyOptions);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
    }

        private void walkFileTree() {
            Walker walker = new Walker(copyArgs);
            try {
                Files.walkFileTree(copyArgs.sourceDirectory.toPath(), walker);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void showArgs(String[] args) throws Exception {
            System.out.println("force: " + copyArgs.force);
            System.out.println("source directory: " + copyArgs.sourceDirectory);
            System.out.println("destination directory: " + copyArgs.destinationDirectory);
            System.out.println("match glob: " + copyArgs.type);
        }

        static class CopyArgs {
            @Option(shortOption = 'f')
            boolean force; //options default to their field names
            @Option(shortOption = 's', converter = Converters.DirectoryThatExists.class)
            File sourceDirectory;
            @Option(shortOption = 'd', converter = Converters.DirectoryThatExists.class)
            File destinationDirectory;

            @Option
            String type;
        }

    }
