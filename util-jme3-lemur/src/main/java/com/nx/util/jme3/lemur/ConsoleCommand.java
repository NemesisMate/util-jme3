package com.nx.util.jme3.lemur;

/**
 * Created by NemesisMate on 4/04/16.
 */
public class ConsoleCommand {

    private String command;

    private String[] args;


    public ConsoleCommand(String command, String[] args) {
        this.command = command;
        this.args = args;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }
}
