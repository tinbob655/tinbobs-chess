package com.tinbobs.chess.server.controller.DTO;

public record MoveResult(boolean valid, String correlationID, String reason) {
}
