package com.plus.blakesroad.pluck;

import com.google.common.base.Optional;
import de.dr1fter.cliparsec.CliParser;
import de.dr1fter.cliparsec.Converters;
import de.dr1fter.cliparsec.ParsingResult;
import de.dr1fter.cliparsec.annotations.Command;
import de.dr1fter.cliparsec.annotations.HelpOption;
import de.dr1fter.cliparsec.annotations.Option;

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
 * Created by pi on 06/04/15.
 */
public class Pluck {

    @Command(name = "copy")
    private CopyArgs copyArgs;

    @HelpOption()
    private Object helpOption;

    static public void main(String[] args) {
        Pluck pluck = new Pluck();
        try {
            ParsingResult parsingResult = CliParser.createCliParser().parse(pluck, args);
            if ("copy".equals(((Optional<ParsingResult.SelectedCommand>) parsingResult.selectedCommand()).get().commandName())) {
                pluck.showArgs(args);
                pluck.walkFileTree();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public static class Walker extends SimpleFileVisitor<Path> {

            private final PathMatcher matcher;
            private final File sourceDirectory;
            private final File destinationDirectory;
            private final boolean force;
            private final boolean replace;
            private long bytes = 0;


            public Walker(CopyArgs copyArgs) {
            matcher = FileSystems.getDefault()
                    .getPathMatcher("glob:" + copyArgs.type);
            this.sourceDirectory = copyArgs.sourceDirectory;
            this.destinationDirectory = copyArgs.destinationDirectory;
            this.force = copyArgs.force;
            this.replace = copyArgs.replace;
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

        private void walkFileTree() {
            Walker walker = new Walker(copyArgs);
            try {
                Files.walkFileTree(copyArgs.sourceDirectory.toPath(), walker);
                System.out.println("Total megabytes: " + (walker.getBytes() != 0 ? (walker.getBytes() / 1024) /1000 : 0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void showArgs(String[] args) throws Exception {
            System.out.println("force: " + copyArgs.force);
            System.out.println("replace: " + copyArgs.replace);
            System.out.println("source directory: " + copyArgs.sourceDirectory);
            System.out.println("destination directory: " + copyArgs.destinationDirectory);
            System.out.println("match glob: " + copyArgs.type);
        }

        static class CopyArgs {
            @Option(shortOption = 'f', description="force copy")
            boolean force; //options default to their field names
            @Option(shortOption = 's', converter = Converters.DirectoryThatExists.class, description="source directory")
            File sourceDirectory;
            @Option(shortOption = 'd', converter = Converters.DirectoryThatExists.class, description="destination directory")
            File destinationDirectory;

            @Option(shortOption = 't', description="match file glob")
            String type;
            @Option(shortOption ='r', description = "replace existing files")
            boolean replace;
        }

    }
