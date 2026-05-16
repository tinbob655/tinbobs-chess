package com.tinbobs.chess.server.service.evaluator;

import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import org.jspecify.annotations.NonNull;

import java.util.*;


public final class Greedy implements Evaluator {


    @Override
    public int evaluate(GameState state, Colour perspective) {

        //greedy algorithm will only ever look one move ahead => just use the static evaluate method
        return this.staticEvaluate(state, perspective);
    }

    @Override
    public int staticEvaluate(GameState state, Colour perspective) {

        Set<Move> legalMoves = state.getLegalMoves();
        return legalMoves.parallelStream()
                .mapToInt(move -> {

                    //pretend we did the move
                    GameState newState = state.advance(move);

                    //reward own material, punish enemy material
                    int res = newState.board().getGrid().stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .mapToInt(piece -> piece.getColour() == perspective ? piece.getValue() : -piece.getValue())
                            .sum();

                    //if we have more available moves then that is good
                    res += newState.getLegalMoves().size();

                    return res;
                }).sum();
    }

    @Override
    public @NonNull Queue<Move> getSortedMoves(GameState state) {

        //compare moves based on static evaluation
        Comparator<Move> moveComparator = Comparator.comparingInt((Move move) -> {

            //pretend we did the move
            Colour perspective = state.currentTurn().getColour();
            GameState newState = state.advance(move);
            return this.staticEvaluate(newState, perspective);
        }).reversed();

        Queue<Move> sortedMoves = new PriorityQueue<>(moveComparator);
        Set<Move> legalMoves = state.getLegalMoves();
        sortedMoves.addAll(legalMoves);
        return sortedMoves;
    }

    @Override
    public void reset() {
        //don't need to do anything
    }
}
