package com.tinbobs.chess.server.config;

import com.tinbobs.chess.server.engine.GameEngine;
import com.tinbobs.chess.server.model.board.Colour;
import com.tinbobs.chess.server.model.player.Bot;
import com.tinbobs.chess.server.model.player.Human;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfig {

    @Bean
    public Human humanPlayer() {
        return new Human("Player", Colour.WHITE);
    }

    @Bean
    public Bot botPlayer() {
        return new Bot("Bot", Colour.BLACK);
    }

    @Bean
    public GameEngine gameEngine(Human human, Bot bot) {
        GameEngine engine = new GameEngine();
        engine.addPlayer(human);
        engine.addPlayer(bot);
        return engine;
    }
}
