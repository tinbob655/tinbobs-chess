package com.tinbobs.chess.server.config;

import com.tinbobs.chess.server.ApplicationConstants;
import com.tinbobs.chess.server.service.evaluator.Evaluator;
import com.tinbobs.chess.server.service.evaluator.Greedy;
import com.tinbobs.chess.server.service.evaluator.Minimax;
import com.tinbobs.chess.server.service.evaluator.RandomEV;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EvaluatorSelector {

    private enum EvaluatorOption {RANDOM, MINIMAX, GREEDY}

    private static final boolean USE_RANDOM_EVALUATION = false;

    @Bean(destroyMethod = "shutdown")
    public Evaluator getEvaluator() {
        return switch (this.deduceEvaluator()) {
            case RANDOM -> new RandomEV();
            case MINIMAX -> new Minimax();
            case GREEDY -> new Greedy();
        };
    }

    //actually works out the evaluator
    private EvaluatorOption deduceEvaluator() {

        //manual option to choose random evaluation
        if (USE_RANDOM_EVALUATION) {
            return EvaluatorOption.RANDOM;
        }

        //we need to make sure we have enough memory to store the transposition table
        long maxMemory = Runtime.getRuntime().maxMemory();
        long maxSize = ApplicationConstants.transpositionTableMaxSize;

        if (maxMemory >= maxSize) {
            System.out.println("MINIMAX EVALUATOR SELECTED.");
            return EvaluatorOption.MINIMAX;
        }
        else {
            System.out.println("GREEDY EVALUATOR SELECTED.");
            return EvaluatorOption.GREEDY;
        }
    }
}
