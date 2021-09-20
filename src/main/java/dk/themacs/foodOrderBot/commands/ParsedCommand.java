package dk.themacs.foodOrderBot.commands;

public class ParsedCommand {
    private String command;
    private String arguments;

    public ParsedCommand(String command, String arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
}
