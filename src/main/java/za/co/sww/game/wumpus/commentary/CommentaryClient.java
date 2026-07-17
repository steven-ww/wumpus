package za.co.sww.game.wumpus.commentary;

import java.util.Optional;

public interface CommentaryClient {
    Optional<String> fetchCommentary(CommentarySnapshot snapshot);
}
