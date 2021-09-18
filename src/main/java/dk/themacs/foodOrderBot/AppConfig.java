package dk.themacs.foodOrderBot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {

    private final String botUserOAuthToken;
    private final String botAppToken;
    private final String channelId;

    public AppConfig(@Value("${bot.user.oauth.token}") String botUserOAuthToken,
                     @Value("${bot.app.token}") String botAppToken,
                     @Value("${channel.id}") String channelId) {
        this.botUserOAuthToken = botUserOAuthToken;
        this.botAppToken = botAppToken;
        this.channelId = channelId;
    }

    public String getBotUserOAuthToken() {
        return botUserOAuthToken;
    }

    public String getBotAppToken() {
        return botAppToken;
    }

    public String getChannelId() {
        return channelId;
    }
}
