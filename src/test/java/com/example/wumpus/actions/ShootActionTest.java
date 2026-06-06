package com.example.wumpus.actions;

import com.example.wumpus.Cave;
import com.example.wumpus.Player;
import com.example.wumpus.io.Input;
import com.example.wumpus.io.Output;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ShootActionTest {

    private ShootAction shootAction;
    private TestInput input;
    private TestOutput output;
    private TestRandom random;
    private Cave[] caves;
    private Player player;

    @BeforeEach
    void setUp() {
        input = new TestInput();
        output = new TestOutput();
        random = new TestRandom();
        shootAction = new ShootAction(input, output);
        shootAction.randomGenerator = random;
        shootAction.hazardChecker.random = random;
        player = new Player();
        caves = new Cave[20];
        for (int i = 0; i < 20; i++) {
            caves[i] = new Cave(new int[]{(i + 1) % 20, (i + 2) % 20, (i + 19) % 20});
        }
        setWumpus(18);
    }

    private void setWumpus(int room) {
        for (Cave cave : caves) {
            cave.setHasWumpus(false);
        }
        caves[room].setHasWumpus(true);
    }

    @Test
    void testShootArrow_ValidInputs() {
        input.addInput("3"); // Number of caves
        input.addInput("1"); // Cave 0
        input.addInput("2"); // Cave 1
        input.addInput("3"); // Cave 2

        shootAction.shootArrow(caves, player);

        assertEquals(4, input.getPrompts().size());
        assertEquals("How many caves should the arrow fly through? (1-5)", input.getPrompts().get(0));
        assertEquals("Cave 0?", input.getPrompts().get(1));
        assertEquals("Cave 1?", input.getPrompts().get(2));
        assertEquals("Cave 2?", input.getPrompts().get(3));
    }

    @Test
    void testShootArrow_InvalidNumberOfCaves() {
        input.addInput("0");    // Invalid: too low
        input.addInput("6");    // Invalid: too high
        input.addInput("abc");  // Invalid: non-numeric
        input.addInput("1");    // Valid
        input.addInput("5");    // Cave 0

        shootAction.shootArrow(caves, player);

        assertTrue(output.getMessages().contains("Please enter a number between 1 and 5."));
        assertTrue(output.getMessages().contains("Please enter a valid number between 1 and 5."));
        assertEquals("Cave 0?", input.getPrompts().get(4));
    }

    @Test
    void testShootArrow_InvalidCaveNumbers() {
        input.addInput("1");    // Number of caves
        input.addInput("-1");   // Invalid: too low
        input.addInput("20");   // Invalid: too high
        input.addInput("xyz");  // Invalid: non-numeric
        input.addInput("10");   // Valid

        shootAction.shootArrow(caves, player);

        assertTrue(output.getMessages().contains("Please enter a valid cave number between 0 and 19."));
        assertEquals(5, input.getPrompts().size());
        assertEquals("10", input.getInputsHandled().get(4));
    }

    @Test
    void testShootArrow_CannotShootIntoPreviousRoom() {
        player.setCurrentRoom(0);
        input.addInput("3"); // Number of caves
        input.addInput("1"); // Cave 0? OK
        input.addInput("2"); // Cave 1? OK
        input.addInput("1"); // Cave 2? Invalid (just left room 1)
        input.addInput("3"); // Cave 2? OK

        shootAction.shootArrow(caves, player);

        assertTrue(output.getMessages().contains("Arrows aren't that crooked — pick another cave."));
        assertEquals(5, input.getPrompts().size());
        assertEquals("3", input.getInputsHandled().get(4));
    }

    @Test
    void testShootArrow_HitsWumpusOnPath() {
        player.setCurrentRoom(0);
        setWumpus(2); // Cave 0 links to 1, 2, 19
        input.addInput("1");
        input.addInput("2");

        shootAction.shootArrow(caves, player);

        assertEquals(Player.PlayerState.WINNER, player.getState());
        assertTrue(output.getMessages().stream()
                .anyMatch(m -> m.contains("killed the Wumpus! You win!")));
    }

    @Test
    void testShootArrow_HitsPlayerOnPath() {
        player.setCurrentRoom(0);
        // Valid loop: 0 -> 2 -> 1 -> 0
        // Links: 0: (1,2,19), 2: (3,4,1), 1: (2,3,0)
        input.addInput("3");
        input.addInput("2");
        input.addInput("1");
        input.addInput("0");

        shootAction.shootArrow(caves, player);

        assertEquals(Player.PlayerState.DEAD, player.getState());
        assertTrue(output.getMessages().contains("Your arrow flew into cave 0 and you shot yourself!"));
    }

    @Test
    void testShootArrow_MissesEverythingOnPath() {
        player.setCurrentRoom(0);
        // Path 0 -> 1 -> 2
        input.addInput("2");
        input.addInput("1");
        input.addInput("2");

        shootAction.shootArrow(caves, player);

        assertEquals(Player.PlayerState.ALIVE, player.getState());
        assertTrue(output.getMessages().contains("Your arrow missed and flew into cave 2."));
    }

    @Test
    void testShootArrow_OffCourse_HitsWumpus() {
        player.setCurrentRoom(0);
        // Wumpus in cave 3
        setWumpus(3);
        // Nomination: 1 (valid), 10 (invalid -> off course), 5 (random)
        input.addInput("3");
        input.addInput("1");
        input.addInput("10");
        input.addInput("5");

        // From cave 1, links are {2, 3, 0}
        // Index 1 corresponds to cave 3
        random.setNextInt(1);

        shootAction.shootArrow(caves, player);

        assertEquals(Player.PlayerState.WINNER, player.getState());
        assertTrue(output.getMessages().contains(
                "Your arrow couldn't find cave 10 from cave 1 and is flying off course!"));
        assertTrue(output.getMessages().stream()
                .anyMatch(m -> m.contains("flew into cave 3 and killed the Wumpus!")));
    }

    @Test
    void testShootArrow_OffCourse_HitsPlayer() {
        player.setCurrentRoom(0);
        // Nomination: 1 (valid), 10 (invalid -> off course), 5 (random)
        input.addInput("3");
        input.addInput("1");
        input.addInput("10");
        input.addInput("5");

        // From cave 1, links are {2, 3, 0}
        // Index 2 corresponds to cave 0
        random.setNextInt(2);

        shootAction.shootArrow(caves, player);

        assertEquals(Player.PlayerState.DEAD, player.getState());
        assertTrue(output.getMessages().contains(
                "Your arrow couldn't find cave 10 from cave 1 and is flying off course!"));
        assertTrue(output.getMessages().contains("Your arrow flew into cave 0 and you shot yourself!"));
    }

    @Test
    void testShootArrow_OffCourse_MissesEverything() {
        player.setCurrentRoom(0);
        // Nomination: 1 (valid), 10 (invalid -> off course), 5 (random)
        input.addInput("3");
        input.addInput("1");
        input.addInput("10");
        input.addInput("5");

        // From cave 1, links are {2, 3, 0}
        // Index 0 corresponds to cave 2
        random.setNextInt(0);

        shootAction.shootArrow(caves, player);

        assertEquals(Player.PlayerState.ALIVE, player.getState());
        assertTrue(output.getMessages().contains(
                "Your arrow couldn't find cave 10 from cave 1 and is flying off course!"));
        assertTrue(output.getMessages().contains("Your arrow missed and flew into cave 3."));
    }

    @Test
    void testShootArrow_OutOfArrows_Dead() {
        player.setCurrentRoom(0);
        player.setNumberOfArrows(1);

        // Shoot 1 cave, and miss
        input.addInput("1");
        input.addInput("1");

        shootAction.shootArrow(caves, player);

        assertEquals(0, player.getNumberOfArrows());
        assertEquals(Player.PlayerState.DEAD, player.getState());
        assertTrue(output.getMessages().contains("You have no more arrows left. You lose."));
    }

    @Test
    void testShootArrow_Miss_WumpusMoves() {
        player.setCurrentRoom(0);
        int wumpusStartCave = 5;
        setWumpus(wumpusStartCave);

        // Arrow misses
        input.addInput("1");
        input.addInput("1"); // Shoot into cave 1

        // Wumpus movement:
        // Option < 3 means move.
        // Cave 5 links are {(5+1)%20, (5+2)%20, (5+19)%20} = {6, 7, 4}
        // Option 0 means move to index 0 (cave 6)
        random.setNextInt(0);

        shootAction.shootArrow(caves, player);

        assertTrue(output.getMessages().contains("Your arrow missed and flew into cave 1."));
        assertFalse(caves[wumpusStartCave].hasWumpus());
        assertTrue(caves[6].hasWumpus());
    }

    @Test
    void testShootArrow_Miss_WumpusMovesToPlayerCave() {
        player.setCurrentRoom(0);
        // Cave 0 links to {1, 2, 19}
        // Place Wumpus in cave 1
        setWumpus(1);

        // Arrow misses
        input.addInput("1");
        input.addInput("2"); // Shoot into cave 2

        // Wumpus movement:
        // Cave 1 links to {2, 3, 0}
        // Option 2 means move to index 2 (cave 0, where player is)
        random.setNextInt(2);

        shootAction.shootArrow(caves, player);

        assertEquals(Player.PlayerState.DEAD, player.getState());
        assertTrue(output.getMessages().contains("The Wumpus found you and ate you. You lose."));
    }

    private static class TestInput implements Input {
        private final Queue<String> inputs = new LinkedList<>();
        private final List<String> prompts = new ArrayList<>();
        private final List<String> inputsHandled = new ArrayList<>();

        public void addInput(String input) {
            inputs.add(input);
        }

        @Override
        public String readln(String prompt) {
            prompts.add(prompt);
            String val = inputs.poll();
            inputsHandled.add(val);
            return val;
        }

        public List<String> getPrompts() {
            return prompts;
        }

        public List<String> getInputsHandled() {
            return inputsHandled;
        }
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
