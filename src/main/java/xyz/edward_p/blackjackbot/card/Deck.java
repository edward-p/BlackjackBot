package xyz.edward_p.blackjackbot.card;

/**
 * @author edward <br/>
 * Date: 4/22/21 10:20 AM <br/>
 * Description:
 */
public enum Deck {
    ACE("A"), TWO("2"), THREE("3"),
    FOUR("4"), FIVE("5"), SIX("6"),
    SEVEN("7"), EIGHT("8"), NINE("9"),
    TEN("10"), JACK("J"), QUEUE("Q"),
    KING("K");

    String value;

    Deck(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
