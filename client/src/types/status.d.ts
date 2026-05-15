import type {Piece} from "./chessObjects";

export interface status {
    gameStatus: gameStatus
    human: playerStats;
    bot: playerStats;
}

export interface playerStats {
    material: number;
    blunderCount: number;
    capturedPieces: Piece[];
}

export type gameStatus = 'CHECKMATE'|'CHECK'|'DRAW'|'STALEMATE'|'ONGOING';