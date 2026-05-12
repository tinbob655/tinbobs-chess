package com.tinbobs.chess.server.model.player;

import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.piece.Piece;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;


public final class Bot extends Player {

    private static final int MINIMAX_DEPTH = 3;
    private Player opponent;


    public Bot(String name, Colour colour) {
        super(name, colour);
    }

    public void giveBotOpponent(Player p) {
        this.opponent = p;
    }

    public Move turn(GameState state) {

        //get all possible move and sort them by suspected best
        Comparator<Move> moveComparator = Comparator.comparingInt(move -> this.score(this.fakeAdvance(state, move)));
        TreeSet<Move> sortedMoves = new TreeSet<>(moveComparator);
        sortedMoves.addAll(state.getLegalMoves());

        //do minimax on each move
        int bestScore = Integer.MIN_VALUE;
        Move bestMove = sortedMoves.first();
        while (!sortedMoves.isEmpty()) {

            Move move = sortedMoves.pollFirst();
            assert move != null;

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
        if (depth == 0 || state.isGameOver()) {
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

        //crude to start with: reward having our own pieces. Punish having opponent pieces
        return state.board().getGrid().parallelStream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .mapToInt(piece -> piece.getColour() == this.getColour() ? piece.getValue() : -piece.getValue())
                .sum();
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
