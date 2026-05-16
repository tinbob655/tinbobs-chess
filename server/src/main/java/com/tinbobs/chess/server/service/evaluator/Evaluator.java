package com.tinbobs.chess.server.service.evaluator;

import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import org.jspecify.annotations.NonNull;

import java.util.Queue;

public interface Evaluator {

    int evaluate(GameState state, Colour perspective);
    int staticEvaluate(GameState state, Colour perspective);
    @NonNull Move bestMove(GameState state, Colour perspective);
    @NonNull Queue<Move> getSortedMoves(GameState state);
    void reset();
}
