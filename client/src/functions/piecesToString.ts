import type {Piece} from "../types/chessObjects";
import {getPieceSymbol} from "./getPieceSymbol.ts";

export default function piecesToString(pieces: Piece[]):string {

    if (!pieces || pieces.length < 1) {
        return '';
    }

    let res:string = '';

    pieces.forEach((piece) => {
        res += getPieceSymbol(piece);
    })

    return res;
}