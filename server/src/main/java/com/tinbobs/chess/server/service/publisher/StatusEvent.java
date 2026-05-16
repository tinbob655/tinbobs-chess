package com.tinbobs.chess.server.service.publisher;

import com.tinbobs.chess.server.model.status.GameStatus;

record StatusEvent(GameStatus status) implements GameEvent {}
