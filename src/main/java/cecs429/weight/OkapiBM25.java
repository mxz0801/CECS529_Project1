package cecs429.weight;


public class OkapiBM25 implements Strategy{

    @Override
    public Float getWdt(Integer tftd, Integer tokensInDoc, double aveTokensInCorpus, double aveTftd) {
        return (float)((2.2*tftd)/(1.2*(0.25+0.75*tokensInDoc/aveTokensInCorpus)+tftd));
    }

    @Override
    public Float getWqt(Integer corpusSize, Integer dft) {
        return (float) Math.max(0.1, Math.log((corpusSize-dft+0.5) / (dft+0.5))/Math.log(Math.E));
    }

    @Override
    public Float getLd(double docWeight, Integer byteSize) {
        return (float)1;
    }

}
