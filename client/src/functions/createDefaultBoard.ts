import squareToIndex from "./squareToIndex";

import type { BoardState } from "../types/chessObjects";
import type { Piece } from "../types/chessObjects";

export default function createDefaultBoard():BoardState {
    const board: BoardState = Array(64).fill(null);

    function place(square: string, piece: Piece) {
        board[squareToIndex(square)] = piece;
    }

    //white pieces
    place('a1', { type: 'rook',   color: 'white' });
    place('b1', { type: 'knight', color: 'white' });
    place('c1', { type: 'bishop', color: 'white' });
    place('d1', { type: 'queen',  color: 'white' });
    place('e1', { type: 'king',   color: 'white' });
    place('f1', { type: 'bishop', color: 'white' });
    place('g1', { type: 'knight', color: 'white' });
    place('h1', { type: 'rook',   color: 'white' });

    //white pawns
    for (let col = 0; col < 8; col++) {
        const square = String.fromCharCode('a'.charCodeAt(0) + col) + '2';
        place(square, { type: 'pawn', color: 'white' });
    }

    //black pieces
    place('a8', { type: 'rook',   color: 'black' });
    place('b8', { type: 'knight', color: 'black' });
    place('c8', { type: 'bishop', color: 'black' });
    place('d8', { type: 'queen',  color: 'black' });
    place('e8', { type: 'king',   color: 'black' });
    place('f8', { type: 'bishop', color: 'black' });
    place('g8', { type: 'knight', color: 'black' });
    place('h8', { type: 'rook',   color: 'black' });

    //black pawns
    for (let col = 0; col < 8; col++) {
        const square = String.fromCharCode('a'.charCodeAt(0) + col) + '7';
        place(square, { type: 'pawn', color: 'black' });
    }

    return board;
}