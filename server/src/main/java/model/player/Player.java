package model.player;

import model.board.Colour;
import model.state.GameState;
import model.state.Move;

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
