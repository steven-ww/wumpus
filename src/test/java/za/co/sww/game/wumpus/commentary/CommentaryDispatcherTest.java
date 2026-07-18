package za.co.sww.game.wumpus.commentary;

import org.junit.jupiter.api.Test;
import za.co.sww.game.wumpus.io.Output;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentaryDispatcherTest {
    @Test
    void shouldPrintNarratorCommentaryWhenAvailable() {
        CapturingOutput output = new CapturingOutput();
        CommentaryDispatcher dispatcher =
                new CommentaryDispatcher(snapshot -> Optional.of("Solid plan. Terrible execution."), output, 200);

        dispatcher.dispatch(sampleSnapshot());

        assertEquals("Narrator is thinking...", output.messages().get(0));
        assertEquals("", output.messages().get(1));
        assertEquals("Narrator: Solid plan. Terrible execution.", output.messages().get(2));
        assertEquals("-------------------------------", output.messages().get(3));
    }

    @Test
    void shouldHandleClientFailureSilently() {
        CapturingOutput output = new CapturingOutput();
        CommentaryDispatcher dispatcher = new CommentaryDispatcher(snapshot -> {
            throw new RuntimeException("network down");
        }, output, 200);

        dispatcher.dispatch(sampleSnapshot());

        assertEquals(3, output.messages().size());
        assertTrue(output.messages().contains("Narrator is thinking..."));
        assertTrue(output.messages().contains("-------------------------------"));
    }

    private CommentarySnapshot sampleSnapshot() {
        return new CommentarySnapshot(
                "SHOOT",
                "SHOOT_THROUGH_CAVES",
                9,
                List.of(7, 9),
                7,
                "SHOT_MISSED",
                4,
                List.of(1, 2, 3),
                List.of("You feel a cold draft from a nearby cave."),
                3,
                5,
                List.of("MOVE -> SAFE @ 5"),
                null
        );
    }

    private static class CapturingOutput implements Output {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void println(String message) {
            messages.add(message);
        }

        public List<String> messages() {
            return messages;
        }
    }
}
