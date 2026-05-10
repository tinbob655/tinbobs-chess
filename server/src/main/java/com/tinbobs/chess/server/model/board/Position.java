package com.tinbobs.chess.server.model.board;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public record Position(char x, int y) {

    @Override
    @NonNull
    public String toString() {
        return String.valueOf(x) + y;
    }

    public int toArrayIndex() {
        return ((int) this.x * 8) + this.y;
    }


    //equality
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Position pos)) {
            return false;
        }

        return pos.toArrayIndex() == this.toArrayIndex();
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }
}
