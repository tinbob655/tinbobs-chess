import {useEffect, useRef, useState} from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

interface Move {
    from: string;
    to: string;
    playerName: string;
}

interface res {
    connected: boolean;
    botMove: Move|null;
    sendMove(from: string, to: string):void;
    startGame():void;
}

export function useChessSocket():res {


    const clientRef = useRef<Client|null>(null);
    const [botMove, setBotMove] = useState<Move|null>(null);
    const [connected, setConnected] = useState(false);


    //creates a STOMP client
    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),

            onConnect: () => {
                setConnected(true);
                console.log('Connected to server...');

                //listen to /topic/game
                client.subscribe('/topic/game', (message) => {
                    const move:Move = JSON.parse(message.body);
                    setBotMove(move);
                });
            },

            onDisconnect: () => setConnected(false),
        });

        client.activate();
        clientRef.current = client;

        //clean stuff up on dismount
        return () => { client.deactivate(); };
    }, []);

    //other components will call this to send a human move to the backend
    function sendMove(from: string, to: string):void {
        clientRef.current?.publish({
            destination: '/app/move',
            body: JSON.stringify({ from, to, player: 'white' }),    //the human always plays as white
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