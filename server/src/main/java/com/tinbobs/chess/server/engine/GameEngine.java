package com.tinbobs.chess.server.engine;


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
public class GameEngine implements Engine_API {

    private GameState state = new GameState();
    private final List<Player> players = new ArrayList<>();
    private int turnIndex = 0;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void startGame() {

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
        this.players.add(player);
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

        //increment turn
        this.turnIndex = (this.turnIndex + 1) % this.players.size();
    }

    private GameState advance(Move move) {
        return null;
    }
}
