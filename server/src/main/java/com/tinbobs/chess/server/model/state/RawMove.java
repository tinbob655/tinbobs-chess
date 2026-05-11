package com.tinbobs.chess.server.model.state;

public record RawMove(String from, String to, String playerName, String correlationID) {
}
