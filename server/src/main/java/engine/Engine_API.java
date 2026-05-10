package engine;

import model.player.Player;
import model.state.GameState;

public interface Engine_API {

    void startGame();
    void addPlayer(Player player);
    void turn();
    GameState getState();
}
