import squareToIndex from "./squareToIndex";

import type { BoardState, Piece } from "../types/chessObjects";
import type Move from "../types/move";


export default function applyMove(board:BoardState, move:Move):BoardState {

    const newBoard = Array.from(board);
    const from:number = squareToIndex(move.from);
    const to:number = squareToIndex(move.to);

    const pieceAtFrom:Piece|null = newBoard[from];

    newBoard[from] = null;
    newBoard[to] = pieceAtFrom;

    return newBoard;
}