package com.tinbobs.chess.server.engine;


import com.tinbobs.chess.server.model.IllegalMoveException;
import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.model.status.GameStatus;
import com.tinbobs.chess.server.service.CreateFrontendStatus;
import com.tinbobs.chess.server.service.MoveParser;
import com.tinbobs.chess.server.service.PositionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


public final class GameEngine implements Engine_API {

    private static final int BLUNDER_THRESHOLD = 300;
    private GameState state;
    private final List<Player> players = new ArrayList<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MoveParser moveParser;

    @Autowired
    private CreateFrontendStatus statusCreator;

    @Autowired
    private PositionEvaluator evaluator;


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

        //work out the score before the player makes a move
        Player currentPlayer = this.state.currentTurn();
        int scoreBeforeTurn = this.evaluator.evaluate(this.state, currentPlayer.getColour());

        Move move = currentPlayer.turn(this.state);

        //validate the move
        Set<Move> validMoves = this.state.getLegalMoves();
        if (!validMoves.contains(move)) {
            throw new IllegalMoveException(move);
        }

        //do the move
        this.state = this.state.advance(move);
        this.messagingTemplate.convertAndSend("/topic/game", moveParser.toRaw(move));

        //work out the score after we do the move and compare to detect a blunder
        GameState stateAfterOpponentBestReply = this.state.getLegalMoves().stream()
                .map(this.state::advance)
                .min(Comparator.comparingInt(s -> evaluator.evaluate(s, currentPlayer.getColour())))
                .orElse(this.state); // fallback if no moves (game over)

        int scoreAfterTurn = evaluator.evaluate(stateAfterOpponentBestReply, currentPlayer.getColour());
        if (scoreAfterTurn < scoreBeforeTurn - BLUNDER_THRESHOLD) {
            currentPlayer.addBlunder();
        }

        //send the new game state to the frontend
        GameStatus status = this.statusCreator.createFrontendStatus(this.players, this.state);
        this.messagingTemplate.convertAndSend("/topic/status", status);
    }
}
