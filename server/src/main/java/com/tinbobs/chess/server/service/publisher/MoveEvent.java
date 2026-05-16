package com.tinbobs.chess.server.service.publisher;

import com.tinbobs.chess.server.model.state.RawMove;

record MoveEvent(RawMove move) implements GameEvent {}
