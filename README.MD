# Food Order Bot
<img src="./bot_logo.png" width="200" height="200">

## Setup
- The database in use is [PostgreSQL](https://www.postgresql.org/).
- Head over to [My Apps (Slack)](https://api.slack.com/apps), to create a new Application, and add it to the desired workspace.
  - Create the app level token (starting with `xapp`) under `Basic Information > App-Level Tokens` with the permissions: _connections:write_. 
  - Create the bot user token (starting with `xoxb`) under `OAuth & Permissions > OAuth Tokens for Your Workspace`. 
  - Set the Scopes under `OAuth & Permissions > Scopes` to: _channels:history_, _chat:write_, _commands_, _im:write_ and _reactions:write_.
  - (optional) Create the two commands `/ordre` and `/ping` under `OAuth & Permissions > Slash Commands`.
- Head over to [SendGrid](https://api.slack.com/apps) to generate your API key.
- Copy the contents of `src/main/java/resources/application.properties.template` into a new file `src/main/java/resources/application.properties` and replace the information with your secrets.

## Building the application
Build the application with the command:
```
./gradlew build
```
Find the _.jar_ file under `build/libs/foodOrderBot-x.x.x-SNAPSHOT.jar` and run it with the command:
```
java -jar foodOrderBot-x.x.x-SNAPSHOT.jar
```
