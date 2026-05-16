package com.tinbobs.chess.server.engine;


import com.tinbobs.chess.server.model.IllegalMoveException;
import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.piece.Pawn;
import com.tinbobs.chess.server.model.piece.Piece;
import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.model.status.GameStatus;
import com.tinbobs.chess.server.service.BlunderDetector;
import com.tinbobs.chess.server.service.CreateFrontendStatus;
import com.tinbobs.chess.server.service.MoveParser;
import com.tinbobs.chess.server.service.PositionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;
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

    @Autowired
    private PositionEvaluator evaluator;

    @Autowired
    private BlunderDetector blunderDetector;


    //totally resets the game
    @Override
    public void reset() {

        //create new state
        Board newBoard = new Board();
        this.players.forEach(Player::reset);
        this.state = new GameState(newBoard, this.players.get(0), this.players);

        //send new status to front
        GameStatus status = this.statusCreator.createFrontendStatus(this.players, this.state);
        this.messagingTemplate.convertAndSend("/topic/status", status);

        //clear the transposition table
        this.evaluator.clearTranspositionTable();

        //log
        System.out.println("Game successfully reset!");
    }

    @Override
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

            //the game is over, tell the frontend
            this.messagingTemplate.convertAndSend("/topic/gameOver", this.state.getStatus());
        });
    }

    @Override
    public GameState getState() {
        return this.state;
    }

    @Override
    public void addPlayer(Player player) {

        //refuse to add more than 2 players
        if (this.players.size() < 2) {
            this.players.add(player);
        }
    }

    @Override
    public void turn() {

        //get a move
        Player currentPlayer = this.state.currentTurn();
        Move move = currentPlayer.turn(this.state);

        //validate the move
        Set<Move> validMoves = this.state.getLegalMoves();
        if (!validMoves.contains(move)) {
            throw new IllegalMoveException(move);
        }
        
        //save if this move was a capture move for later
        Optional<Piece> targetPiece = this.state.board().getPieceAt(move.to());

        //do the move
        GameState oldState = this.state;
        this.state = this.state.advance(move);
        this.messagingTemplate.convertAndSend("/topic/game", moveParser.toRaw(move));
        this.blunderDetector.checkForBlunder(currentPlayer, oldState, this.state);

        //the move may have been a capture move
        //take it from the next player's pieces and add it to the current player's captured pieces
        targetPiece.ifPresent(piece -> {
            this.state.currentTurn().pieceTaken(piece);
            currentPlayer.takePiece(piece);
        });

        //deal with pawn promotions
        boolean wasPromotion = (this.state.board().getPieceAt(move.from()).orElse(null) instanceof Pawn)
                && ((currentPlayer.getColour() == Colour.WHITE && move.to().y() == 8)
                ||  (currentPlayer.getColour() == Colour.BLACK && move.to().y() == 1));
        if (wasPromotion) {
            currentPlayer.promotion();
        }

        //send the new game state to the frontend
        GameStatus status = this.statusCreator.createFrontendStatus(this.players, this.state);
        this.messagingTemplate.convertAndSend("/topic/status", status);
    }
}
