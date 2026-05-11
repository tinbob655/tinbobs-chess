import type Move from "./move";

export default interface socketInfo {
    connected: boolean;
    botMove: Move|null;
    sendMove (from: string, to: string):void;
    startGame():void;
}