package com.plus.blakesroad.pluck;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.dr1fter.cliparsec.annotations.Command;
import de.dr1fter.cliparsec.annotations.HelpOption;

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

    @HelpOption
    private boolean helpOption;
    @Command(name = "copy")
    private CopyArgs copyArgs;

    public Pluck() {
        this.copyArgs = new CopyArgs();
    }



    static public void main(String[] args) {
        Pluck pluck = new Pluck();
        try {

            JCommander jCommander = new JCommander();
            jCommander.addCommand("copy", pluck.copyArgs);
            jCommander.parse(args);

            if (pluck.copyArgs.help) {
                jCommander.usage();
                System.exit(0);
            }


            if ("copy".equals(jCommander.getParsedCommand())) {
                pluck.showArgs();
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
                System.out.println("Total megabytes: " + walker.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void showArgs() throws Exception {
            System.out.println("force: " + copyArgs.force);
            System.out.println("replace: " + copyArgs.replace);
            System.out.println("source directory: " + copyArgs.sourceDirectory);
            System.out.println("destination directory: " + copyArgs.destinationDirectory);
            System.out.println("match glob: " + copyArgs.type);
        }

    @Parameters(commandDescription = "Copy files matching glob pattern")
    static class CopyArgs {
        @Parameter(names = {"--help", "-h"}, help = true)
        boolean help;


        @Parameter(names = { "-f", "-force" }, description = "Carry out the action")
            boolean force; //options default to their field names
            @Parameter(names = { "-s", "-source" }, required = true, description = "Source directory")
            File sourceDirectory;
            @Parameter(names = { "-d", "-destination" }, required = true, description = "Destination directory")
            File destinationDirectory;

            @Parameter(names = { "-t", "-type" }, required = true, description = "File match glob")
            String type;
            @Parameter(names = { "-r", "-replace" }, description = "Replace files which exist in destination directory")
            boolean replace;
        }

    }
