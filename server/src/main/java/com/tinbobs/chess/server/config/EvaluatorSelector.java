package com.tinbobs.chess.server.config;

import com.tinbobs.chess.server.ApplicationConstants;
import com.tinbobs.chess.server.service.evaluator.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class EvaluatorSelector {

    private enum EvaluatorOption {RANDOM, MINIMAX, GREEDY, DELIBERATELY_LOOSE}

    private final EvaluatorOption EVALUATOR_OVERRIDE;

    public EvaluatorSelector() {

        //read text file to detect evaluator option
        this.EVALUATOR_OVERRIDE = this.readOverrideFile();
    }

    private EvaluatorOption readOverrideFile() {

        try {
            ClassPathResource resource = new ClassPathResource("static/EvaluatorOverride.txt");

            //read the file
            String content = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            ).trim();


            //file might be blank
            if (content.isEmpty()) {
                System.out.println("No evaluator override detected.");
                return null;
            }

            //make sure the result is upper case
            return EvaluatorOption.valueOf(content.toUpperCase());

        }

        //file might not exist
        catch (IOException e) {
            System.out.println("No evaluator override file detected.");
            return null;

        }

        //content of the file might be invalid
        catch (IllegalArgumentException e) {
            System.out.println("Could not parse evaluator override.");
            return null;
        }
    }

    @Bean(destroyMethod = "shutdown")
    public Evaluator getEvaluator() {

        EvaluatorOption choice;
        if (EVALUATOR_OVERRIDE != null) {

            System.out.println("Evaluator override detected");
            choice = EVALUATOR_OVERRIDE;
        }
        else {
            choice = this.deduceEvaluator();
        }

        System.out.println(choice + " EVALUATOR SELECTED");

        return switch (choice) {
            case RANDOM -> new RandomEV();
            case MINIMAX -> new Minimax();
            case GREEDY -> new Greedy();
            case DELIBERATELY_LOOSE -> new DeliberatelyLoose();
        };
    }

    //actually works out the evaluator
    private EvaluatorOption deduceEvaluator() {

        //we need to make sure we have enough memory to store the transposition table
        long maxMemory = Runtime.getRuntime().maxMemory();
        long maxSize = ApplicationConstants.transpositionTableMaxSize;

        if (maxMemory >= maxSize) {
            return EvaluatorOption.MINIMAX;
        }
        else {
            return EvaluatorOption.GREEDY;
        }
    }
}
