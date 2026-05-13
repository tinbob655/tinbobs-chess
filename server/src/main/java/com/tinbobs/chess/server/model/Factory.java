package com.tinbobs.chess.server.model;

public interface Factory<T, A, B, C> {

    T create(A a, B b, C c);
}
