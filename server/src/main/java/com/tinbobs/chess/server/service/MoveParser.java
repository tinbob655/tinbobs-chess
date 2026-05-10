package com.tinbobs.chess.server.service;


import com.tinbobs.chess.server.model.board.Position;
import com.tinbobs.chess.server.model.state.Move;
import com.tinbobs.chess.server.model.state.RawMove;
import org.springframework.stereotype.Service;

@Service
public class MoveParser {

    //"e2" -> Position('e', 2)
    public Move toMove(RawMove raw) {

        String from = raw.from();
        String to = raw.to();

        Position fromPos = new Position(from.charAt(0), from.charAt(1));
        Position toPos = new Position(to.charAt(0), to.charAt(1));

        return new Move(fromPos, toPos, raw.playerName());
    }

    //Position('e', 2) -> "e2"
    public RawMove toRaw(Move move) {

        Position from = move.from();
        Position to = move.to();

        String fromStr = from.toString();
        String toStr = to.toString();

        return new RawMove(fromStr, toStr, move.playerName());
    }
}
