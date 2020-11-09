package cecs429.weight;

import cecs429.index.DiskPositionalIndex;

public interface Strategy {
    public Float getWdt(Integer tftd, Integer tokensInDoc, double aveTokensInCorpus, double aveTftd);
    public Float getWqt(Integer corpusSize, Integer dft);
    public Float getLd(double docWeight, Integer byteSize);
}
