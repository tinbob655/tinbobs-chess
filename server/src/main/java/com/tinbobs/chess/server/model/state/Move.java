package com.tinbobs.chess.server.model.state;

import com.tinbobs.chess.server.model.board.Position;
import org.jspecify.annotations.NonNull;

import java.util.Objects;


public record Move(Position from, Position to, String playerName) {

    //equality
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Move m)) {
            return false;
        }

        return (
                (m.from().equals(this.from))
                && (m.to().equals(this.to))
                );
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.from, this.to);
    }

    @Override
    @NonNull
    public String toString() {
        return this.from.toString() +
                "->" +
                this.to.toString();
    }
}
