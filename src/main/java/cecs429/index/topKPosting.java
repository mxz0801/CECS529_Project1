package cecs429.index;

public class topKPosting {
    private int mDocumentId;
    private Float mScore;
    public topKPosting(int documentId, Float score) {
        mDocumentId = documentId;
        mScore = score;
    }

    public int getDocumentId() {
        return mDocumentId;
    }

    public Float getScore(){
        return mScore;
    }
}
