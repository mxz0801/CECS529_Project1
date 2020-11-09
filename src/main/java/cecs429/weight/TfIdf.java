package cecs429.weight;

import cecs429.index.DiskPositionalIndex;

public class TfIdf implements Strategy{

    @Override
    public Float getWdt(Integer tftd, Integer tokensInDoc, double aveTokensInCorpus, double aveTftd) {
        return (float)tftd;
    }

    @Override
    public Float getWqt(Integer corpusSize, Integer dft) {
        return (float) Math.log(corpusSize / dft);
    }

    @Override
    public Float getLd(double docWeight, Integer byteSize) {
        return (float)docWeight;
    }

}
