import React from 'react';
import type {Piece} from "../../../types/chessObjects";
import piecesToString from "../../../functions/piecesToString.ts";


interface params {
    material: number,
    capturedPieces: Piece[],
}

export default function PlayerInfo({material, capturedPieces}:params):React.ReactElement {

    return (
        <div className="playerInfoCard">
            <div className="playerInfoStat playerInfoStat--material">
                <span className="playerInfoLabel">Material</span>
                <span className="playerInfoValue">{material}</span>
            </div>
            <p className={"capturedPiecesText"}>
                Captured pieces: <br/> {piecesToString(capturedPieces)}
            </p>
        </div>
    );
}