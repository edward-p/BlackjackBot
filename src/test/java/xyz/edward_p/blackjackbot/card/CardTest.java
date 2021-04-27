package xyz.edward_p.blackjackbot.card;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author edward <br/>
 * Date: 4/22/21 10:41 AM <br/>
 * Description:
 */
class CardTest {
    @Test
    public void printTest() {
        System.out.println(Card.CARD_BACK);
        Arrays.stream(Suit.values()).forEach(x -> {
            Arrays.stream(Deck.values()).forEach(y -> {
                Card card = new Card(x, y);
                System.out.print(card);
            });
            System.out.println();
        });
    }

    @Test
    public void cloneTest() throws CloneNotSupportedException {
        Card card = new Card(Suit.SPADE, Deck.ACE);

        Card clone = card.clone();
        clone.setValue(1);

        assert card.getValue() == 11;
        assert clone.getValue() == 1;
    }


    @Test
    public void sumCardsTest() {
        List<Card> cardList = new ArrayList<>();
        Card ace = new Card(Suit.SPADE, Deck.ACE);
        cardList.add(new Card(Suit.CLUB, Deck.TEN));
        cardList.add(new Card(Suit.HEART, Deck.KING));
        cardList.add(ace);
        cardList.add(ace);
        assert Card.sumOfCards(cardList) == 22;
        assert ace.getValue() == 11;
    }

}