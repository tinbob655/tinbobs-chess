package com.tinbobs.chess.server.engine;


import com.tinbobs.chess.server.model.IllegalMoveException;
import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.piece.Piece;
import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.GameStateFactory;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.model.status.GameStatus;
import com.tinbobs.chess.server.service.CreateFrontendStatus;
import com.tinbobs.chess.server.service.MoveParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


public final class GameEngine implements Engine_API {

    private GameState state;
    private final List<Player> players = new ArrayList<>();
    private int turnIndex = 0;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MoveParser moveParser;

    @Autowired
    private CreateFrontendStatus statusCreator;

    @Autowired
    private GameStateFactory gameStateFactory;


    public void startGame() {

        //do not start if we don't have 2 players
        if (this.players.size() != 2) {
            throw new IllegalStateException("Cannot start the game without two players");
        }

        //create a board
        Board board = new Board();

        //create a game state
        this.state = this.recomputeState(board);

        //need to run on a separate thread to do blocking
        CompletableFuture.runAsync(() -> {
            while (!this.state.isGameOver()) {
                this.turn();
            }
        });
    }

    public GameState getState() {
        return this.state;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public void addPlayer(Player player) {

        //refuse to add more than 2 players
        if (this.players.size() < 2) {
            this.players.add(player);
        }
    }

    public void turn() {

        Player currentPlayer = this.currentTurn();
        Move move = currentPlayer.turn(this.state);

        //validate the move
        Set<Move> validMoves = this.state.getLegalMoves();
        if (!validMoves.contains(move)) {
            throw new IllegalMoveException(move);
        }

        //do the move
        this.state = this.advance(move);
        this.messagingTemplate.convertAndSend("/topic/game", moveParser.toRaw(move));

        //send the new game state to the frontend
        GameStatus status = this.statusCreator.createFrontendStatus(this.players, this.state);
        this.messagingTemplate.convertAndSend("/topic/status", status);
    }
    
    private Player currentTurn() {
        return this.players.get(this.turnIndex);
    }

    private GameState advance(Move move) {

        //update the board
        Board board = this.state.board();
        Piece p = board.getPieceAt(move.from()).orElseThrow();
        board.removePieceAt(move.from());
        
        //if there is a piece at the target location then this is a capture move
        board.getPieceAt(move.to()).ifPresent(piece -> this.currentTurn().capture(piece));

        board.setPieceAt(move.to(), p);

        //increment the turn
        this.turnIndex = (this.turnIndex + 1) % this.players.size();

        //recompute and return state
        return this.recomputeState(board);
    }

    private GameState recomputeState(Board board) {
        return this.gameStateFactory.create(board, this.currentTurn(), this.players);
    }
}
