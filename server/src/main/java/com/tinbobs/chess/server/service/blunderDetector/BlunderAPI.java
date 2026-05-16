package com.tinbobs.chess.server.service.blunderDetector;

import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.state.GameState;

public interface BlunderAPI {

    boolean checkForBlunder(Player currentPlayer, GameState stateBeforeMove, GameState stateAfterMove);
}
