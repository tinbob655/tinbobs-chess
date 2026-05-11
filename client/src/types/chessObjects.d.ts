export type PieceType = 'pawn' | 'rook' | 'knight' | 'bishop' | 'queen' | 'king';

export type PieceColour = 'white' | 'black';

export interface Piece {
    type: PieceType;
    color: PieceColour;
}

export type BoardState = (Piece | null)[];