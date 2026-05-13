package com.tinbobs.chess.server.model.player;

import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.piece.*;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;

import java.util.*;


public final class Bot extends Player {

    private static final int MINIMAX_DEPTH = 5;
    private Player opponent;


    public Bot(String name, Colour colour) {
        super(name, colour);
    }

    public void giveBotOpponent(Player p) {
        this.opponent = p;
    }

    public Move turn(GameState state) {

        System.out.println("Bot is thinking...");

        //get all possible move and sort them by suspected best
        Comparator<Move> moveComparator = Comparator.comparingInt(move -> {
            GameState next = this.fakeAdvance(state, move);

            //negative so the best move is first
            return -this.score(next);
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
            GameState newState = this.fakeAdvance(state, move);
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
            return this.score(state);
        }

        //if we are not done then do another layer of recursion
        Set<Move> availableMoves = state.getLegalMoves();
        int res;

        if (state.currentTurn().getColour() == this.getColour()) {

            //it is our turn: we want to maximise our own score
            res = Integer.MIN_VALUE;
            for (Move move : availableMoves) {

                GameState nextState = this.fakeAdvance(state, move);
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

                GameState nextState = this.fakeAdvance(state, move);
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

    //maps a game state to an integer score based on how favourable it is
    private int score(GameState state) {

        int res = 0;

        List<Optional<Piece>> grid = state.board().getGrid();
        for (int i = 0; i < 64; i++) {

            //do nothing for blank grid space
            if (grid.get(i).isEmpty()) continue;

            Piece piece = grid.get(i).get();

            //reward the bot having material and punish the enemy having material
            boolean ours = piece.getColour() == this.getColour();
            int sign = ours ? 1 : -1;
            int material = piece.getValue() * 100;

            //knight in the middle is better that knight at the edge. Reward / punish this accordingly
            int positional = getPositionalBonus(piece, i, ours);
            res += sign * (material + positional);
        }

        return res;
    }

    //pieces in different places have different values. Maps a piece to this value
    private int getPositionalBonus(Piece piece, int index, boolean isOurs) {

        //tables are written as if we are white: flip if we are black
        int tableIndex = isOurs
                ? (this.getColour() == Colour.WHITE ? index : mirror(index))
                : (this.getColour() == Colour.WHITE ? mirror(index) : index);

        return piece.getPieceTable()[tableIndex];
    }

    //helper to flip a table
    private int mirror(int index) {
        int col = index % 8;
        int row = index / 8;
        return (7 - row) * 8 + col;
    }

    //advances a board when given a move
    private GameState fakeAdvance(GameState state, Move move) {

        Board newBoard = new Board(state.board().getGrid());
        Piece fromPiece = newBoard.getPieceAt(move.from()).orElseThrow();
        newBoard.removePieceAt(move.from());
        newBoard.setPieceAt(move.to(), fromPiece);

        Player nextPlayer = state.currentTurn().equals(this) ? this.opponent : this;

        return new GameState(newBoard, nextPlayer);
    }
}
