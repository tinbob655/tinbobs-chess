package com.tinbobs.chess.server.model.player;

import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.piece.Piece;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;

public abstract class Player {

    private final String name;
    private final Colour colour;
    private int score;
    private int blunders;

    public Player(String name, Colour colour) {
        this.name = name;
        this.colour = colour;
        this.score = 0;
        this.blunders = 0;
    }

    public String getName() {
        return this.name;
    }
    public Colour getColour() {
        return this.colour;
    }
    public int getScore() {
        return this.score;
    }
    public int getBlunders() {
        return this.blunders;
    }

    public void capture(Piece piece) {
        this.score += piece.getValue();
    }
    public void addBlunder() {
        this.blunders++;
    }

    //abstract methods players must implement
    public abstract Move turn(GameState state);
}
