package dk.themacs.foodOrderBot.commands;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CommandParser {

    private final static Pattern pattern = Pattern.compile("^\\?([A-Za-z0-9]+) (.*)$", Pattern.CASE_INSENSITIVE);

    public ParsedCommand getParsedCommand(String inputText) throws UnknownCommandException {
        Matcher matcher = pattern.matcher(inputText);
        if(!matcher.matches()) {
            throw new UnknownCommandException("Mangler en kommando i: '" + inputText + "'", false);
        }

        String command = matcher.group(1);
        String arguments = matcher.group(2);

        return new ParsedCommand(command, arguments);
    }

}
