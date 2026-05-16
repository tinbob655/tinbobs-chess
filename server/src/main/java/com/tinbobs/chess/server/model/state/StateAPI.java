package com.tinbobs.chess.server.model.state;

import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.status.Status;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public interface StateAPI {

    //getters
    Board board();
    Player currentTurn();
    long getZorbristHash();

    //equality
    boolean equals(Object o);
    int hashCode();


    //GameState should be able to:
    //apply a move
    @NonNull
    GameState advance(Move move);

    //calculate all possible moves
    @NonNull
    Set<Move> getLegalMoves();

    //calculate if the game is over
    boolean isGameOver();

    //calculate the status of the game
    @NonNull
    Status getStatus();
}
