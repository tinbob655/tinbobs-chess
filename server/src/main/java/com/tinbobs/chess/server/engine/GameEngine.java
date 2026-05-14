package com.tinbobs.chess.server.engine;


import com.tinbobs.chess.server.model.IllegalMoveException;
import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.piece.Piece;
import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.model.status.GameStatus;
import com.tinbobs.chess.server.service.CreateFrontendStatus;
import com.tinbobs.chess.server.service.MoveParser;
import com.tinbobs.chess.server.service.PositionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;
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


    //totally resets the game
    public void reset() {

        //create new state
        Board newBoard = new Board();
        this.players.forEach(Player::reset);
        this.state = new GameState(newBoard, this.players.get(0), this.players);

        //send new status to front
        GameStatus status = this.statusCreator.createFrontendStatus(this.players, this.state);
        this.messagingTemplate.convertAndSend("/topic/status", status);

        //log
        System.out.println("Game successfully reset!");
    }

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
        
        //save if this move was a capture move for later
        Optional<Piece> targetPiece = this.state.board().getPieceAt(move.to());

        //do the move
        this.state = this.state.advance(move);
        this.messagingTemplate.convertAndSend("/topic/game", moveParser.toRaw(move));
        this.checkForBlunder(currentPlayer, scoreBeforeTurn);

        //the move may have been a capture move (take the piece from the other (next) player)
        targetPiece.ifPresent(piece -> this.state.currentTurn().capture(piece));

        //send the new game state to the frontend
        GameStatus status = this.statusCreator.createFrontendStatus(this.players, this.state);
        this.messagingTemplate.convertAndSend("/topic/status", status);
    }

    private void checkForBlunder(Player currentPlayer, int scoreBeforeTurn) {

        //work out the score after we do the move and compare to detect a blunder
        GameState stateAfterOpponentBestReply = this.state.getLegalMoves().stream()
                .map(this.state::advance)
                .min(Comparator.comparingInt(s -> evaluator.evaluate(s, currentPlayer.getColour())))
                .orElse(this.state); // fallback if no moves (game over)

        int scoreAfterTurn = evaluator.evaluate(stateAfterOpponentBestReply, currentPlayer.getColour());
        if (scoreAfterTurn < scoreBeforeTurn - BLUNDER_THRESHOLD) {
            currentPlayer.addBlunder();
        }
    }
}
