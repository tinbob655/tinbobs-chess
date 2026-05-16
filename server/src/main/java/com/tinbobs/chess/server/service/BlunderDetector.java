package com.tinbobs.chess.server.service;

import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public final class BlunderDetector {

    @Autowired
    private PositionEvaluator evaluator;

    //information for blunder detection
    private static final int BLUNDER_THRESHOLD = 300;
    private static final int DECISIVE_POSITION_THRESHOLD = 600;
    private static final int FORCED_MOVE_COUNT = 2;
    private static final int BLUNDER_MINIMAX_DEPTH = 3;

    public void checkForBlunder(Player currentPlayer, GameState stateBeforeMove, GameState stateAfterMove) {

        Set<Move> availableMoves = stateBeforeMove.getLegalMoves();

        //if the move is forced then don't punish the player
        if (availableMoves.size() <= FORCED_MOVE_COUNT) {
            return;
        }

        //give a score to the state before and after the move
        int scoreBeforeMove = 0;
        int scoreAfterMove = 0;
        scoreBeforeMove = this.evaluator.minimax(stateBeforeMove, BLUNDER_MINIMAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, currentPlayer.getColour());
        scoreAfterMove = this.evaluator.minimax(stateAfterMove, BLUNDER_MINIMAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, currentPlayer.getColour());

        //if a position was already winning or loosing then don't punish twice
        if (Math.abs(scoreBeforeMove) > DECISIVE_POSITION_THRESHOLD) {
            return;
        }

        //blunders happen if the evaluation drops
        if (scoreAfterMove < scoreBeforeMove - BLUNDER_THRESHOLD) {
            currentPlayer.addBlunder();
        }
    }
}
