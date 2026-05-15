package com.tinbobs.chess.server.model.player;

import com.tinbobs.chess.server.model.piece.*;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.service.PositionEvaluator;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;


public final class Bot extends Player {

    @Autowired
    private PositionEvaluator evaluator;

    private static final int MINIMAX_DEPTH = 4;


    //transposition table stuff
    private static final Map<Long, TTRow> transpositionTable = new HashMap<>();
    private enum TTFlag {PERFECT, MAXIMUM, MINIMUM}
    private record TTRow(int score, int depth, TTFlag flag) {}


    public Bot(String name, Colour colour) {
        super(name, colour);
    }

    //we also need to clear the transposition table on a game reset
    @Override
    public void reset() {
        super.reset();
        transpositionTable.clear();
    }

    @NonNull
    public Move turn(GameState state) {

        System.out.println("Bot is thinking...");

        //get all possible move and sort them by suspected best
        Queue<Move> sortedMoves = getSortedMoves(state);

        if (sortedMoves.isEmpty()) {
            throw new IllegalStateException("No possible moves");
        }

        //do minimax on each move
        Move bestMove = sortedMoves.peek();
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

        System.out.println("Bot decided on a move: " + bestMove.toString());

        return bestMove;
    }

    private static @NonNull Queue<Move> getSortedMoves(GameState state) {

        Comparator<Move> moveComparator = Comparator.comparingInt((Move move) -> {

            int score = 0;
            Optional<Piece> victim = state.board().getPieceAt(move.to());
            Optional<Piece> attacker = state.board().getPieceAt(move.from());

            if (victim.isPresent() && attacker.isPresent()) {
                score += (victim.get().getValue() * 100) - attacker.get().getValue();
            }
            return score;

        }).reversed();

        Queue<Move> sortedMoves = new PriorityQueue<>(moveComparator);
        Set<Move> legalMoves = state.getLegalMoves();
        sortedMoves.addAll(legalMoves);
        return sortedMoves;
    }

    private int minimax(GameState state, int depth, int alpha, int beta) {

        //we might be done
        if (state.isGameOver()) {
            return this.evaluator.evaluate(state, this.getColour());
        }
        else if (depth <= 0) {
            return this.quiescence(state, alpha, beta);
        }

        int originalAlpha = alpha;

        //we may have seen this state before at a sufficient depth
        long key = state.hashCode();
        TTRow cached = transpositionTable.get(key);
        if (cached != null && cached.depth() >= depth) {

            switch (cached.flag()) {
                case PERFECT: return cached.score();
                case MAXIMUM: beta = Math.min(beta, cached.score()); break;
                case MINIMUM: alpha = Math.max(alpha, cached.score()); break;
            }

            //prune
            if (beta <= alpha) {
                return cached.score();
            }
        }

        //if we are not done then do another layer of recursion
        Queue<Move> availableMoves = getSortedMoves(state);
        int res;

        if (state.currentTurn().getColour() == this.getColour()) {

            //it is our turn: we want to maximise our own score
            res = Integer.MIN_VALUE;
            while (!availableMoves.isEmpty()) {
                Move move = availableMoves.poll();

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
            while (!availableMoves.isEmpty()) {
                Move move = availableMoves.poll();

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

        //store our calculated value in the transposition table
        TTFlag flag;
        if (res <= originalAlpha) flag = TTFlag.MAXIMUM;
        else if (res >= beta) flag = TTFlag.MINIMUM;
        else flag = TTFlag.PERFECT;
        transpositionTable.put(key, new TTRow(res, depth, flag));

        return res;
    }

    //keeps evaluating states until all pieces are safe
    private int quiescence(GameState state, int alpha, int beta) {

        //if we choose not to pieceTaken we get this score
        int standPat = this.evaluator.evaluate(state, this.getColour());

        if (state.currentTurn().getColour() == this.getColour()) {

            //our turn
            if (standPat >= beta) return beta;
            alpha = Math.max(alpha, standPat);
        }
        else {

            //opponent's turn
            if (standPat <= alpha) return alpha;
            beta = Math.min(beta, standPat);
        }

        //now assume we will pieceTaken
        Set<Move> captures = state.getLegalMoves().stream()
                .filter(m -> state.board().getPieceAt(m.to()).isPresent())
                .collect(Collectors.toSet());

        if (captures.isEmpty()) return standPat;

        int res = standPat;
        if (state.currentTurn().getColour() == this.getColour()) {

            //our turn
            for (Move move : captures) {
                int score = quiescence(state.advance(move), alpha, beta);
                res = Math.max(res, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) break;
            }
        }
        else {

            //opponent's turn
            for (Move move : captures) {
                int score = quiescence(state.advance(move), alpha, beta);
                res = Math.min(res, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) break;
            }
        }
        return res;
    }
}
