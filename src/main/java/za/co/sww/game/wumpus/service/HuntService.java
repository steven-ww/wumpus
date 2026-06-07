package za.co.sww.game.wumpus.service;

import za.co.sww.game.wumpus.domain.Cave;
import za.co.sww.game.wumpus.domain.Player;
import za.co.sww.game.wumpus.actions.HazardChecker;
import za.co.sww.game.wumpus.actions.PlayerMovement;
import za.co.sww.game.wumpus.actions.ShootAction;
import za.co.sww.game.wumpus.io.ConsoleInput;
import za.co.sww.game.wumpus.io.ConsoleOutput;
import org.jspecify.annotations.NonNull;

import java.util.Scanner;

public class HuntService {
    Cave[] caves = new Cave[20];
    final CavesService cavesService = new CavesService();

    private final ConsoleInput input = new ConsoleInput();
    private final ConsoleOutput output = new ConsoleOutput();

    PlayerMovement playerMovement = new PlayerMovement(input);
    HazardChecker hazardChecker = new HazardChecker(output);
    ShootAction shootAction = new ShootAction(input, output);

    public void runGame() {
        boolean gameRunning = true;
        do {

            Player player = new Player();
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

            startGameLoop(player);
            gameRunning = input.readln("Play again? (Y/N) ").toUpperCase().equals("Y");

        }
        while (gameRunning);
    }

    public void startGameLoop(Player player) {
        while (player.getState() == Player.PlayerState.ALIVE) {
            printPlayerCave(player);
            hazardChecker.printHazards(caves, player.getCurrentRoom());
            switch (input.readln("Shoot or move? (S/M) ").toUpperCase()) {
                case "S":
                    runShoot(player);
                    break;
                case "M":
                    runMove(player);
                    break;
                default: output.println("That's not a valid action. Please enter S to shoot or M to move.");
            }
        }
        switch (player.getState()) {
            case WINNER:
                output.println("Congratulations! You have slain the Wumpus and won the game!");
                break;
            case DEAD:
                output.println("Game over. Better luck next time!");
                break;
        }
    }

    private void runMove(Player player) {
        playerMovement.movePlayer(caves, player);
        hazardChecker.checkForHazards(caves, player);
    }


    private void runShoot(Player player) {
        shootAction.shootArrow(caves, player);
    }

    private void printPlayerCave(@NonNull Player player) {
        output.println("You are in cave " + player.getCurrentRoom() + ". Tunnels lead to caves " +
                caves[player.getCurrentRoom()].getLinkedCaves()[0] + ", " +
                caves[player.getCurrentRoom()].getLinkedCaves()[1] + " and " +
                caves[player.getCurrentRoom()].getLinkedCaves()[2]);
    }

}
