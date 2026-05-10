package model.state;

import model.board.Position;
import org.springframework.stereotype.Component;


@Component
public record Move(Position from, Position to, String playerName) {}
