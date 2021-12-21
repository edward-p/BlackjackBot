package xyz.edward_p.blackjackbot.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.edward_p.blackjackbot.card.Card;
import xyz.edward_p.blackjackbot.card.Deck;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Objects;


/**
 * @author edward <br/>
 * Date: 4/22/21 4:47 PM <br/>
 * Description:
 */
@Getter
@ToString
public class UserDataV2 implements Serializable {
    private static final long serialVersionUID = 1L;
    private final long id;
    private volatile long balance;
    private volatile String username;
    private transient volatile boolean inGame;
    private transient volatile long chatId;
    private transient volatile long cdUntil;
    private transient volatile long bets;
    /**
     * 1 : quarter in
     * 2 : half in
     * 3 : all in
     * 0 : none of the above
     */
    private transient volatile int betQHA;
    private transient volatile long initBets;

    private transient volatile LinkedList<Card> leftHand;
    private transient volatile int sumOfLeft;
    private transient volatile boolean leftHandDouble;
    private transient volatile LinkedList<Card> rightHand;
    private transient volatile int sumOfRight;
    private transient volatile boolean rightHandDouble;
    private transient volatile LinkedList<Card> currentHand;

    public UserDataV2(long id) {
        this.id = id;
        this.cdUntil = System.currentTimeMillis();
    }

    public synchronized void setUsername(String username) {
        this.username = username;
    }

    public synchronized void addBalance(long delta) {
        this.balance += delta;
        if (this.balance < 0) {
            this.balance = Long.MAX_VALUE;
        }
    }

    public synchronized void setBalance(long balance) {
        this.balance = balance;
    }

    public synchronized long bet() {
        if (balance == 0) {
            return -1L;
        }
        switch (betQHA) {
            case 0:
                bets = balance / 4;
                balance -= bets;
                betQHA = 1;
                break;
            case 1:
                balance += bets;
                bets = balance / 2;
                balance -= bets;
                betQHA = 2;
                break;
            case 2:
                balance += bets;
                bets = balance;
                balance -= bets;
                betQHA = 3;
                break;
        }
        this.initBets = this.bets;
        return this.bets;
    }

    public synchronized long bet(long bets) {
        if (this.balance < bets) {
            return -1L;
        }
        this.balance -= bets;
        this.bets += bets;
        this.initBets = this.bets;

        return this.bets;
    }

    public synchronized void doubleDown() {
        this.balance -= initBets;
        this.bets += initBets;

        if (currentHand == leftHand) {
            leftHandDouble = true;
        } else if (currentHand == rightHand) {
            rightHandDouble = true;
        }
    }

    public synchronized void leave() {
        if (this.bets != 0) {
            this.balance += this.bets;
            this.bets = 0;
        }
        this.inGame = false;
        this.bets = 0;
        this.betQHA = 0;
        this.initBets = 0;
        this.leftHand = null;
        this.sumOfLeft = 0;
        this.leftHandDouble = false;
        this.rightHand = null;
        this.sumOfRight = 0;
        this.rightHandDouble = false;
        this.currentHand = null;
    }

    public synchronized void draw(Card card) {
        if (this.leftHand == null) {
            this.leftHand = new LinkedList<>();
            this.currentHand = leftHand;
        }
        if (currentHand == leftHand) {
            this.leftHand.add(card);
            sumOfLeft = Card.sumOfCards(leftHand);
            if (sumOfLeft >= 21) {
                currentHand = rightHand;
                if (this.sumOfRight >= 21) {
                    // Right hand black jack.
                    this.currentHand = null;
                }
            }
        } else if (currentHand == rightHand) {
            this.rightHand.add(card);
            sumOfRight = Card.sumOfCards(rightHand);
            if (this.sumOfRight >= 21) {
                // Right reach 21
                this.currentHand = null;
            }
        }
    }

    public synchronized void stand() {
        if (currentHand == leftHand) {
            if (this.sumOfRight >= 21) {
                // Right hand black jack.
                this.currentHand = null;
            } else {
                // switch to right hand
                this.currentHand = rightHand;
            }
        } else {
            // Current hand is right hand, and player stand
            this.currentHand = null;
        }
    }

    public synchronized void split(Card cardLeft, Card cardRight) {
        this.balance -= initBets;
        this.bets += initBets;

        this.rightHand = new LinkedList<>();
        this.rightHand.add(this.leftHand.removeLast());
        this.leftHand.add(cardLeft);
        this.rightHand.add(cardRight);
        // recovery value of ACEs
        this.leftHand.forEach(c -> {
            if (c.getDeck() == Deck.ACE) {
                c.setValue(11);
            }
        });
        this.rightHand.forEach(c -> {
            if (c.getDeck() == Deck.ACE) {
                c.setValue(11);
            }
        });

        this.sumOfLeft = Card.sumOfCards(leftHand);
        this.sumOfRight = Card.sumOfCards(rightHand);

        if (this.sumOfLeft >= 21) {
            this.currentHand = this.rightHand;
            if (this.sumOfRight >= 21) {
                // Double black jack after split
                this.currentHand = null;
            }
        }
    }

    public synchronized void clearBets() {
        this.bets = 0;
    }

    public boolean isLeftHandBust() {
        return sumOfLeft > 21;
    }

    public boolean isRightHandBust() {
        return sumOfRight > 21;
    }

    public synchronized void addCd(long duration) {
        this.cdUntil = System.currentTimeMillis() + duration;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDataV2 userData = (UserDataV2) o;
        return id == userData.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public synchronized void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public synchronized long transfer(UserDataV2 toUser, long amount) {
        if (this.balance < amount) {
            return -1L;
        }
        this.balance -= amount;
        toUser.addBalance(amount);
        return amount;
    }
}
