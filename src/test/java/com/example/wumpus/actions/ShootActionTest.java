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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShootActionTest {

    private ShootAction shootAction;
    private TestInput input;
    private TestOutput output;
    private Cave[] caves;
    private Player player;

    @BeforeEach
    void setUp() {
        input = new TestInput();
        output = new TestOutput();
        shootAction = new ShootAction(input, output);
        player = new Player();
        caves = new Cave[20];
        for (int i = 0; i < 20; i++) {
            caves[i] = new Cave(new int[]{(i + 1) % 20, (i + 2) % 20, (i + 19) % 20});
        }
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
}
