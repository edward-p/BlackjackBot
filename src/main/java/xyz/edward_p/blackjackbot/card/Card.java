package xyz.edward_p.blackjackbot.card;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * @author edward <br/>
 * Date: 4/22/21 10:12 AM <br/>
 * Description:
 */
@Getter
@Setter
public class Card implements Cloneable {
    public final static String CARD_BACK = "|??| ";
    private final Suit suit;
    private final Deck deck;
    private final String text;
    private int value;

    public Card(Suit suit, Deck deck) {
        this.suit = suit;
        this.deck = deck;
        this.text = "|" + suit.getValue() + deck.getValue() + "| ";
        this.value = getValue(deck);
    }

    private int getValue(Deck deck) {
        switch (deck) {
            case ACE:
                return 11;
            case TWO:
                return 2;
            case THREE:
                return 3;
            case FOUR:
                return 4;
            case FIVE:
                return 5;
            case SIX:
                return 6;
            case SEVEN:
                return 7;
            case EIGHT:
                return 8;
            case NINE:
                return 9;
            case TEN:
            case JACK:
            case QUEUE:
            case KING:
                return 10;
        }
        return -1;
    }

    public static int sumOfCards(List<Card> cards) {
        int sum = cards.stream().mapToInt(Card::getValue).sum();
        boolean haveACE = true;
        // Try to make sum <= 21 by adjust the value of ACE
        while (sum > 21 && haveACE) {
            int i;
            for (i = 0; i < cards.size(); i++) {
                Card c = cards.get(i);
                if (c.getValue() == 11) {
                    try {
                        // clone instead of modify the original instance
                        Card clone = (Card) c.clone();
                        clone.setValue(1);
                        cards.set(i, clone);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            if (i >= cards.size()) {
                haveACE = false;
            }
            sum = cards.stream().mapToInt(Card::getValue).sum();
        }

        return sum;
    }

    @Override
    public String toString() {
        return text;
    }
}
