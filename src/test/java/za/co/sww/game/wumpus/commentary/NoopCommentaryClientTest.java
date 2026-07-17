package za.co.sww.game.wumpus.commentary;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NoopCommentaryClientTest {
    @Test
    void shouldReturnEmptyCommentary() {
        NoopCommentaryClient client = new NoopCommentaryClient();
        CommentarySnapshot snapshot = new CommentarySnapshot(
                "MOVE",
                "MOVE_TO_ROOM",
                5,
                List.of(),
                5,
                "SAFE",
                5,
                List.of(1, 2, 3),
                List.of("You smell something terrible nearby."),
                4,
                2,
                List.of("MOVE -> SAFE @ 4"),
                null
        );

        assertTrue(client.fetchCommentary(snapshot).isEmpty());
    }
}
