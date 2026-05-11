export default function squareToIndex(square: string):number {

    const x:number = square.charCodeAt(0) - 'a'.charCodeAt(0);
    const y:number = parseInt(square[1]) - 1;
    
    return (x * 8) + y;
}