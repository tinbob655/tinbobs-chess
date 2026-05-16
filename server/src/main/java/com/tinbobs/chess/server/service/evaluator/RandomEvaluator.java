package com.tinbobs.chess.server.service.evaluator;

import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Qualifier("random")
public final class RandomEvaluator implements Evaluator {

    private static final Random random = new Random();


    @Override
    public int evaluate(GameState state, Colour perspective) {
        return random.nextInt(0, 100);
    }

    @Override
    public int staticEvaluate(GameState state, Colour perspective) {
        return random.nextInt(0, 10);
    }

    @Override
    public @NonNull Queue<Move> getSortedMoves(GameState state) {

        //return the moves in a random order
        List<Move> allMoves = new java.util.ArrayList<>(state.getLegalMoves().stream().toList());
        Collections.shuffle(allMoves);
        return new PriorityQueue<>(allMoves);

    }

    @Override
    public void reset() {

        //don't need to do anything
    }
}
