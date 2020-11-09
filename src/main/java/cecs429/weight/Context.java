package cecs429.weight;

import cecs429.index.DiskPositionalIndex;

import java.io.IOException;

public class Context {
    private Strategy strategy;
    private Float wdt;
    private Float wqt;
    private Float Ld;
    public Context(Strategy strategy){
        this.strategy = strategy;
    }

    public Float executeStrategy(String query, DiskPositionalIndex dIndex, Integer corpusSize) throws IOException {
        return null;
    }
}
