package com.tinbobs.chess.server.model.state;

import com.tinbobs.chess.server.model.board.Position;


public record Move(Position from, Position to, String playerName) {}
