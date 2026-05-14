import {useEffect, useRef, useState} from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

import type socketInfo from '../types/socketInfo';
import type Move from '../types/move';
import type { status } from '../types/status';


interface MoveResult {
    valid: boolean;
    correlationID: string;
    reason: string | null;
};

export function useChessSocket():socketInfo {


    const clientRef = useRef<Client|null>(null);
    const pendingMoves = useRef<Map<string, { resolve: () => void; reject: (reason: string) => void }>>(new Map());

    const [waitingForBotMove, setWaitingForBotMove] = useState<boolean>(false);
    const [botMove, setBotMove] = useState<Move|null>(null);
    const [connected, setConnected] = useState(false);
    const [status, setStatus] = useState<status>({
        gameStatus: 'ONGOING',
        human: {
            material: 0,
            blunderCount: 0,
        },
        bot: {
            material: 0,
            blunderCount: 0,
        },
    });



    //creates a STOMP client
    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),

            onConnect: () => {
                setConnected(true);

                //listen for bot moves
                client.subscribe('/topic/game', (message) => {
                    const move: Move = JSON.parse(message.body);

                    //only for bot moves
                    if (move.playerName !== 'Player') {
                        setWaitingForBotMove(false);
                        setBotMove(move);
                    }
                });

                //listen for the result of backend move validation
                client.subscribe('/topic/moveResult', (message) => {

                    const result: MoveResult = JSON.parse(message.body);
                    const pending = pendingMoves.current.get(result.correlationID);

                    if (pending) {
                        if (result.valid) {

                            //the move we submitted was valid
                            setWaitingForBotMove(true);
                            pending.resolve();
                        }
                        else {

                            //the move we submitted was invalid
                            pending.reject(result.reason ?? 'Invalid move');
                        }
                        pendingMoves.current.delete(result.correlationID);
                    }
                });

                //every time a move is done we will receive a game status
                client.subscribe('/topic/status', (message) => {

                    const gameStatus:status = JSON.parse(message.body);
                    setStatus(gameStatus);
                })
            },

            onDisconnect: () => setConnected(false),
        });

        client.activate();
        clientRef.current = client;
        return () => {
            client.deactivate();
        };

    }, []);


    //other components will call this to send a human move to the backend
    function sendMove(from: string, to: string): Promise<void> {
        return new Promise<void>((resolve, reject) => {

            const correlationId = crypto.randomUUID();
            pendingMoves.current.set(correlationId, { resolve, reject });

            const move:Move = {
                from: from,
                to: to,
                playerName: "Player",
                correlationID: correlationId,
            };

            clientRef.current?.publish({
                destination: '/app/move',
                body: JSON.stringify(move),
            });
        });
    }

    //start the game
    function startGame():void {
        clientRef.current?.publish({
            destination: '/app/start',
            body: '',
        });
    }

    //when we refresh the front we need to tell the back to restart the game
    window.onbeforeunload = ((event:BeforeUnloadEvent) => {
        event.preventDefault();
        
        clientRef.current?.publish({
            destination: "/app/refresh",
            body: '',
        });
    });

    return { connected, botMove, sendMove, startGame, status, waitingForBotMove };
}