package com.tinbobs.chess.server.model.state;


import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.board.ZobristTable;
import com.tinbobs.chess.server.model.piece.*;
import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.status.Status;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Collectors;


public final class GameState implements StateAPI {

    private final Board board;
    private final Player currentTurn;
    private final List<Player> players;
    private Set<Move> cachedLegalMoves = null;
    private Status cachedGameStatus = null;
    private final long zorbristHash;
    private final Map<Long, Integer> history;

    //create a blank state (no pre-existing history)
    public GameState(Board board, Player currentTurn, List<Player> players) {
        this.board = board;
        this.currentTurn = currentTurn;
        this.players = players;
        this.zorbristHash = this.computeZobristHash();
        this.history = new HashMap<>();
        this.history.put(this.zorbristHash, 1);
    }
    //create a state with a pre-existing history
    private GameState(Board board, Player currentTurn, List<Player> players, Map<Long, Integer> previousHistory) {
        this.board = board;
        this.currentTurn = currentTurn;
        this.players = players;
        this.zorbristHash = this.computeZobristHash();
        this.history = new HashMap<>(previousHistory);
        this.history.merge(this.zorbristHash, 1, Integer::sum);
    }

    //advances the game state
    @Override
    public @NonNull GameState advance(Move move) {

        //update the board
        Board newBoard = new Board(this.board().getGrid());
        Piece fromPiece = newBoard.getPieceAt(move.from()).orElseThrow();
        newBoard.removePieceAt(move.from());
        newBoard.setPieceAt(move.to(), fromPiece);

        //find the next player
        Player nextPlayer = players.stream()
                .filter(p -> p.getColour() != this.currentTurn().getColour())
                .findFirst()
                .orElseThrow();

        //pawns become queens if they reach the end of the board
        boolean isWhitePawnPromoting = (fromPiece instanceof Pawn) && (fromPiece.getColour() == Colour.WHITE) && (move.to().y() == 8);
        boolean isBlackPawnPromoting = (fromPiece instanceof Pawn) && (fromPiece.getColour() == Colour.BLACK) && (move.to().y() == 1);
        if (isWhitePawnPromoting) {
            newBoard.setPieceAt(move.to(), new Queen(Colour.WHITE));
        }
        else if (isBlackPawnPromoting) {
            newBoard.setPieceAt(move.to(), new Queen(Colour.BLACK));
        }

        return new GameState(newBoard, nextPlayer, players, this.history);
    }

    //getters
    @Override
    public Board board() {
        return this.board;
    }
    @Override
    public Player currentTurn() {
        return this.currentTurn;
    }
    @Override
    public long getZorbristHash() {
        return this.zorbristHash;
    }

    @Override
    public @NonNull Set<Move> getLegalMoves() {

        //only calculate moves once to save computation
        if (this.cachedLegalMoves != null) {
            return this.cachedLegalMoves;
        }
        
        Set<Move> res = new HashSet<>();

        //add all pieces legal moves
        for (int i = 0; i < 64; i++) {
            Position pos = new Position(i);

            Optional<Piece> maybePiece = board.getPieceAt(pos);
            if (maybePiece.isEmpty()) continue;

            Piece p = maybePiece.get();
            if (p.getColour() == currentTurn.getColour()) {
                res.addAll(p.getLegalMoves(pos, board));
            }
        }

        //filter out all moves which result in a check
        res = res.parallelStream()
                .filter(move -> {

                    //pretend we did the move
                    GameState fakeState = this.advance(move);
                    return !fakeState.isInCheck(this.currentTurn().getColour());
                })
                .collect(Collectors.toSet());

        this.cachedLegalMoves = res;
        return res;
    }

    @Override
    public boolean isGameOver() {
        Status s = this.getStatus();
        return (s == Status.CHECKMATE) || (s == Status.STALEMATE) || (s == Status.DRAW);
    }


    //equality
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameState s)) {
            return false;
        }

        return (
                (s.board().equals(this.board))
                && (s.currentTurn().equals(this.currentTurn))
                );
    }
    @Override
    public int hashCode() {
        long boardHash = ZobristTable.hash(this.board.getGrid());
        long turnHash = (long) this.currentTurn.getColour().hashCode() * 0x9E3779B97F4A7C15L;
        long combined = boardHash ^ turnHash;
        return Long.hashCode(combined);
    }

    @NonNull
    @Override
    public Status getStatus() {

        if (this.cachedGameStatus == null) {
            this.cachedGameStatus = this.calculateFirstStatus();
        }
        return this.cachedGameStatus;
    }

    @NonNull
    private Status calculateFirstStatus() {

        boolean inCheck = isInCheck(this.currentTurn().getColour());
        boolean hasMoves = !getLegalMoves().isEmpty();

        //a checkmate happens if we are in check and can't move
        if (inCheck && !hasMoves) return Status.CHECKMATE;

        //a stalemate happens if we can't move
        if (!inCheck && !hasMoves) return Status.STALEMATE;

        //a check happens if we are in check (surprisingly)
        if (inCheck) return Status.CHECK;

        //a draw happens if we don't have enough material
        if (isInsufficientMaterial()) return Status.DRAW;

        //a draw also happens if we repeat the same state three times
        if (this.history.getOrDefault(this.zorbristHash, 0) >= 3) return Status.DRAW;

        return Status.ONGOING;
    }

    //detects a check
    private boolean isInCheck(Colour colour) {

        Position kingPos = board.findPiece(new King(colour)).orElse(null);
        if (kingPos == null) return false;

        //repeat for all opponent pieces
        for (int i = 0; i < 64; i++) {
            Position pos = new Position(i);
            Optional<Piece> piece = board.getPieceAt(pos);
            if (piece.isEmpty()) continue;
            if (piece.get().getColour() == colour) continue;

            //is the opponent able to take our king
            boolean attacks = piece.get()
                    .getLegalMoves(pos, board)
                    .stream()
                    .anyMatch(m -> m.to().equals(kingPos));

            if (attacks) return true;
        }

        return false;
    }


    //a draw happens when both sizes don't have enough material
    private boolean isInsufficientMaterial() {

        List<Piece> pieces = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            board.getPieceAt(new Position(i)).ifPresent(pieces::add);
        }

        if (pieces.size() == 2) return true;
        if (pieces.size() == 3) {
            return pieces.stream().anyMatch(p -> p instanceof Bishop || p instanceof Knight);
        }

        return false;
    }

    private long computeZobristHash() {
        long boardHash = ZobristTable.hash(this.board.getGrid());
        long turnHash = (long) this.currentTurn.getColour().hashCode() * 0x9E3779B97F4A7C15L;
        return boardHash ^ turnHash;
    }
}
