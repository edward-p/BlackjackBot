package xyz.edward_p.blackjackbot.card;

/**
 * @author edward <br/>
 * Date: 4/22/21 10:19 AM <br/>
 * Description:
 */
public enum Suit {
    SPADE("♠️"),
    HEART("♥️"),
    DIAMOND("♦️"),
    CLUB("♣️");
    String value;

    Suit(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
