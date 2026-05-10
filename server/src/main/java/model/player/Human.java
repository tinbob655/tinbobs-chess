package model.player;

import model.board.Colour;
import model.state.GameState;
import model.state.Move;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;


@Service
public final class Human extends Player {

    private CompletableFuture<Move> pendingMove = new CompletableFuture<>();

    public Human(String name, Colour colour) {
        super(name, colour);
    }

    public Move turn(GameState state) {

        //block until frontend chooses a move
        this.pendingMove = new CompletableFuture<>();
        return pendingMove.join();
    }

    //will be called when frontend chooses a move
    public void submitMove(Move move) {
        this.pendingMove.complete(move);
    }
}
