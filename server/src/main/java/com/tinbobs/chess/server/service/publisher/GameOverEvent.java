package com.tinbobs.chess.server.service.publisher;

import com.tinbobs.chess.server.model.status.Status;

record GameOverEvent(Status status) implements GameEvent {}
