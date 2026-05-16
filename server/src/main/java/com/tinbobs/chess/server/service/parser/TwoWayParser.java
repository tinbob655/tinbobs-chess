package com.tinbobs.chess.server.service.parser;

public interface TwoWayParser<from, to> extends Parser<from, to> {

    //a two-way parser can decode an input like a normal parser but can also go the other way
    from encode(to decodedInput);
}
