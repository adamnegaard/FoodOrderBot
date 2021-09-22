package dk.themacs.foodOrderBot.commands;

public class UnknownCommandException extends Exception {

    private final boolean inform;

    public UnknownCommandException(String s) {
        super(s);
        this.inform = true;
    }

    public UnknownCommandException(String s, boolean inform) {
        super(s);
        this.inform = inform;
    }

    public boolean isInform() {
        return inform;
    }
}
