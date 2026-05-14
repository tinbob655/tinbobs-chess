package com.tinbobs.chess.server.model.player;

import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.piece.Piece;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import org.jspecify.annotations.NonNull;

public abstract class Player {

    private final String name;
    private final Colour colour;
    private int material;
    private int blunders;

    public Player(String name, Colour colour) {
        this.name = name;
        this.colour = colour;
        this.material = 39;
        this.blunders = 0;
    }

    public String getName() {
        return this.name;
    }
    public Colour getColour() {
        return this.colour;
    }
    public int getMaterial() {
        return this.material;
    }
    public int getBlunders() {
        return this.blunders;
    }

    //will happen if we LOOSE a piece
    public void capture(Piece piece) {
        this.material -= piece.getValue();
    }
    public void addBlunder() {
        this.blunders++;
    }

    public void reset() {
        this.material = 39;
        this.blunders = 0;
    }

    //abstract methods players must implement
    @NonNull
    public abstract Move turn(GameState state);
}
