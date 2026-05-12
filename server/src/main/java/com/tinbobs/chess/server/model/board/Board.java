package com.tinbobs.chess.server.model.board;

import com.tinbobs.chess.server.model.piece.*;
import org.jspecify.annotations.NonNull;

import java.util.*;

public final class Board {

    private List<Optional<Piece>> grid = new ArrayList<>(Collections.nCopies(64, Optional.empty()));


    //create a board with default chess pieces
    public Board() {

        //need to place pieces on the boar
        //white pieces
        grid.set(0, Optional.of(new Rook(Colour.WHITE)));
        grid.set(1, Optional.of(new Knight(Colour.WHITE)));
        grid.set(2, Optional.of(new Bishop(Colour.WHITE)));
        grid.set(3, Optional.of(new King(Colour.WHITE)));
        grid.set(4, Optional.of(new Queen(Colour.WHITE)));
        grid.set(5, Optional.of(new Bishop(Colour.WHITE)));
        grid.set(6, Optional.of(new Knight(Colour.WHITE)));
        grid.set(7, Optional.of(new Rook(Colour.WHITE)));

        //white pawns
        for (int cell = 8; cell < 16; cell++) {
            grid.set(cell, Optional.of(new Pawn(Colour.WHITE)));
        }

        //black pieces
        grid.set(56, Optional.of(new Rook(Colour.BLACK)));
        grid.set(57, Optional.of(new Knight(Colour.BLACK)));
        grid.set(58, Optional.of(new Bishop(Colour.BLACK)));
        grid.set(59, Optional.of(new King(Colour.BLACK)));
        grid.set(60, Optional.of(new Queen(Colour.BLACK)));
        grid.set(61, Optional.of(new Bishop(Colour.BLACK)));
        grid.set(62, Optional.of(new Knight(Colour.BLACK)));
        grid.set(63, Optional.of(new Rook(Colour.BLACK)));

        //black pawns
        for (int cell = 48; cell < 56; cell++) {
            grid.set(cell, Optional.of(new Pawn(Colour.BLACK)));
        }
    };

    //can also create a predefined board
    public Board(List<Optional<Piece>> startingGrid) {
        this.grid = startingGrid;
    }

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
