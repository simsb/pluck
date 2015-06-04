package com.plus.blakesroad.pluck;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by ben on 04/06/15.
 */
@Parameters(commandDescription = "Copy files matching glob pattern")
class CopyCommand implements PluckCommand {

    @Parameter(names = {"-f", "-force"}, description = "Carry out the action")
    boolean force; //options default to their field names
    @Parameter(names = {"-s", "-source"}, required = true, description = "Source directory")
    File sourceDirectory;
    @Parameter(names = {"-d", "-destination"}, required = true, description = "Destination directory")
    File destinationDirectory;

    @Parameter(names = {"-t", "-type"}, required = true, description = "File match glob")
    String type;
    @Parameter(names = {"-r", "-replace"}, description = "Replace files which exist in destination directory")
    boolean replace;

    public void apply() {
        CopyWalker walker = new CopyWalker(force, sourceDirectory, destinationDirectory, type, replace);
        try {
            Files.walkFileTree(sourceDirectory.toPath(), walker);
            System.out.println("Total megabytes: " + walker.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
