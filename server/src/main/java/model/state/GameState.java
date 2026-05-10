package model.state;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public record GameState() {

    public Set<Move> getLegalMoves() {
        Set<Move> res = new HashSet<>();

        return res;
    }

    public boolean isGameOver() {
        return false;
    }
}
