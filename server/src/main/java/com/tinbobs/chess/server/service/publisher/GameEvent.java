package com.tinbobs.chess.server.service.publisher;

// Define the events
sealed interface GameEvent permits MoveEvent, StatusEvent, GameOverEvent {}
