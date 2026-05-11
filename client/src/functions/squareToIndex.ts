export default function squareToIndex(square: string):number {
    const x = square.charCodeAt(0);
    const y = parseInt(square[1]);
    return (x * 8) + y;
}