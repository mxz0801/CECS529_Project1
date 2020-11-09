package cecs429.index;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class DiskPositionalIndex implements Index{
    FileInputStream fileInputStream = null;
    DataInputStream dataInputStream = null;

    ConcurrentMap map;


    public void loadMap(ConcurrentMap map) {

        this.map = map;
    }

    private List<Posting> seek(Integer index) throws IOException {
        List<Posting> posting = new ArrayList<>();
        fileInputStream = new FileInputStream( "corpus/index/postings.bin");
        dataInputStream = new DataInputStream(fileInputStream);
        dataInputStream.skipBytes(index);
        int docCount = dataInputStream.readInt();
        for(int i = 0; i<docCount; i++){
            ArrayList<Integer> position = new ArrayList<>();
            int docId = dataInputStream.readInt();
            int termCount = dataInputStream.readInt();
            for(int j = 0; j<termCount; j++){
                position.add(dataInputStream.readInt());
                j++;
            }
            posting.add(new Posting(docId,position));
        }
        return posting;
    }

    @Override
    public List<Posting> getPostings(String term) throws IOException {
        ArrayList<Posting> p = new ArrayList<>();
        if(!map.containsKey(term)){
            return p;
        }else{
            Integer index = (Integer) map.get(term);
            System.out.println(index);
            return seek(index);
        }
    }

    @Override
    public List<String> getVocabulary() {

    return null;
    }

}
