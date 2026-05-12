package com.tinbobs.chess.server.controller;


import com.tinbobs.chess.server.engine.GameEngine;
import com.tinbobs.chess.server.model.IllegalMoveException;
import com.tinbobs.chess.server.model.player.Human;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.model.state.MoveResult;
import com.tinbobs.chess.server.model.state.RawMove;
import com.tinbobs.chess.server.service.MoveParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Set;

@Controller
public class GameController {

    @Autowired
    private GameEngine gameEngine;

    @Autowired
    private Human humanPlayer;

    @Autowired
    private MoveParser moveParser;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    //start the game when told
    @MessageMapping("/start")
    public void startGame() {
        System.out.println("Game start signal received");
        gameEngine.startGame();
    }

    //move received from frontend
    @MessageMapping("/move")
    public void handleMove(RawMove raw) {

        //log receipt of the move
        System.out.println("Move received: " + raw.from() + "->" + raw.to());
        try {
            Move move = moveParser.toMove(raw);

            Set<Move> validMoves = gameEngine.getState().getLegalMoves();
            if (!validMoves.contains(move)) {
                throw new IllegalMoveException(move);
            }

            humanPlayer.submitMove(move);

            //tell the frontend the move was accepted
            messagingTemplate.convertAndSend("/topic/moveResult",
                    new MoveResult(true, raw.correlationID(), null));

        }
        catch (IllegalMoveException e) {

            //tell the frontend that the move was not accepted
            messagingTemplate.convertAndSend("/topic/moveResult",
                    new MoveResult(false, raw.correlationID(), e.getMessage()));
        }
    }
}
