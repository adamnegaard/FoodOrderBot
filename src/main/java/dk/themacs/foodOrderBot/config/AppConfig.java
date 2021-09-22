package dk.themacs.foodOrderBot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AppConfig {

    private final String botUserOAuthToken;
    private final String botAppToken;
    private final String channelId;

    public final static int reminderMinute = 0;
    public final static int reminderHour = 8;

    public final static int orderMinute = 30;
    public final static int orderHour = 9;

    public final static int closingMinute = 30;
    public final static int closingHour = 10;

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
