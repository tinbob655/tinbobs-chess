import React from 'react';
import type {BoardState} from '../../../types/chessObjects';
import squareToIndex from '../../../functions/squareToIndex';
import {getPieceSymbol} from "../../../functions/getPieceSymbol.ts";

interface params {
    board: BoardState;
    selectedSquare: string | null;
    handleSquareClick: (square: string) => void;
    from: number,
    to: number,
    availableTargetLocations: number[];
}

export default function ChessBoard({ board, selectedSquare, handleSquareClick, from, to, availableTargetLocations }: params): React.ReactElement {


    return (
        <table id="board">
            <tbody>
                {renderBoard()}
            </tbody>
        </table>
    );


    //creates the board
    function renderBoard(): React.ReactElement[] {
        const rows: React.ReactElement[] = [];
    
        //7 down to start with white
        for (let row = 7; row >= 0; row--) {
            const cells: React.ReactElement[] = [];
    
            for (let col = 0; col < 8; col++) {
    
                const square = indexToSquare(col, row);
                const piece = board[squareToIndex(square)];
                const isLight = (row + col) % 2 !== 0;
                const isSelected = square === selectedSquare;
    
                //highlight all cells which the previous bot move involved
                const index = squareToIndex(square);
                const isFromOrTo = (index === from) || (index === to);

                //is this a valid target location
                const isTarget = availableTargetLocations.includes(index);
    
                cells.push(
                    <td className={`cell noVerticalSpacing ${(isSelected || isFromOrTo) ? 'highlighted' : ''} ${isLight ? 'light' : 'dark'}`} 
                    key={square} onClick={() => handleSquareClick(square)}>
                        <div className="chessCell">
                            {isTarget && (
                                <span
                                    className={`moveTargetDot ${piece ? 'occupied' : ''}`}
                                    aria-hidden="true"
                                />
                            )}
                             {piece && (
                                <span className={`noVerticalSpacing pieceWrapper ${piece.color === 'white' ? 'white' : 'black'}`}>
                                    {getPieceSymbol(piece)}
                                </span>
                            )}
                        </div>
    
                    </td>
                );
            }
            rows.push(<tr className="noVerticalSpacing" key={row}>{cells}</tr>);
        }
    
        return rows;
    }
}

function indexToSquare(col: number, row: number): string {
    return String.fromCharCode('a'.charCodeAt(0) + col) + (row + 1);
}