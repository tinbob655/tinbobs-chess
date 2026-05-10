package com.tinbobs.chess.server.model.board;

import com.tinbobs.chess.server.model.piece.Piece;
import org.jspecify.annotations.NonNull;

import java.util.*;

public final class Board {

    private final List<Optional<Piece>> grid = new ArrayList<>(Collections.nCopies(64, Optional.empty()));

    public int size() {
        return this.grid.size();
    }
    public List<Optional<Piece>> getGrid() {
        return this.grid;
    }
    public Optional<Piece> getPieceAt(@NonNull Position pos) {
        return this.grid.get(pos.toArrayIndex());
    }
    public void setPieceAt(@NonNull Position pos, Piece p) {
        this.grid.set(pos.toArrayIndex(), Optional.of(p));
    }
    public void removePieceAt(@NonNull Position pos) {
        this.grid.set(pos.toArrayIndex(), Optional.empty());
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Board b)) {
            return false;
        }

        if (b.size() != this.size()) {
            return false;
        }

        return this.grid.equals(b.getGrid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.grid);
    }
}
