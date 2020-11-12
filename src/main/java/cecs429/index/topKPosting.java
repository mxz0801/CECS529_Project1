package cecs429.index;

public class topKPosting {
    private int mDocumentId;
    private Float mScore;
    public topKPosting(int documentId, Float score) {
        this.mDocumentId = documentId;
        this.mScore = score;
    }

    public int getDocumentId() {
        return mDocumentId;
    }

    public Float getScore(){
        return mScore;
    }
}
