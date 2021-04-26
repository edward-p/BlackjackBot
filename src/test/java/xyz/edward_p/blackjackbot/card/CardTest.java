package xyz.edward_p.blackjackbot.card;

import org.junit.jupiter.api.Test;

import java.util.Arrays;


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

}