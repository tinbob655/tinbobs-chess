import React, {useRef, useState, useEffect} from 'react';
import { useChessSocket } from '../../hooks/useChessSocket';
import ChessBoard from './chessBoard';

import applyMove from '../../functions/applyMove';
import createDefaultBoard from '../../functions/createDefaultBoard';

import type { BoardState } from '../../types/chessObjects';
import type socketInfo from '../../types/socketInfo';
import type Move from '../../types/move';


export default function Home():React.ReactElement {

    const {connected, botMove, sendMove, startGame}:socketInfo = useChessSocket();

    const playNowWrapper = useRef<HTMLDivElement>(null);

    const [board, setBoard] = useState<BoardState>([]);
    const [selectedSquare, setSelectedSquare] = useState<string|null>(null);
    const [invalidMoveMessage, setInvalidMoveMessage] = useState<string>('');


    //create a default board on page load
    useEffect(() => {
        setBoard(createDefaultBoard());
    }, []);

    //when a bot moves, apply the move
    useEffect(() => {

        if (!botMove) return;

        const newBoard = applyMove(board, botMove);
        setBoard(newBoard);
    }, [botMove]);


    return (
        <React.Fragment>
            <div id="playNowWrapper" ref={playNowWrapper}>
                <button id="playNowButton" className={connected ? '' : "greyed"} onClick={playButtonClicked}>
                    <h3 id="playNowText" className={`noVerticalSpacing ${connected ? '' : "greyed"}`} style={{transform: 'unset'}}>
                        Play some chess!
                    </h3>
                </button>
            </div>

            <div id="chessBoardWrapper">
                {connected ? (
                    <React.Fragment>
                        <p>
                            {invalidMoveMessage}
                        </p>

                        {/*CHESS BOARD*/}
                        <ChessBoard board={board} handleSquareClick={(square:string) => {squareClicked(square)}} selectedSquare={selectedSquare} />
                    </React.Fragment>
                ) : (
                    <React.Fragment>
                        
                        {/*will display if not connected to backend*/}
                        <p className="code">
                            Awaiting connection from engine...
                        </p>
                    </React.Fragment>
                )}
            </div>

            <div className="dividerLine"></div>

            <div>
                <h2 className="alignRight">
                    Information
                </h2>
                <p className="alignRight">
                    Tinbob's Chess is a full-stack chess application with a React/TypeScript frontend and a Java Spring Boot backend. Moves are transmitted in real time over a STOMP WebSocket connection, keeping the client and server in sync without any polling. The game engine runs server-side, handling move validation, turn management, and bot logic, while the frontend focuses purely on rendering state and dispatching player input.
                </p>
            </div>
        </React.Fragment>
    );


    //fires when the user clicks the play button
    async function playButtonClicked():Promise<void> {

        if (!connected) return;

        //hide the play button and the connected message
        playNowWrapper.current?.classList.add('hidden');

        //start the game
        startGame();
    }

    //fires when the user clicks a square in the chess board
    async function squareClicked(square: string): Promise<void> {

        setInvalidMoveMessage('');

        //the user wants to use the clicked square to make a move
        if (selectedSquare) {

            const move: Move = { from: selectedSquare, to: square, playerName: '' };
            setSelectedSquare(null);
    
            //attempt to run the move on the backend
            sendMove(move.from, move.to)

                //backend was happy with the move
                .then(() => {
                    setBoard(prev => applyMove(prev, move));
                })

                //backend rejected the move
                .catch((reason: string) => {
                    console.warn('Move rejected:', reason);
                    setInvalidMoveMessage("Invalid move!");
                });
    
        }

        //the user has selected this square as the first part of their move
        else {
            setSelectedSquare(square);
        }
    }
};