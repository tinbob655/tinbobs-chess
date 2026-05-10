package com.tinbobs.chess.server.model.player;

import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.piece.Piece;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;

public abstract class Player {

    private final String name;
    private final Colour colour;

    public Player(String name, Colour colour) {
        this.name = name;
        this.colour = colour;
    }

    public String getName() {
        return this.name;
    }
    public Colour getColour() {
        return this.colour;
    }

    //abstract methods players must implement
    public abstract Move turn(GameState state);
}
