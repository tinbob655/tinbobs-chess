package com.tinbobs.chess.server.service.publisher;


import com.tinbobs.chess.server.controller.DTO.MoveResult;
import com.tinbobs.chess.server.controller.DTO.TargetsResult;
import com.tinbobs.chess.server.controller.DTO.RawMove;
import com.tinbobs.chess.server.model.status.GameStatus;
import com.tinbobs.chess.server.model.status.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

//publisher will be a push observer for outbound information
@Service
public final class GameEventPublisher {

    //routes for sending information to the frontend
    private static final String TOPIC_GAME = "/topic/game";
    private static final String TOPIC_STATUS = "/topic/status";
    private static final String TOPIC_GAME_OVER = "/topic/gameOver";
    private static final String TOPIC_MOVE_RESULT = "/topic/moveResult";
    private static final String TOPIC_VALID_MOVE_TARGETS = "/topic/validMoveTargets";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    //we can publish a series of events without caring how using overloading
    public void publish(RawMove move) {
        messagingTemplate.convertAndSend(TOPIC_GAME, move);
    }
    public void publish(GameStatus status) {
        messagingTemplate.convertAndSend(TOPIC_STATUS, status);
    }
    public void publish(Status status) {
        messagingTemplate.convertAndSend(TOPIC_GAME_OVER, status);
    }
    public void publish(MoveResult moveResult) {
        messagingTemplate.convertAndSend(TOPIC_MOVE_RESULT, moveResult);
    }
    public void publish(TargetsResult res) {
        messagingTemplate.convertAndSend(TOPIC_VALID_MOVE_TARGETS, res);
    }
}