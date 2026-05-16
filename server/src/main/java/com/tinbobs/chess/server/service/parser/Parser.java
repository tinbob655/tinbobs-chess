package com.tinbobs.chess.server.service.parser;

public interface Parser<from, to> {

    //a parser transforms an input type into an output type
    to decode(from codedInput);
}
