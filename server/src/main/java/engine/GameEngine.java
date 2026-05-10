package engine;


import model.player.Player;
import model.state.GameState;
import model.state.Move;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

//SINGLETON
@Service
public class GameEngine implements Engine_API {

    private static GameEngine instance;
    private GameState state;
    private final List<Player> players = new ArrayList<>();
    private int turnIndex = 0;

    private GameEngine() {};

    public static GameEngine getInstance() {

        if (instance == null) {
            instance = new GameEngine();
        }

        return instance;
    }

    public void startGame() {

        //need to run on a separate thread to do blocking
        CompletableFuture.runAsync(() -> {
            while (!this.state.isGameOver()) {
                this.turn();

                //send state to frontend
                
            }
        });
    }

    public GameState getState() {
        return this.state;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public void turn() {

        Player currentPlayer = this.players.get(this.turnIndex);
        Move move = currentPlayer.turn(this.state);

        //validate the move
        Set<Move> validMoves = this.state.getLegalMoves();
        if (!validMoves.contains(move)) {
            throw new IllegalArgumentException("Illegal move");
        }

        //do the move
        this.state = this.advance(move);
    }

    private GameState advance(Move move) {
        return null;
    }
}
