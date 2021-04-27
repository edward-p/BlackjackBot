package xyz.edward_p.blackjackbot.game;

import org.junit.jupiter.api.Test;
import xyz.edward_p.blackjackbot.card.Card;
import xyz.edward_p.blackjackbot.card.Deck;
import xyz.edward_p.blackjackbot.card.Suit;
import xyz.edward_p.blackjackbot.entity.UserData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author edward <br/>
 * Date: 4/27/21 10:41 PM <br/>
 * Description:
 */
class BlackJackTest {

    private int dealerCardSum;
    private List<Card> dealerCards;

    @Test
    public void judgeTest() {
        UserData p = new UserData(114514);
        p.draw(new Card(Suit.HEART, Deck.JACK));
        p.draw(new Card(Suit.HEART, Deck.JACK));
        p.draw(new Card(Suit.HEART, Deck.ACE));

        dealerCards = new ArrayList<>();
        dealerCards.add(new Card(Suit.HEART, Deck.JACK));
        dealerCards.add(new Card(Suit.HEART, Deck.JACK));
        dealerCards.add(new Card(Suit.HEART, Deck.ACE));
        dealerCardSum = Card.sumOfCards(dealerCards);

        assert judge(p).equals("[DRAW]");

        dealerCards.clear();
        dealerCards.add(new Card(Suit.HEART, Deck.JACK));
        dealerCards.add(new Card(Suit.HEART, Deck.ACE));
        p.leave();
        p.draw(new Card(Suit.HEART, Deck.JACK));
        p.draw(new Card(Suit.HEART, Deck.ACE));

        assert judge(p).equals("[DRAW]");
    }

    private String judge(UserData p) {
        String str;
        if (p.isLeftHandBust() || (p.getSumOfLeft() < dealerCardSum && dealerCardSum <= 21)) {
            str = "[LOSE]";
        } else if ((dealerCardSum == p.getSumOfLeft() && dealerCards.size() != 2) ||
                (dealerCardSum == 21 && dealerCards.size() == 2 && p.getLeftHand().size() == 2
                        && p.getSumOfLeft() == 21)) {
            str = "[DRAW]";
        } else {
            if (p.getLeftHand().size() == 2 && p.getSumOfLeft() == 21) {
                str = "[BLACKJACK]";
            } else {
                str = "[WIN]";
            }
        }
        return str;
    }
}