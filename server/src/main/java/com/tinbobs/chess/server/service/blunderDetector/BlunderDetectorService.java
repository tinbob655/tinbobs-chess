package com.tinbobs.chess.server.service.blunderDetector;

import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.service.evaluator.Evaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public final class BlunderDetectorService implements BlunderDetector {

    private final Evaluator evaluator;

    @Autowired
    public BlunderDetectorService(Evaluator evaluator) {
        this.evaluator = evaluator;
    }


    //information for blunder detection
    private static final int BLUNDER_THRESHOLD = 300;
    private static final int DECISIVE_POSITION_THRESHOLD = 600;
    private static final int FORCED_MOVE_COUNT = 2;

    @Override
    public boolean checkForBlunder(Player currentPlayer, GameState stateBeforeMove, GameState stateAfterMove) {

        Set<Move> availableMoves = stateBeforeMove.getLegalMoves();

        //if the move is forced then don't punish the player
        if (availableMoves.size() <= FORCED_MOVE_COUNT) {
            return false;
        }

        //give a score to the state before and after the move
        int scoreBeforeMove;
        int scoreAfterMove;
        scoreBeforeMove = this.evaluator.evaluate(stateBeforeMove, currentPlayer.getColour());
        scoreAfterMove = this.evaluator.evaluate(stateAfterMove, currentPlayer.getColour());

        //if a position was already winning or loosing then don't punish twice
        if (Math.abs(scoreBeforeMove) > DECISIVE_POSITION_THRESHOLD) {
            return false;
        }

        //blunders happen if the evaluation drops
        return scoreAfterMove < scoreBeforeMove - BLUNDER_THRESHOLD;
    }
}
