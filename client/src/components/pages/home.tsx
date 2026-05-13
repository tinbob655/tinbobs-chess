import React, { useState, useEffect} from 'react';
import { useChessSocket } from '../../hooks/useChessSocket';
import ChessBoard from './chessBoard';
import PlayerInfo from './playerInfo';

import applyMove from '../../functions/applyMove';
import createDefaultBoard from '../../functions/createDefaultBoard';

import type { BoardState } from '../../types/chessObjects';
import type socketInfo from '../../types/socketInfo';
import type Move from '../../types/move';


export default function Home():React.ReactElement {

    const {connected, botMove, sendMove, startGame, status}:socketInfo = useChessSocket();

    const [board, setBoard] = useState<BoardState>([]);
    const [selectedSquare, setSelectedSquare] = useState<string|null>(null);
    const [invalidMoveMessage, setInvalidMoveMessage] = useState<string>('');

    //create a default board on page load
    useEffect(() => {

        setBoard(createDefaultBoard());
    }, []);

    //when we are connected, start the game
    useEffect(() => {

        if (connected) {
            startGame();
        }
    }, [connected]);

    //when a bot moves, apply the move
    useEffect(() => {

        if (!botMove) return;

        setBoard(prev => applyMove(prev, botMove));
    }, [botMove]);


    return (
        <React.Fragment>
            <div id="chessBoardWrapper">

                {connected ? (
                    <React.Fragment>
                        <div id="gameWrapper">
                            
                            {/*player info*/}
                            <div id="playerInfoWrapper">
                                <p>
                                    Players:
                                </p>
                                <table>
                                    <thead>
                                        <tr>
                                            <td>

                                                {/*human player*/}
                                                <p>
                                                    You:
                                                </p>
                                                <PlayerInfo material={status.human.material || 0} blunderCount={status.human.blunderCount || 0} />
                                            </td>
                                            <td>

                                                {/*bot player*/}
                                                <p>
                                                    Bot:
                                                </p>
                                                <PlayerInfo material={status.bot.material || 0} blunderCount={status.bot.blunderCount || 0} />
                                            </td>
                                        </tr>
                                    </thead>
                                </table>
                            </div>

                            {/*CHESS BOARD*/}
                            <div id="chessBoardWrapper">
                                <ChessBoard board={board} handleSquareClick={(square:string) => {squareClicked(square)}} selectedSquare={selectedSquare} />
                            </div>

                            {/*invalid move message*/}
                            <div id="invalidMoveMessageWrapper">
                                <p style={{height: '20px'}}>
                                    {invalidMoveMessage}
                                </p>
                            </div>
                        </div>

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

    //fires when the user clicks a square in the chess board
    async function squareClicked(square: string): Promise<void> {

        setInvalidMoveMessage('');

        //the user wants to use the clicked square to make a move
        if (selectedSquare) {

            const move: Move = { from: selectedSquare, to: square, playerName: '', correlationID: '' };
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