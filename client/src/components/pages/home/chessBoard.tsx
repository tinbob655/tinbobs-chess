import React from 'react';
import type { BoardState, Piece } from '../../../types/chessObjects';
import squareToIndex from '../../../functions/squareToIndex';

interface params {
    board: BoardState;
    selectedSquare: string | null;
    handleSquareClick: (square: string) => void;
}

export default function ChessBoard({ board, selectedSquare, handleSquareClick }: params): React.ReactElement {
    return (
        <table id="board">
            <tbody>
                {renderBoard(board, selectedSquare, handleSquareClick)}
            </tbody>
        </table>
    );
}

function renderBoard(board: BoardState, selectedSquare: string | null, handleSquareClick: (square: string) => void): React.ReactElement[] {
    const rows: React.ReactElement[] = [];

    //7 down to start with white
    for (let row = 7; row >= 0; row--) {
        const cells: React.ReactElement[] = [];

        for (let col = 0; col < 8; col++) {

            const square = indexToSquare(col, row);
            const piece = board[squareToIndex(square)];
            const isLight = (row + col) % 2 !== 0;
            const isSelected = square === selectedSquare;

            cells.push(
                <td className={`cell noVerticalSpacing ${isSelected ? 'highlighted' : ''} ${isLight ? 'light' : 'dark'}`} 
                key={square} onClick={() => handleSquareClick(square)}>
                    <div className="chessCell">
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

function indexToSquare(col: number, row: number): string {
    return String.fromCharCode('a'.charCodeAt(0) + col) + (row + 1);
}

function getPieceSymbol(piece: Piece): string {
    const symbols: Record<string, Record<string, string>> = {
        white: { king: '♔', queen: '♕', rook: '♖', bishop: '♗', knight: '♘', pawn: '♙' },
        black: { king: '♚', queen: '♛', rook: '♜', bishop: '♝', knight: '♞', pawn: '♟' },
    };
    return symbols[piece.color][piece.type];
}