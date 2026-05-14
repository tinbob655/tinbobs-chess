package com.tinbobs.chess.server.controller.records;

public record MoveResult(boolean valid, String correlationID, String reason) {
}
