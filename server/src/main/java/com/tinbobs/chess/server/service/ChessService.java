package com.tinbobs.chess.server.service;
import engine.GameEngine;
import model.state.Move;
import org.springframework.stereotype.Service;

@Service
public class ChessService {

    public Move processHumanMove(Move humanMove) {

        GameEngine engine = GameEngine.getInstance();
    }
}
