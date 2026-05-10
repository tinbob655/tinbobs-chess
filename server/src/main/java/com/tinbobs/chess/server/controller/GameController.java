package com.tinbobs.chess.server.controller;


import com.tinbobs.chess.server.engine.GameEngine;
import com.tinbobs.chess.server.model.player.Human;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.model.state.RawMove;
import com.tinbobs.chess.server.service.MoveParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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
        gameEngine.startGame();
    }

    //move received from frontend
    @MessageMapping("/move")
    public void handleMove(RawMove raw) {
        Move move = moveParser.toMove(raw);
        humanPlayer.submitMove(move);
    }

    //send a move to the frontend
    public void sendMove(Move move) {
        RawMove raw = moveParser.toRaw(move);
        messagingTemplate.convertAndSend("/topic/game", raw);
    }
}
