import type {Piece} from "../types/chessObjects";

export function getPieceSymbol(piece: Piece): string {
    const symbols: Record<string, Record<string, string>> = {
        white: {king: '♔', queen: '♕', rook: '♖', bishop: '♗', knight: '♘', pawn: '♙'},
        black: {king: '♚', queen: '♛', rook: '♜', bishop: '♝', knight: '♞', pawn: '♟'},
    };
    return symbols[piece.color.toLowerCase()][piece.type];
}