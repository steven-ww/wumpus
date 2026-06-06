package com.example.wumpus.actions;

import com.example.wumpus.Cave;
import com.example.wumpus.Player;
import com.example.wumpus.io.ConsoleInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMovementTest {

    private PlayerMovement playerMovement;
    private Player player;
    private Cave[] caves;

//    @BeforeEach
//    void setUp() {
//        playerMovement = new PlayerMovement(new ConsoleInput());
//        player = new Player();
//        caves = new Cave[2];
//        caves[0] = new Cave(new int[]{1});
//        caves[1] = new Cave(new int[]{0});
//        player.setCurrentRoom(0);
//    }
//
//    @Test
//    void testMoveToLinkedCave() {
//        playerMovement.movePlayer(caves, player);
//    }
}
