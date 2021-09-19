package dk.themacs.foodOrderBot.jobs;

import dk.themacs.foodOrderBot.ClientHandler;

public class FoodOrderMessageJob {

    private final ClientHandler clientHandler;

    public FoodOrderMessageJob(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }
}
