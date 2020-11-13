package cecs429.weight;

import cecs429.index.DiskPositionalIndex;

public class Wacky implements Strategy{
    @Override
    public Float getWdt(double tftd, double tokensInDoc, double aveTokensInCorpus, double aveTftd) {
        return (float)((1+Math.log(tftd))/(1+Math.log(aveTftd)));
    }

    @Override
    public Float getWqt(double corpusSize, double dft) {
        return (float)Math.max(0, Math.log((corpusSize-dft)/dft));
    }

    @Override
    public Float getLd(double docWeight, double byteSize) {
        return (float)Math.sqrt(byteSize);
    }

}
