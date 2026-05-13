package com.tinbobs.chess.server.model.state;

import com.tinbobs.chess.server.model.Factory;
import com.tinbobs.chess.server.model.board.Board;
import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.service.FakeAdvance;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class GameStateFactory implements Factory<GameState, Board, Player, List<Player>> {

    private final FakeAdvance fakeAdvance;

    public GameStateFactory(FakeAdvance fakeAdvance) {
        this.fakeAdvance = fakeAdvance;
    }

    public GameState create(Board a, Player b, List<Player> c) {
        return new GameState(a, b, fakeAdvance, c);
    }
}
