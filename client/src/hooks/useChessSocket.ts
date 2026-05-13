import {useEffect, useRef, useState} from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

import type socketInfo from '../types/socketInfo';
import type Move from '../types/move';


interface MoveResult {
    valid: boolean;
    correlationID: string;
    reason: string | null;
};

export function useChessSocket():socketInfo {


    const clientRef = useRef<Client|null>(null);
    const pendingMoves = useRef<Map<string, { resolve: () => void; reject: (reason: string) => void }>>(new Map());

    const [botMove, setBotMove] = useState<Move|null>(null);
    const [connected, setConnected] = useState(false);



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
                        setBotMove(move);
                    }
                });

                //listen for the result of backend move validation
                client.subscribe('/topic/moveResult', (message) => {

                    const result: MoveResult = JSON.parse(message.body);
                    const pending = pendingMoves.current.get(result.correlationID);
                    console.log(pending);

                    if (pending) {
                        if (result.valid) {

                            //the move we submitted was valid
                            pending.resolve();
                        }
                        else {

                            //the move we submitted was invalid
                            pending.reject(result.reason ?? 'Invalid move');
                        }
                        pendingMoves.current.delete(result.correlationID);
                    }
                });
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

    return { connected, botMove, sendMove, startGame };
}