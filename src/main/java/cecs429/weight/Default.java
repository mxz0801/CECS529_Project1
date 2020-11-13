package cecs429.weight;

import cecs429.index.DiskPositionalIndex;
import cecs429.index.Posting;

import java.util.HashMap;

public class Default implements Strategy{

    @Override
    public Float getWdt(double tftd, double tokensInDoc, double aveTokensInCorpus, double aveTftd) {
        return (float)(1 + Math.log(tftd));
    }

    @Override
    public Float getWqt(double corpusSize, double dft) {
        return (float) (Math.log(1 + corpusSize / dft));
    }

    @Override
    public Float getLd(double docWeight, double byteSize) {
        return (float)docWeight;
    }

}
