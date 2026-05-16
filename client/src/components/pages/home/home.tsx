import React, { useState, useEffect} from 'react';
import { useChessSocket } from '../../../hooks/useChessSocket';
import ChessBoard from './chessBoard';
import PlayerInfo from './playerInfo';
import Thinking from './thinking';

import squareToIndex from '../../../functions/squareToIndex';
import applyMove from '../../../functions/applyMove';
import createDefaultBoard from '../../../functions/createDefaultBoard';

import type { BoardState } from '../../../types/chessObjects';
import type socketInfo from '../../../types/socketInfo';
import type Move from '../../../types/move';
import type {gameStatus} from "../../../types/status";


export default function Home():React.ReactElement {

    const {connected, botMove, sendMove, startGame, status, waitingForBotMove, getValidMoves, gameOverState}:socketInfo = useChessSocket();

    const [board, setBoard] = useState<BoardState>(() => createDefaultBoard());
    const [selectedSquare, setSelectedSquare] = useState<string|null>(null);
    const [invalidMoveMessage, setInvalidMoveMessage] = useState<string>('');
    const [validMoveTargets, setValidMoveTargets] = useState<number[]>([]);
    const [gameOverMessage, setGameOverMessage] = useState<string>('');

    const statusMessage = getStatusMessage(status.gameStatus, waitingForBotMove)

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

    //when the game is over create a message
    useEffect(() => {

        if (!gameOverState) return;

        const messageUpdater = () => {
            setTimeout(() => {
                switch (gameOverState) {

                    //shouldn't ever happen really
                    case "ONGOING": setGameOverMessage(''); break;
                    case "CHECK": setGameOverMessage(''); break;

                    //actual game ending states
                    case "DRAW": setGameOverMessage("The game ended in a DRAW!"); break;
                    case "STALEMATE": setGameOverMessage("The game ended in a STALEMATE!"); break;
                    case "CHECKMATE": setGameOverMessage(waitingForBotMove ? "You won!" : "The bot won!"); break;
                }
            }, 0);
        };

        messageUpdater();
    }, [gameOverState, waitingForBotMove]);


    return (
        <React.Fragment>
            <div id="mainWrapper">

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
                                                <PlayerInfo
                                                    material={status.human.material || 39}
                                                    blunderCount={status.human.blunderCount || 0}
                                                    capturedPieces={status.human.capturedPieces || []}
                                                />
                                            </td>
                                            <td>

                                                {/*bot player*/}
                                                <p>
                                                    Bot:
                                                </p>
                                                <PlayerInfo
                                                    material={status.bot.material || 39}
                                                    blunderCount={status.bot.blunderCount || 0}
                                                    capturedPieces={status.bot.capturedPieces || []}
                                                />
                                            </td>
                                        </tr>
                                    </thead>
                                </table>
                            </div>

                            <div id="chessBoardWrapper">
                                {gameOverState ? (
                                    <React.Fragment>

                                        {/*game is over*/}
                                        <h2>
                                            GAME OVER!
                                        </h2>
                                        <p>
                                            {gameOverMessage}
                                        </p>
                                    </React.Fragment>
                                ) : (
                                    <React.Fragment>

                                        {/*thinking...*/}
                                        <Thinking botThinking={waitingForBotMove}/>
                                    </React.Fragment>
                                )}

                                {/*CHESS BOARD*/}
                                <ChessBoard board={board} handleSquareClick={(square:string) => squareClicked(square)} selectedSquare={selectedSquare} from={botMove ? squareToIndex(botMove.from) : -1} to={botMove ? (squareToIndex(botMove.to)) : -1} availableTargetLocations={validMoveTargets} />
                            </div>

                            {/*invalid move message*/}
                            <div id="gameMessageWrapper">
                                <p style={{height: '1lh'}}>
                                    {invalidMoveMessage}
                                </p>
                                <p style={{height: '1lh'}}>
                                    {statusMessage}
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

function getStatusMessage(gameStatus: gameStatus, waitingForBotMove: boolean): string {
    switch (gameStatus) {
        case 'CHECK': return !waitingForBotMove ? "You put the bot's king in check!" : "The bot has put your king in check!";
        default: return '';
    }
}