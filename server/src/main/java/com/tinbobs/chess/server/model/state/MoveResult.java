package com.tinbobs.chess.server.model.state;

public record MoveResult(boolean valid, String correlationID, String reason) {
}
