package dk.themacs.foodOrderBot.jobs;

import dk.themacs.foodOrderBot.ClientHandler;

public class FoodOrderJob {

    private final ClientHandler clientHandler;

    public FoodOrderJob(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }
}
