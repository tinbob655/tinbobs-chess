package com.tinbobs.chess.server.engine;

import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.state.GameState;

public interface Engine_API {

    void startGame();
    void addPlayer(Player player);
    void turn();
    GameState getState();
}
