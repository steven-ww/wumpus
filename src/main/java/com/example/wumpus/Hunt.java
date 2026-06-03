package com.example.wumpus;

import java.util.Scanner;

public class Hunt {
    Cave[] caves = new Cave[20];
    CavesService cavesService = new CavesService();
    Player player = new Player();

    public void runGame() {
        caves = cavesService.buildCaves();
        player.setCurrentRoom(cavesService.initializeCaves(caves));

        IO.println("Welcome to Hunt the Wumpus");
        IO.println();
        IO.println("You are hunting the Wumpus through a system of 20 caves, each linked to 3 others.");
        IO.println("The Wumpus is asleep somewhere in these caves. Find it and shoot it before it eats");
        IO.println("you or you blunder into one of the other hazards.");
        IO.println();
        IO.println("Your senses warn you about the 3 caves next to you, but not which one holds what:");
        IO.println("  - You smell something terrible nearby when the Wumpus is in an adjacent cave.");
        IO.println("  - You feel a cold draft from a nearby cave when a bottomless pit is adjacent.");
        IO.println("  - You hear the flapping of giant bats nearby when a bat cave is adjacent.");
        IO.println();
        IO.println("The hazards:");
        IO.println("  - 2 bottomless pits. Stumble into one and you fall to your death.");
        IO.println("  - 2 caves of super bats. They grab you and whisk you away to a random cave.");
        IO.println();
        IO.println("You carry 5 crooked arrows. When you shoot, you steer an arrow through up to 5 caves,");
        IO.println("naming the caves it should fly through. If it reaches the Wumpus you win; if you miss,");
        IO.println("the startled Wumpus may move, and it might end up in your cave and eat you.");
        IO.println("Good luck adventurer!");
        IO.println();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Press Enter to continue...");
        scanner.nextLine();
        startGameLoop();
    }

    public void startGameLoop() {
        while (player.state == PlayerState.ALIVE) {
            IO.println("You are in cave " + player.currentRoom + ". Tunnels lead to caves " +
                    caves[player.currentRoom].linkedCaves[0] + ", " +
                    caves[player.currentRoom].linkedCaves[1] + " and " +
                    caves[player.currentRoom].linkedCaves[2]);
        }
    }
}
