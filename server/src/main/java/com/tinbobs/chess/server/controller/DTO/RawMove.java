package com.tinbobs.chess.server.controller.DTO;

public record RawMove(String from, String to, String playerName, String correlationID) {
}
