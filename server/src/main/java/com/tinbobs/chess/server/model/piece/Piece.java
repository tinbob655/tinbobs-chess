package com.tinbobs.chess.server.model.piece;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.state.Move;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

//when we send a piece to the frontend, it needs to know what type of piece it is
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Pawn.class,   name = "pawn"),
        @JsonSubTypes.Type(value = Rook.class,   name = "rook"),
        @JsonSubTypes.Type(value = Knight.class, name = "knight"),
        @JsonSubTypes.Type(value = Bishop.class, name = "bishop"),
        @JsonSubTypes.Type(value = Queen.class,  name = "queen"),
        @JsonSubTypes.Type(value = King.class,   name = "king"),
})

public abstract class Piece {

    private final Colour colour;

    public Piece(Colour colour) {
        this.colour = colour;
    }

    @JsonProperty("color")
    public Colour getColour() {
        return this.colour;
    }

    protected Set<Move> traverseAndGenerateMoves(int[][] directions, Position startingPos, Board board) {

        Set<Move> res = new HashSet<>();

        for (int[] direction : directions) {
            Position newPos = startingPos.add(direction[0], direction[1]);

            //keep going until we reach a boundary or another piece
            while (!newPos.outOfBounds() && board.getPieceAt(newPos).isEmpty()) {
                res.add(new Move(startingPos, newPos, this.colour.name()));
                newPos = newPos.add(direction[0], direction[1]);
            }

            //if at the end we collided with a piece of a different colour then add that as a pieceTaken move
            if (!newPos.outOfBounds()) {
                Position finalNewPos = newPos;
                board.getPieceAt(newPos).ifPresent(piece -> {
                    if (piece.getColour() != this.getColour()) {
                        res.add(new Move(startingPos, finalNewPos, this.colour.name()));
                    }
                });
            }
        }

        return res;
    }

    protected Set<Move> spiralAndGenerateMoves(int[][] directions, Position startingPos, Board board) {

        Set<Move> res = new HashSet<>();

        for (int[] direction : directions) {

            Position newPos = startingPos.add(direction[0], direction[1]);
            if (newPos.outOfBounds()) continue;

            Optional<Piece> opPiece = board.getPieceAt(newPos);
            if (opPiece.isEmpty() || opPiece.get().getColour() != this.colour) {
                res.add(new Move(startingPos, newPos, this.colour.name()));
            }
        }

        return res;
    }


    //abstract methods
    public abstract Set<Move> getLegalMoves(Position startingPos, Board board);
    public abstract int getValue();
    public abstract int[] getPieceTable();


    //equality
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Piece p)) {
            return false;
        }

        return p.getColour() == this.colour;
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.colour);
    }
}
