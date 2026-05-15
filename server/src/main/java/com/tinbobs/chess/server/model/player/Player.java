package com.tinbobs.chess.server.model.player;

import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.piece.Piece;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.state.Move;
import org.jspecify.annotations.NonNull;

import java.util.LinkedList;
import java.util.List;

public abstract class Player {

    private final String name;
    private final Colour colour;
    private final List<Piece> capturedPieces = new LinkedList<>();
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
    public List<Piece> getCapturedPieces() {
        return this.capturedPieces;
    }

    public void pieceTaken(Piece piece) {
        if (piece.getColour() != this.colour) {
            throw new IllegalArgumentException("Cannot loose material for the loss of an opponent's piece");
        }

        this.material -= piece.getValue();
    }
    public void takePiece(Piece piece) {
        if (piece.getColour() == this.colour) {
            throw new IllegalArgumentException("Cannot take own piece");
        }

        this.capturedPieces.add(piece);
    }
    public void addBlunder() {
        this.blunders++;
    }

    public void reset() {
        this.material = 39;
        this.blunders = 0;
        this.capturedPieces.clear();
    }

    //abstract methods players must implement
    @NonNull
    public abstract Move turn(GameState state);
}
