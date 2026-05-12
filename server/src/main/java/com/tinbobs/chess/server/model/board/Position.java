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

        if (this.x < 'a' || this.x > 'h' || this.y < 1 || this.y > 8) {
            throw new IllegalStateException("Position " + this + " is out of bounds for a standard chessboard.");
        }

        int xInt = this.x - 'a';
        return xInt + ((this.y - 1) * 8);
    }

    //we can traverse the board using number or positions
    public Position add(int dx, int dy) {
        char newX = (char) (this.x + dx);
        int newY = this.y + dy;
        return new Position(newX, newY);
    }
    public Position add(Position offset) {
        int dx = offset.x() - 'a';
        int dy = offset.y();
        return this.add(dx, dy);
    }

    public boolean outOfBounds() {

        //x
        if (this.x < 'a' || this.x > 'h') {
            return true;
        }

        //y
        return this.y < 1 || this.y > 8;
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
