package model.player;

import model.board.Colour;
import model.state.GameState;
import model.state.Move;
import org.springframework.stereotype.Service;
import java.util.Set;


@Service
public final class Bot extends Player {

    public Bot(String name, Colour colour) {
        super(name, colour);
    }

    public Move turn(GameState state) {

        //do a random move for now
        Set<Move> validMoves = state.getLegalMoves();
        return validMoves.stream().findAny().orElseThrow();
    }
}
