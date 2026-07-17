package za.co.sww.game.wumpus.commentary;

import java.util.Optional;

public class NoopCommentaryClient implements CommentaryClient {
    @Override
    public Optional<String> fetchCommentary(CommentarySnapshot snapshot) {
        return Optional.empty();
    }
}
