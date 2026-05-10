package com.tinbobs.chess.server.controller;


import com.tinbobs.chess.server.engine.GameEngine;
import com.tinbobs.chess.server.model.player.Human;
import com.tinbobs.chess.server.model.state.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {

    @Autowired
    private GameEngine gameEngine;

    @Autowired
    private Human humanPlayer;

    @MessageMapping("/start")
    public void startGame() {
        gameEngine.startGame();
    }

    @MessageMapping("/move")
    public void handleMove(Move playerMove) {
        humanPlayer.submitMove(playerMove);
    }
}
