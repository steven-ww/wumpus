package za.co.sww.game.wumpus.actions;

import za.co.sww.game.wumpus.domain.Cave;
import za.co.sww.game.wumpus.domain.Player;
import za.co.sww.game.wumpus.io.Output;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;

class HazardCheckerTest {

    private HazardChecker hazardChecker;
    private TestOutput output;
    private TestRandom random;
    private Cave[] caves;
    private Player player;

    @BeforeEach
    void setUp() {
        output = new TestOutput();
        hazardChecker = new HazardChecker(output);
        random = new TestRandom();
        hazardChecker.random = random;
        player = new Player();
        
        // Setup 20 caves
        caves = new Cave[20];
        for (int i = 0; i < 20; i++) {
            caves[i] = new Cave(new int[]{(i + 1) % 20, (i + 2) % 20, (i + 19) % 20});
        }
    }

    @Test
    void testCheckForHazards_Bats() {
        // Arrange
        int startRoom = 0;
        int targetRoom = 5;
        player.setCurrentRoom(startRoom);
        caves[startRoom].setHasBats(true);
        random.setNextInt(targetRoom);

        // Act
        hazardChecker.checkForHazards(caves, player);

        // Assert
        assertEquals(targetRoom, player.getCurrentRoom());
        assertTrue(output.getMessages().contains(
                "Giant bats picked you up and dropped you in cave " + targetRoom + "!"));
    }

    @Test
    void testCheckForHazards_BottomlessPit() {
        // Arrange
        int startRoom = 0;
        player.setCurrentRoom(startRoom);
        caves[startRoom].setIsBottomLessPit(true);

        // Act
        hazardChecker.checkForHazards(caves, player);

        // Assert
        assertEquals(Player.PlayerState.DEAD, player.getState());
        assertTrue(output.getMessages().contains("You fell into a bottomless pit. You lose."));
    }

    @Test
    void testCheckForHazards_Wumpus_Eaten() {
        // Arrange
        int startRoom = 0;
        player.setCurrentRoom(startRoom);
        caves[startRoom].setHasWumpus(true);
        // Option 3 means Wumpus stays in the current cave
        random.setNextInt(3);

        // Act
        hazardChecker.checkForHazards(caves, player);

        // Assert
        assertEquals(Player.PlayerState.DEAD, player.getState());
        assertTrue(output.getMessages().contains("You bumped into the Wumpus!"));
        assertTrue(output.getMessages().contains("The Wumpus found you and ate you. You lose."));
    }

    @Test
    void testCheckForHazards_Wumpus_Moves() {
        // Arrange
        int startRoom = 0;
        player.setCurrentRoom(startRoom);
        caves[startRoom].setHasWumpus(true);
        // Option < 3 means Wumpus moves to linked cave at index. 
        // linkedCaves for cave 0 are {1, 2, 19}
        random.setNextInt(0); // index 0 -> cave 1

        // Act
        hazardChecker.checkForHazards(caves, player);

        // Assert
        assertEquals(Player.PlayerState.ALIVE, player.getState());
        assertFalse(caves[startRoom].hasWumpus());
        assertTrue(caves[1].hasWumpus());
        assertTrue(output.getMessages().contains("You bumped into the Wumpus!"));
        assertFalse(output.getMessages().contains("The Wumpus found you and ate you. You lose."));
    }

    @Test
    void testPrintHazards_Bats() {
        // Arrange
        int currentRoom = 0;
        // Cave 0 links to 1, 2, 19
        caves[1].setHasBats(true);

        // Act
        hazardChecker.printHazards(caves, currentRoom);

        // Assert
        assertTrue(output.getMessages().contains("You hear the flapping of giant bats nearby."));
    }

    @Test
    void testPrintHazards_Wumpus() {
        // Arrange
        int currentRoom = 0;
        caves[2].setHasWumpus(true);

        // Act
        hazardChecker.printHazards(caves, currentRoom);

        // Assert
        assertTrue(output.getMessages().contains("You smell something terrible nearby."));
    }

    @Test
    void testPrintHazards_Pit() {
        // Arrange
        int currentRoom = 0;
        caves[19].setIsBottomLessPit(true);

        // Act
        hazardChecker.printHazards(caves, currentRoom);

        // Assert
        assertTrue(output.getMessages().contains("You feel a cold draft from a nearby cave."));
    }

    @Test
    void testGetWumpusCave() {
        // Arrange
        int wumpusRoom = 7;
        caves[wumpusRoom].setHasWumpus(true);

        // Act
        Cave wumpusCave = hazardChecker.getWumpusCave(caves);

        // Assert
        assertSame(caves[wumpusRoom], wumpusCave);
        assertTrue(wumpusCave.hasWumpus());
    }

    @Test
    void testBumpTheWumpus_Stays() {
        // Arrange
        int wumpusRoom = 0;
        caves[wumpusRoom].setHasWumpus(true);
        // Option 3 means stay
        random.setNextInt(3);

        // Act
        hazardChecker.bumpTheWumpus(caves, caves[wumpusRoom], player);

        // Assert
        assertTrue(caves[wumpusRoom].hasWumpus());
    }

    @Test
    void testBumpTheWumpus_Moves() {
        // Arrange
        int wumpusRoom = 0;
        caves[wumpusRoom].setHasWumpus(true);
        // Cave 0 links to {1, 2, 19}
        // Option 1 means move to index 1 (cave 2)
        random.setNextInt(1);

        // Act
        hazardChecker.bumpTheWumpus(caves, caves[wumpusRoom], player);

        // Assert
        assertFalse(caves[wumpusRoom].hasWumpus());
        assertTrue(caves[2].hasWumpus());
    }

    private static class TestOutput implements Output {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void println(String message) {
            messages.add(message);
        }

        public List<String> getMessages() {
            return messages;
        }
    }

    private static class TestRandom implements RandomGenerator {
        private int nextInt;

        public void setNextInt(int nextInt) {
            this.nextInt = nextInt;
        }

        @Override
        public int nextInt() {
            return nextInt;
        }

        @Override
        public int nextInt(int bound) {
            return nextInt;
        }

        @Override
        public long nextLong() {
            return 0;
        }
    }
}
