package cecs429.weight;


public interface Strategy {
    public Float getWdt(double tftd, double tokensInDoc, double aveTokensInCorpus, double aveTftd);
    public Float getWqt(double corpusSize, double dft);
    public Float getLd(double docWeight, double byteSize);
}
