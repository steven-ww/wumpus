package com.example.wumpus.actions;

import com.example.wumpus.Cave;
import com.example.wumpus.Player;
import com.example.wumpus.io.Input;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerMovementTest {

    private PlayerMovement playerMovement;
    private Player player;
    private Cave[] caves;

    @BeforeEach
    public void setup() {
        setupCaves();
    }

    @Test
    void testMoveToLinkedCave() {
        playerMovement = new PlayerMovement(new Input() {
            @Override
            public String readln(String prompt) {
                return "1";
            }
        });

        player = new Player();
        player.setCurrentRoom(2);

        playerMovement.movePlayer(caves, player);
        assert player.getCurrentRoom() == 1;
    }

    @Test
    void testMoveToNotACave() {
        playerMovement = new PlayerMovement(new Input() {
            int callCount = 0;
            @Override
            public String readln(String prompt) {
                if (callCount == 0) {
                    callCount++;
                    return "NotACave";
                }
                return "exit";
            }
        });

        player = new Player();
        player.setCurrentRoom(2);

        playerMovement.movePlayer(caves, player);
        assert player.getCurrentRoom() == 2;

    }

    @Test
    void testMoveToInvalidCave() {
        playerMovement = new PlayerMovement(new Input() {
            int callCount = 0;
            @Override
            public String readln(String prompt) {
                if (callCount == 0) {
                    callCount++;
                    return "10";
                }
                return "exit";
            }
        });

        player = new Player();
        player.setCurrentRoom(2);

        playerMovement.movePlayer(caves, player);
        assert player.getCurrentRoom() == 2;

    }

    private void setupCaves() {
        caves = new Cave[3];
        caves[0] = new Cave(new int[]{2, 7, 8});
        caves[1] = new Cave(new int[]{2, 5, 6});
        caves[2] = new Cave(new int[]{1, 3, 4});
    }
}
