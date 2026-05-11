import React, {useRef} from 'react';
import PageHeader from '../multiPageComponents/pageHeader';
import ChessBoard from './chessBoard';


export default function Home():React.ReactElement {

    const playNowWrapper:React.RefObject<HTMLDivElement | null> = useRef<HTMLDivElement>(null);


    return (
        <React.Fragment>
            <PageHeader title="Tinbob's Chess" subtitle="Anonymously declared top #1 chess websites worldwide" />

            <div id="playNowWrapper" ref={playNowWrapper}>
                <button id="playNowButton" onClick={playButtonClicked}>
                    <h3 className="noVerticalSpacing" style={{transform: 'unset'}}>
                        Play some chess!
                    </h3>
                </button>
            </div>

            <div id="chessBoardWrapper">
                <ChessBoard />
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

    async function playButtonClicked():Promise<void> {

        //hide the play button
        playNowWrapper.current?.classList.add('hidden');

        //start the game
    }
}