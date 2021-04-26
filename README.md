# BlackjackBot

A Telegram Bot allows you to play blackjack.

### How to build

```
git clone https://github.com/edward-p/BlackjackBot.git
cd BlackjackBot
mvn package
```

### How to use

```
java -jar blackjack-bot-1.0-SNAPSHOT.jar <TOKEN> <DATA_FILE_PATH>
```

e.g.

```
java -jar blackjack-bot-1.0-SNAPSHOT.jar "00000000:AAHkGMEC8MbCdvr6IjJb4kEkP76bU8NOFQ8" $HOME/.cache/blackjack.data
```

### Bot Command Sheet

```
blackjack - Playing Black Jack.
transfer - Transfer chips to another player.
rank - Rank players.
```

### Bot Settings

- Inline Mode: on
- Inline Feedback: 100%


