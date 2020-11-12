package cecs429.weight;

import cecs429.index.DiskPositionalIndex;

public class Wacky implements Strategy{
    @Override
    public Float getWdt(Integer tftd, double tokensInDoc, double aveTokensInCorpus, double aveTftd) {
        return (float)((1+Math.log(tftd)/Math.log(Math.E))/(1+Math.log(aveTftd)/Math.log(Math.E)));
    }

    @Override
    public Float getWqt(Integer corpusSize, Integer dft) {
        return (float)Math.max(0, Math.log((corpusSize-dft)/dft)/Math.log(Math.E));
    }

    @Override
    public Float getLd(double docWeight, double byteSize) {
        return (float)Math.sqrt(byteSize);
    }

}
