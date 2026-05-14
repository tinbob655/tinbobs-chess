import type Move from "./move";
import type { status } from "./status";

export default interface socketInfo {
    connected: boolean;
    botMove: Move|null;
    sendMove (from: string, to: string):Promise<void>;
    startGame():void;
    status: status;
    waitingForBotMove: boolean;
    getValidMoves (squareIndex: number): Promise<number[]>;
}