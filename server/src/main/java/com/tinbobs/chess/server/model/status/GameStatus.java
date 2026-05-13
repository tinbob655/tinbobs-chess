package com.tinbobs.chess.server.model.status;

public record GameStatus(Status gameStatus, PlayerInfo human, PlayerInfo bot) {}
