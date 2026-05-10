package com.tinbobs.chess.server.model.piece;

import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.state.Move;

import java.util.Objects;
import java.util.Set;

public abstract class Piece {

    private final Colour colour;
    private final Position position;

    public Piece(Colour colour, Position pos) {
        this.colour = colour;
        this.position = pos;
    }

    public Colour getColour() {
        return this.colour;
    }
    public Position getPosition() {
        return this.position;
    }


    //abstract methods
    public abstract Set<Move> getLegalMoves();
    public abstract int getValue();


    //equality
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Piece p)) {
            return false;
        }

        return (
                (p.getColour() == this.colour)
                && (p.position.equals(this.position))
                );
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.colour, this.position);
    }
}
