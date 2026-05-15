import type {Piece} from "./chessObjects";

export interface status {
    gameStatus: 'CHECKMATE'|'CHECK'|'DRAW'|'STALEMATE'|'ONGOING';
    human: playerStats;
    bot: playerStats;
}

export interface playerStats {
    material: number;
    blunderCount: number;
    capturedPieces: Piece[];
}