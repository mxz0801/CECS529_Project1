package cecs429.index;

public class weightPosting {
    private int mDocumentID;
    private double mLd;
    private int mDocLengthD;
    private int mByteSize;
    private double mAveTfd;
    public weightPosting(int documenID,double Ld,int docLengthD, int byteSize, double aveTfd) {
        this.mDocumentID = documenID;
        this.mLd = Ld;
        this.mDocLengthD = docLengthD;
        this.mByteSize = byteSize;
        this.mAveTfd = aveTfd;
    }
    public weightPosting(){}

    public int getDocumentID() {
        return mDocumentID;
    }
    public int getDocLengthD() {
        return mDocLengthD;
    }
    public int getByteSize(){
        return mByteSize;
    }
    public double getAveTfd(){
        return mAveTfd;
    }
    public  double getLd()
    {
        return mLd;
    }
}
