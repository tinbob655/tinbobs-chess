package com.tinbobs.chess.server.engine;


import com.tinbobs.chess.server.controller.GameController;
import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.piece.Piece;
import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public final class GameEngine implements Engine_API {

    private GameState state;
    private final List<Player> players = new ArrayList<>();
    private int turnIndex = 0;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameController controller;

    public GameEngine() {

        //create a board
        Board board = new Board();

        //TODO: ADD PIECES TO THE BOARD

        //create a game state
        this.state = this.recomputeState(board);
    }

    public void startGame() {

        //do not start if we don't have 2 players
        if (this.players.size() != 2) {
            throw new IllegalStateException("Cannot start the game without two players");
        }

        //need to run on a separate thread to do blocking
        CompletableFuture.runAsync(() -> {
            while (!this.state.isGameOver()) {
                this.turn();

                //send state to frontend
                messagingTemplate.convertAndSend("/topic/game", this.state);
            }
        });
    }

    public GameState getState() {
        return this.state;
    }

    public void addPlayer(Player player) {

        //refuse to add more than 2 players
        if (this.players.size() < 2) {
            this.players.add(player);
        }
    }

    public void turn() {

        Player currentPlayer = this.players.get(this.turnIndex);
        Move move = currentPlayer.turn(this.state);

        //validate the move
        Set<Move> validMoves = this.state.getLegalMoves();
        if (!validMoves.contains(move)) {
            throw new IllegalArgumentException("Illegal move");
        }

        //do the move
        this.state = this.advance(move);
        this.controller.sendMove(move);
    }

    private GameState advance(Move move) {

        //update the board
        Board board = this.state.board();
        Piece p = board.getPieceAt(move.from()).orElseThrow();
        board.removePieceAt(move.from());
        board.setPieceAt(move.to(), p);

        //increment the turn
        this.turnIndex = (this.turnIndex + 1) % this.players.size();

        //recompute and return state
        return this.recomputeState(board);
    }

    private GameState recomputeState(Board board) {
        return new GameState(board, this.players.get(this.turnIndex));
    }
}
