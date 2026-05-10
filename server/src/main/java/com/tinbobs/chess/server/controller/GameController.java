package com.tinbobs.chess.server.controller;


import com.tinbobs.chess.server.service.ChessService;
import engine.GameEngine;
import model.state.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChessService chessService;

    @MessageMapping("/start")
    public void startGame() {
        GameEngine.getInstance().startGame();
    }

    @MessageMapping("/move")
    public void handleMove(Move playerMove) {
    }
}
