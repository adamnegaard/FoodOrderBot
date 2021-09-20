package dk.themacs.foodOrderBot.commands;

public class UnknownCommandException extends Exception {

    public UnknownCommandException(String s) {
        super(s);
    }
}
