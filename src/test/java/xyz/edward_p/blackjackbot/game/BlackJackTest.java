package xyz.edward_p.blackjackbot.game;

import org.junit.jupiter.api.Assertions;
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
        Assertions.assertEquals("[DRAW]", judge(p));

        dealerCards.clear();
        dealerCards.add(new Card(Suit.HEART, Deck.JACK));
        dealerCards.add(new Card(Suit.HEART, Deck.ACE));
        dealerCardSum = Card.sumOfCards(dealerCards);
        p.leave();
        p.draw(new Card(Suit.HEART, Deck.JACK));
        p.draw(new Card(Suit.HEART, Deck.ACE));
        Assertions.assertEquals("[DRAW]", judge(p));

        dealerCards.clear();
        dealerCards.add(new Card(Suit.HEART, Deck.EIGHT));
        dealerCards.add(new Card(Suit.HEART, Deck.JACK));
        dealerCards.add(new Card(Suit.HEART, Deck.THREE));
        dealerCardSum = Card.sumOfCards(dealerCards);
        p.leave();
        p.draw(new Card(Suit.HEART, Deck.ACE));
        p.draw(new Card(Suit.HEART, Deck.JACK));
        Assertions.assertEquals("[BLACKJACK]", judge(p));


        dealerCards.clear();
        dealerCards.add(new Card(Suit.HEART, Deck.FIVE));
        dealerCards.add(new Card(Suit.HEART, Deck.THREE));
        dealerCards.add(new Card(Suit.HEART, Deck.NINE));
        dealerCardSum = Card.sumOfCards(dealerCards);
        p.leave();
        p.draw(new Card(Suit.HEART, Deck.KING));
        p.draw(new Card(Suit.HEART, Deck.SEVEN));
        Assertions.assertEquals("[DRAW]", judge(p));

        // Dealer bust
        dealerCards.clear();
        dealerCards.add(new Card(Suit.HEART, Deck.TEN));
        dealerCards.add(new Card(Suit.HEART, Deck.THREE));
        dealerCards.add(new Card(Suit.HEART, Deck.QUEUE));
        dealerCardSum = Card.sumOfCards(dealerCards);
        p.leave();
        p.draw(new Card(Suit.HEART, Deck.KING));
        p.draw(new Card(Suit.HEART, Deck.SEVEN));
        Assertions.assertEquals("[WIN]", judge(p));
    }

    private String judge(UserData p) {
        String str;
        if (p.isLeftHandBust() || (p.getSumOfLeft() < dealerCardSum && dealerCardSum <= 21)) {
            str = "[LOSE]";
        } else if (!p.isLeftHandBust() && (dealerCardSum > 21 ||
                (p.getSumOfLeft() > dealerCardSum && p.getLeftHand().size() != 2))) {
            str = "[WIN]";
        } else if ((p.getLeftHand().size() == 2 && p.getSumOfLeft() == 21)
                && !(dealerCardSum == 21 && dealerCards.size() == 2)) {
            str = "[BLACKJACK]";
        } else {
            str = "[DRAW]";
        }
        return str;
    }
}