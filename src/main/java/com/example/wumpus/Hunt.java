package com.example.wumpus;

import com.example.wumpus.actions.HazardChecker;
import com.example.wumpus.actions.PlayerMovement;
import com.example.wumpus.actions.ShootAction;
import com.example.wumpus.io.ConsoleInput;
import com.example.wumpus.io.ConsoleOutput;

import java.util.Scanner;

public class Hunt {
    Cave[] caves = new Cave[20];
    final CavesService cavesService = new CavesService();
    final Player player = new Player();

    private final ConsoleInput input = new ConsoleInput();
    private final ConsoleOutput output = new ConsoleOutput();

    PlayerMovement playerMovement = new PlayerMovement(input);
    HazardChecker hazardChecker = new HazardChecker(output);
    ShootAction shootAction = new ShootAction(input, output);

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

        output.println("Press Enter to continue...");
        scanner.nextLine();
        startGameLoop();
    }

    public void startGameLoop() {
        while (player.state == Player.PlayerState.ALIVE) {
            printPlayerCave(player);
            hazardChecker.printHazards(caves, player.currentRoom);
            switch (input.readln("Shoot or move? (S/M) ").toUpperCase()) {
                case "S":
                    runShoot();
                    break;
                case "M":
                    runMove();
                    break;
                default: output.println("That's not a valid action. Please enter S to shoot or M to move.");
            }
        }
        switch (player.state) {
            case WINNER:
                output.println("Congratulations! You have slain the Wumpus and won the game!");
                break;
            case DEAD:
                output.println("Game over. Better luck next time!");
                break;
        }
    }

    private void runMove() {
        playerMovement.movePlayer(caves, player);
        hazardChecker.checkForHazards(caves, player);
    }


    private void runShoot() {
        shootAction.shootArrow(caves, player);
    }

    private void printPlayerCave(Player player) {
        output.println("You are in cave " + player.currentRoom + ". Tunnels lead to caves " +
                caves[player.currentRoom].linkedCaves[0] + ", " +
                caves[player.currentRoom].linkedCaves[1] + " and " +
                caves[player.currentRoom].linkedCaves[2]);
    }
    

}
