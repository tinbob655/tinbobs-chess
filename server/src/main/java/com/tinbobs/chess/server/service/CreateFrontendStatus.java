package com.tinbobs.chess.server.service;


import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.player.Player;
import com.tinbobs.chess.server.model.state.GameState;
import com.tinbobs.chess.server.model.status.GameStatus;
import com.tinbobs.chess.server.model.status.PlayerInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public final class CreateFrontendStatus {

    public GameStatus createFrontendStatus(List<Player> players, GameState state) {

        //organise our players into something more usable
        Map<Colour, PlayerInfo> infoByColour = players.stream()
                .collect(Collectors.toMap(
                        Player::getColour,
                        p -> new PlayerInfo(p.getMaterial(), p.getBlunders())
                ));

        //make sure we have a human and a bot
        PlayerInfo human = infoByColour.get(Colour.WHITE);
        PlayerInfo bot = infoByColour.get(Colour.BLACK);
        if (human == null || bot == null) {
            throw new IllegalStateException("Expected one WHITE and one BLACK player");
        }

        //create and return out status
        return new GameStatus(state.getStatus(), human, bot);
    }
}
