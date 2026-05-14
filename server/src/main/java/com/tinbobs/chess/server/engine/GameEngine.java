package com.tinbobs.chess.server.engine;


import com.tinbobs.chess.server.model.IllegalMoveException;
import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.state.GameState;
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

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MoveParser moveParser;

    @Autowired
    private CreateFrontendStatus statusCreator;


    public void startGame() {

        //do not start if we don't have 2 players
        if (this.players.size() != 2) {
            throw new IllegalStateException("Cannot start the game without two players");
        }

        //create a board
        Board board = new Board();

        //create a game state
        this.state = new GameState(board, this.players.get(0), this.players);

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

        Player currentPlayer = this.state.currentTurn();
        Move move = currentPlayer.turn(this.state);

        //validate the move
        Set<Move> validMoves = this.state.getLegalMoves();
        if (!validMoves.contains(move)) {
            throw new IllegalMoveException(move);
        }

        //do the move
        this.state = this.state.advance(move);
        this.messagingTemplate.convertAndSend("/topic/game", moveParser.toRaw(move));

        //send the new game state to the frontend
        GameStatus status = this.statusCreator.createFrontendStatus(this.players, this.state);
        this.messagingTemplate.convertAndSend("/topic/status", status);
    }
}
