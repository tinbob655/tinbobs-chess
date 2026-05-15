package com.tinbobs.chess.server.model.status;

import com.tinbobs.chess.server.model.piece.Piece;

import java.util.List;

public record PlayerInfo(int material, int blunderCount, List<Piece> capturedPieces) {}
