package cecs429.weight;

import cecs429.index.DiskPositionalIndex;

public class TfIdf implements Strategy{

    @Override
    public Float getWdt(double tftd, double tokensInDoc, double aveTokensInCorpus, double aveTftd) {
        return (float)tftd;
    }

    @Override
    public Float getWqt(double corpusSize, double dft) {
        return (float) (Math.log(corpusSize / dft));
    }

    @Override
    public Float getLd(double docWeight, double byteSize) {
        return (float)docWeight;
    }

}
