package com.tinbobs.chess.server.model.player;

import com.tinbobs.chess.server.model.piece.*;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.service.PositionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


public final class Bot extends Player {

    @Autowired
    private PositionEvaluator evaluator;


    private static final int MINIMAX_DEPTH = 5;


    public Bot(String name, Colour colour) {
        super(name, colour);
    }

    public Move turn(GameState state) {

        System.out.println("Bot is thinking...");

        //get all possible move and sort them by suspected best
        Comparator<Move> moveComparator = Comparator.comparingInt(move -> {
            GameState next = state.advance(move);

            //negative so the best move is first
            return -this.evaluator.evaluate(next, this.getColour());
        });
        Queue<Move> sortedMoves = new PriorityQueue<>(moveComparator);
        Set<Move> legalMoves = state.getLegalMoves();
        sortedMoves.addAll(legalMoves);

        if (sortedMoves.isEmpty()) {
            throw new IllegalStateException("No possible moves");
        }

        //do minimax on each move
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        while (!sortedMoves.isEmpty()) {

            Move move = sortedMoves.poll();

            //pretend we did the move
            GameState newState = state.advance(move);
            int moveScore = this.minimax(newState, MINIMAX_DEPTH -1, Integer.MIN_VALUE, Integer.MAX_VALUE);

            //keep track of the best move
            if (moveScore > bestScore) {
                bestScore = moveScore;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private int minimax(GameState state, int depth, int alpha, int beta) {

        //we might be done
        if (depth <= 0 || state.isGameOver()) {
            return this.evaluator.evaluate(state, this.getColour());
        }

        //if we are not done then do another layer of recursion
        Set<Move> availableMoves = state.getLegalMoves();
        int res;

        if (state.currentTurn().getColour() == this.getColour()) {

            //it is our turn: we want to maximise our own score
            res = Integer.MIN_VALUE;
            for (Move move : availableMoves) {

                GameState nextState = state.advance(move);
                int nextScore = this.minimax(nextState, depth - 1, alpha, beta);

                //if we found the next best thing
                alpha = Math.max(alpha, nextScore);
                res = Math.max(res, nextScore);

                //might be able to prune this branch
                if (beta <= alpha) {
                    break;
                }
            }
        }
        else {

            //it is our opponent's turn: assume they will try to minimise our own score
            res = Integer.MAX_VALUE;
            for (Move move : availableMoves) {

                GameState nextState = state.advance(move);
                int nextScore = this.minimax(nextState, depth -1, alpha, beta);

                //if we found the next worst thing
                beta = Math.min(beta, nextScore);
                res = Math.min(res, nextScore);

                //we might be able to prune this branch
                if (beta <= alpha) {
                    break;
                }
            }
        }

        return res;
    }
}
