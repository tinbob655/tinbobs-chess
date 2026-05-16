package com.tinbobs.chess.server.service.evaluator;

import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.piece.Piece;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Qualifier("minimax")
@Primary
public final class MinimaxEvaluator implements Evaluator {


    private static final int MINIMAX_DEPTH = 4;

    //transposition table stuff
    private final Map<Long, TTRow> transpositionTable = new HashMap<>();
    private enum TTFlag {PERFECT, MAXIMUM, MINIMUM}
    private record TTRow(int score, int depth, TTFlag flag) {}

    @Override
    public int evaluate(GameState state, Colour perspective) {
        return this.minimax(state, MINIMAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, perspective);
    }

    //gives a state a score from a player's perspective
    @Override
    public int staticEvaluate(GameState state, Colour perspective) {

        int res = 0;
        List<Optional<Piece>> grid = state.board().getGrid();

        for (int i = 0; i < 64; i++) {

            if (grid.get(i).isEmpty()) continue;
            Piece piece = grid.get(i).get();

            //our piece: add. Opponent's piece: subtract
            int sign = piece.getColour() == perspective ? 1 : -1;
            int material = piece.getValue() * 100;

            //tables are written from white's POV so need to flip if we are black
            boolean pieceIsWhite = piece.getColour() == Colour.WHITE;
            boolean perspectiveIsWhite = perspective == Colour.WHITE;
            int tableIndex = (pieceIsWhite == perspectiveIsWhite) ? i : mirror(i);
            int positional = piece.getPieceTable()[tableIndex];

            res += sign * (material + positional);
        }

        //a piece with more available moves is in a better position
        int mobilityBonus = state.currentTurn().getColour() == perspective ? 5 : -5;
        res += mobilityBonus * state.getLegalMoves().size();

        return res;
    }

    //gets moves in order of predicted best to worst
    @Override
    public @NonNull Queue<Move> getSortedMoves(GameState state) {

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

    //transposition table will be cleared on a reset
    @Override
    public void reset() {
        transpositionTable.clear();
    }

    //evaluates state upto a depth
    public int minimax(GameState state, int depth, int alpha, int beta, Colour perspective) {


        //we might be done
        if (state.isGameOver()) {
            return this.staticEvaluate(state, perspective);
        }
        else if (depth <= 0) {
            return this.quiescence(state, alpha, beta, perspective);
        }

        int originalAlpha = alpha;

        //we may have seen this state before at a sufficient depth
        long key = state.hashCode() ^ (perspective == Colour.WHITE ? 0xDEADBEEFL : 0xCAFEBABEL);
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

        if (state.currentTurn().getColour() == perspective) {

            //it is our turn: we want to maximise our own score
            res = Integer.MIN_VALUE;
            while (!availableMoves.isEmpty()) {
                Move move = availableMoves.poll();

                GameState nextState = state.advance(move);
                int nextScore = this.minimax(nextState, depth - 1, alpha, beta, perspective);

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
                int nextScore = this.minimax(nextState, depth -1, alpha, beta, perspective);

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
    private int quiescence(GameState state, int alpha, int beta, Colour perspective) {


        //if we choose not to pieceTaken we get this score
        int standPat = this.staticEvaluate(state, perspective);

        if (state.currentTurn().getColour() == perspective) {

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
        if (state.currentTurn().getColour() == perspective) {

            //our turn
            for (Move move : captures) {
                int score = quiescence(state.advance(move), alpha, beta, perspective);
                res = Math.max(res, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) break;
            }
        }
        else {

            //opponent's turn
            for (Move move : captures) {
                int score = quiescence(state.advance(move), alpha, beta, perspective);
                res = Math.min(res, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) break;
            }
        }
        return res;
    }

    //helper to flip a table
    private int mirror(int index) {
        int col = index % 8;
        int row = index / 8;
        return (7 - row) * 8 + col;
    }
}
