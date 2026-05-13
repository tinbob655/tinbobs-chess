package com.tinbobs.chess.server.service;


import com.tinbobs.chess.server.engine.GameEngine;
import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.piece.Piece;
import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.GameStateFactory;
import com.tinbobs.chess.server.model.state.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class FakeAdvance {

    @Autowired
    private GameStateFactory gameStateFactory;

    //advances a board when given a move
    public GameState fakeAdvance(GameState state, Move move, List<Player> players) {

        //update the board
        Board newBoard = new Board(state.board().getGrid());
        Piece fromPiece = newBoard.getPieceAt(move.from()).orElseThrow();
        newBoard.removePieceAt(move.from());
        newBoard.setPieceAt(move.to(), fromPiece);

        //find the next player
        Player nextPlayer = players.stream()
                .filter(p -> p.getColour() != state.currentTurn().getColour())
                .findFirst()
                .orElseThrow();

        return this.gameStateFactory.create(newBoard, nextPlayer, players);
    }
}
