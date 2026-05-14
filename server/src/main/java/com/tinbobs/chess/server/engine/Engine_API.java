package com.tinbobs.chess.server.engine;

import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.state.GameState;

import java.util.List;

public interface Engine_API {

    void startGame();
    void addPlayer(Player player);
    void turn();
    GameState getState();
}
