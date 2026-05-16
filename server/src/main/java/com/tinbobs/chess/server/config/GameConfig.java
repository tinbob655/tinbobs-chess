package com.tinbobs.chess.server.config;

import com.tinbobs.chess.server.engine.GameEngine;
import com.tinbobs.chess.server.model.piece.Colour;
import com.tinbobs.chess.server.model.player.Bot;
import com.tinbobs.chess.server.model.player.Human;
import com.tinbobs.chess.server.service.blunderDetector.BlunderDetector;
import com.tinbobs.chess.server.service.evaluator.Evaluator;
import com.tinbobs.chess.server.service.parser.MoveParser;
import com.tinbobs.chess.server.service.publisher.GameEventPublisher;
import com.tinbobs.chess.server.service.statusCreator.CreateFrontendStatus;
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
    public GameEngine gameEngine(Human human, Bot bot, MoveParser moveParser, CreateFrontendStatus statusCreator, BlunderDetector blunderDetector, GameEventPublisher publisher, Evaluator evaluator) {
        GameEngine engine = new GameEngine(moveParser, statusCreator, blunderDetector, publisher, evaluator);
        engine.addPlayer(human);
        engine.addPlayer(bot);
        return engine;
    }
}
