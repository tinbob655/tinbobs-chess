package com.tinbobs.chess.server.service.parser;


import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.controller.DTO.RawMove;
import org.springframework.stereotype.Service;

@Service
public final class MoveParser implements TwoWayParser<RawMove, Move> {

    //"e2" -> Position('e', 2)
    public Move decode(RawMove raw) {

        String from = raw.from();
        String to = raw.to();

        Position fromPos = new Position(from.charAt(0), Character.getNumericValue(from.charAt(1)));
        Position toPos = new Position(to.charAt(0), Character.getNumericValue(to.charAt(1)));

        return new Move(fromPos, toPos, raw.playerName());
    }

    //Position('e', 2) -> "e2"
    public RawMove encode(Move move) {

        Position from = move.from();
        Position to = move.to();

        String fromStr = from.toString();
        String toStr = to.toString();

        return new RawMove(fromStr, toStr, move.playerName(), null);
    }
}
