package com.plus.blakesroad.pluck;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * Created by pi on 06/04/15.
 */
public class Pluck {

    @Parameter(names = {"--help", "-h"}, help = true)
    private boolean helpOption;

    private CopyCommand copyArgs;

    public Pluck() {
        this.copyArgs = new CopyCommand();
    }

    static public void main(String[] args) {
        Pluck pluck = new Pluck();

        JCommander jCommander = new JCommander(pluck);
        jCommander.addCommand("copy", pluck.copyArgs);
        jCommander.parse(args);

        if (pluck.helpOption) {
            jCommander.usage();
            System.exit(0);
        }

        if ("copy".equals(jCommander.getParsedCommand())) {
            pluck.copyArgs.apply();
        }
    }
}
