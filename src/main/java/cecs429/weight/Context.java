package cecs429.weight;

public class Context {
    private Strategy strategy;
    private int tftd, corpusSize, dft;
    private double docWeight,byteSize,aveTftd,tokensInDoc,aveTokensInCorpus;
    public Context(Strategy strategy){
        this.strategy = strategy;
    }
    public void setTftd(int mTftd){this.tftd = mTftd;}
    public void setCorpusSize(int mCorpusSize){this.corpusSize = mCorpusSize;}
    public void setDft(int mDft){this.dft = mDft;}
    public void setDocWeight(int mDocWeight){this.docWeight = mDocWeight;}
    public void setTokensInDoc(int mTokensInDoc){this.tokensInDoc = mTokensInDoc;}
    public void setAveTokensInCorpus(int mAveTokensInCorpus){this.aveTokensInCorpus = mAveTokensInCorpus;}
    public void setByteSize(int mByteSize){this.byteSize = mByteSize;}
    public void setAveTftd(int mAveTftd){this.aveTftd = mAveTftd;}
}