package com.tinbobs.chess.server.service.evaluator;

import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.piece.Piece;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.model.status.Status;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public final class Minimax implements Evaluator {


    private static final int MINIMAX_DEPTH = 4;

    //transposition table stuff
    private final Map<Long, TTRow> transpositionTable = new ConcurrentHashMap<>();
    private enum TTFlag {PERFECT, MAXIMUM, MINIMUM}
    private record TTRow(int score, int depth, TTFlag flag, Move bestMove) {}

    //killer moves stuff
    private static final int KILLER_SLOTS = 2;
    private final Move[][] killerMoves = new Move[MINIMAX_DEPTH + 10][KILLER_SLOTS];

    //parallelism stuff
    private final ExecutorService threadPool;
    private final int cpuCount;

    //create a thread pool
    public Minimax() {
        this.cpuCount = Math.max(1, Runtime.getRuntime().availableProcessors());
        this.threadPool = Executors.newFixedThreadPool(cpuCount);
    }

    //kill the thread pool on shutdown
    public void shutdown() {
        this.threadPool.shutdownNow();
    }

    @Override
    public int evaluate(GameState state, Colour perspective) {
        return this.minimax(state, MINIMAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, perspective, 0);
    }

    //gives a state a score from a player's perspective
    @Override
    public int staticEvaluate(GameState state, Colour perspective) {

        //checkmate is the goal
        if (state.getStatus() == Status.CHECKMATE) {
            return (state.currentTurn().getColour() == perspective ? Integer.MAX_VALUE : Integer.MIN_VALUE) / 2;
        }

        int res = 0;
        int whiteMobility = 0;
        int blackMobility = 0;
        List<Optional<Piece>> grid = state.board().getGrid();

        for (int i = 0; i < 64; i++) {

            //get the piece at the index
            Position pos = new Position(i);
            Optional<Piece> maybePiece = state.board().getPieceAt(pos);
            if (maybePiece.isEmpty()) continue;

            Piece piece = maybePiece.get();
            int moveCount = piece.getLegalMoves(pos, state.board()).size();

            //reward / punish mobility
            if (piece.getColour() == Colour.WHITE) {
                whiteMobility += moveCount;
            }
            else {
                blackMobility += moveCount;
            }
        }

        //a piece with more available moves is in a better position
        int mobility = perspective == Colour.WHITE
                ? whiteMobility - blackMobility
                : blackMobility - whiteMobility;

        res += 5 * mobility;

        return res;
    }

    //gets moves in order of predicted best to worst
    @Override
    public @NonNull Queue<Move> getSortedMoves(GameState state, Colour perspective) {
        return this.getSortedMovesWithTranspositionTable(state, null, null);
    }

    //transposition table will be cleared on a reset
    @Override
    public void reset() {
        transpositionTable.clear();

        //reset the killer moves
        for (Move[] slot : killerMoves) {
            Arrays.fill(slot, null);
        }
    }

    @Override
    @NonNull
    public Move bestMove(GameState state, Colour perspective) {

        // Helper threads seed the TT at a shallower depth
        List<Future<?>> helpers = new ArrayList<>();
        for (int t = 0; t < this.cpuCount - 1; t++) {
            helpers.add(this.threadPool.submit(() -> {
                try {
                    this.searchAtDepth(state, perspective, MINIMAX_DEPTH - 1);
                }
                catch (Exception ignored) {}
            }));
        }

        // Main thread does the authoritative search
        Move result = this.searchAtDepth(state, perspective, MINIMAX_DEPTH);
        helpers.forEach(f -> f.cancel(true));
        return result;
    }

    private @NonNull Move searchAtDepth(GameState state, Colour perspective, int depth) {

        //check the transposition table for a precomputed move
        long key = state.getZorbristHash() ^ (perspective == Colour.WHITE ? 0xDEADBEEFL : 0xCAFEBABEL);
        TTRow cached = transpositionTable.get(key);
        Move ttMove = (cached != null) ? cached.bestMove() : null;

        //setup for minimax
        Queue<Move> sortedMoves = getSortedMovesWithTranspositionTable(state, ttMove, null);
        Move bestMove = sortedMoves.peek();
        int alpha = Integer.MIN_VALUE;
        int beta  = Integer.MAX_VALUE;

        //do minimax on each move
        while (!sortedMoves.isEmpty()) {
            Move move = sortedMoves.poll();
            int score = minimax(state.advance(move), depth - 1, alpha, beta, perspective, 1);
            if (score > alpha) {
                alpha = score;
                bestMove = move;
            }
        }

        assert(bestMove != null);
        return bestMove;
    }

    //evaluates state upto a depth
    public int minimax(GameState state, int depth, int alpha, int beta, Colour perspective, int ply) {


        //we might be done
        if (state.isGameOver()) {
            return this.staticEvaluate(state, perspective);
        }
        else if (depth <= 0) {
            return this.quiescence(state, alpha, beta, perspective);
        }

        int originalAlpha = alpha;

        //we may have seen this state before at a sufficient depth
        long key = state.getZorbristHash() ^ (perspective == Colour.WHITE ? 0xDEADBEEFL : 0xCAFEBABEL);
        TTRow cached = transpositionTable.get(key);
        Move ttMove = (cached != null) ? cached.bestMove() : null;
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
        Move[] killers = (ply < killerMoves.length) ? killerMoves[ply] : null;
        Queue<Move> availableMoves = this.getSortedMovesWithTranspositionTable(state, ttMove, killers);
        int res;
        Move bestMove = availableMoves.peek();

        if (state.currentTurn().getColour() == perspective) {

            //it is our turn: we want to maximise our own score
            res = Integer.MIN_VALUE;
            while (!availableMoves.isEmpty()) {
                Move move = availableMoves.poll();

                GameState nextState = state.advance(move);
                int nextScore = this.minimax(nextState, depth - 1, alpha, beta, perspective, ply + 1);

                //if we found the next best thing
                alpha = Math.max(alpha, nextScore);
                if (nextScore > res) {
                    res = nextScore;
                    bestMove = move;
                }

                //might be able to prune this branch
                if (beta <= alpha) {
                    this.updateKillers(move, ply);
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
                int nextScore = this.minimax(nextState, depth -1, alpha, beta, perspective, ply + 1);

                //if we found the next worst thing
                beta = Math.min(beta, nextScore);
                if (nextScore < res) {
                    res = nextScore;
                    bestMove = move;
                }

                //we might be able to prune this branch
                if (beta <= alpha) {
                    this.updateKillers(move, ply);
                    break;
                }
            }
        }

        //store our calculated value in the transposition table
        TTFlag flag;
        if (res <= originalAlpha) flag = TTFlag.MAXIMUM;
        else if (res >= beta) flag = TTFlag.MINIMUM;
        else flag = TTFlag.PERFECT;
        transpositionTable.put(key, new TTRow(res, depth, flag, bestMove));

        return res;
    }

    @NonNull
    private Queue<Move> getSortedMovesWithTranspositionTable(GameState state, Move ttMove, Move[] killers) {
        Comparator<Move> moveComparator = Comparator.comparingInt((Move move) -> {

            //we may have already computed the move
            if (move.equals(ttMove)) return 10_000;

            int score = 0;
            Optional<Piece> victim = state.board().getPieceAt(move.to());
            Optional<Piece> attacker = state.board().getPieceAt(move.from());

            if (victim.isPresent() && attacker.isPresent()) {
                score += (victim.get().getValue() * 100) - attacker.get().getValue();
            }

            //the move may be a killer
            if (killers != null) {
                if (move.equals(killers[0])) return 900;
                if (move.equals(killers[1])) return 800;
            }

            return score;

        }).reversed();

        Queue<Move> sortedMoves = new PriorityQueue<>(moveComparator);
        sortedMoves.addAll(state.getLegalMoves());
        return sortedMoves;
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

    //keeps out killer moves up to date on beta cutoff
    private void updateKillers(Move move, int ply) {
        if (ply < killerMoves.length && !move.equals(killerMoves[ply][0])) {
            killerMoves[ply][1] = killerMoves[ply][0];
            killerMoves[ply][0] = move;
        }
    }
}
