import React, { useState, useEffect} from 'react';
import { useChessSocket } from '../../../hooks/useChessSocket';
import ChessBoard from './chessBoard';
import PlayerInfo from './playerInfo';

import applyMove from '../../../functions/applyMove';
import createDefaultBoard from '../../../functions/createDefaultBoard';

import type { BoardState } from '../../../types/chessObjects';
import type socketInfo from '../../../types/socketInfo';
import type Move from '../../../types/move';
import Thinking from './thinking';
import squareToIndex from '../../../functions/squareToIndex';


export default function Home():React.ReactElement {

    const {connected, botMove, sendMove, startGame, status, waitingForBotMove, getValidMoves}:socketInfo = useChessSocket();

    const [board, setBoard] = useState<BoardState>(() => createDefaultBoard());
    const [selectedSquare, setSelectedSquare] = useState<string|null>(null);
    const [invalidMoveMessage, setInvalidMoveMessage] = useState<string>('');
    const [validMoveTargets, setValidMoveTargets] = useState<number[]>([]);

    //when we are connected, start the game
    useEffect(() => {

        if (connected) {
            startGame();
        }
    }, [connected, startGame]);

    //when a bot moves, apply the move
    useEffect(() => {

        if (!botMove) return;

        //queue updates rather than doing them straight away
        const stateQueue = () => {
            setTimeout(() => {
                setBoard(prev => applyMove(prev, botMove));
            }, 0);
        };
        stateQueue();
    }, [botMove]);


    return (
        <React.Fragment>
            <div id="chessBoardWrapper">

                {connected ? (
                    <React.Fragment>
                        <div id="gameWrapper">
                            
                            {/*player info*/}
                            <div id="playerInfoWrapper">
                                <h2>
                                    Players:
                                </h2>
                                <div className="dividerLine"></div>
                                <table className="playerInfoTable">
                                    <thead>
                                        <tr>
                                            <td>

                                                {/*human player*/}
                                                <p>
                                                    You:
                                                </p>
                                                <PlayerInfo material={status.human.material || 39} blunderCount={status.human.blunderCount || 0} />
                                            </td>
                                            <td>

                                                {/*bot player*/}
                                                <p>
                                                    Bot:
                                                </p>
                                                <PlayerInfo material={status.bot.material || 39} blunderCount={status.bot.blunderCount || 0} />
                                            </td>
                                        </tr>
                                    </thead>
                                </table>
                            </div>

                            <div id="chessBoardWrapper">

                                {/*tells the user when the bot is thinking*/}
                                <Thinking botThinking={waitingForBotMove}/>

                                {/*CHESS BOARD*/}
                                <ChessBoard board={board} handleSquareClick={(square:string) => squareClicked(square)} selectedSquare={selectedSquare} from={botMove ? squareToIndex(botMove.from) : -1} to={botMove ? (squareToIndex(botMove.to)) : -1} availableTargetLocations={validMoveTargets} />
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

            //reset UI selection indicators
            setSelectedSquare(null);
            setValidMoveTargets([]);
    
            //attempt to run the move on the backend
            sendMove(move.from, move.to)

                //backend was happy with the move
                .then(() => {
                    setBoard(prev => applyMove(prev, move));
                })

                //backend rejected the move
                .catch((reason: string) => {
                    console.warn('Move rejected:', reason);

                    //it might not even be our turn
                    if (waitingForBotMove) {
                        setInvalidMoveMessage("Not your turn yet!");
                    }
                    else {
                        setInvalidMoveMessage("Invalid move.");
                    }
                });
    
        }

        //the user has selected this square as the first part of their move
        else {
            setSelectedSquare(square);

            //show the user all the valid moves
            getValidMoves(squareToIndex(square))

                .then((locations: number[]) => {
                    setValidMoveTargets(locations);
                })

                //for some reason we couldn't get that piece's moves
                .catch((reason: string) => {
                    setInvalidMoveMessage(reason);
                })
        }
    }
};