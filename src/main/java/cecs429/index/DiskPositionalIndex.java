package cecs429.index;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class DiskPositionalIndex implements Index{
    FileInputStream fileInputStream = null;
    DataInputStream dataInputStream = null;

    FileOutputStream fileOutputStream = null;
    DataOutputStream dataOutputStream = null;
    Map<Integer, Long> weightMap = new HashMap<>();
    long Ld;
    long Wdt;

    ConcurrentMap map;


    public void loadMap(ConcurrentMap map) {

        this.map = map;
    }

    public void docWeight() throws IOException {
        fileInputStream = new FileInputStream( "corpus/index/postings.bin");
        dataInputStream = new DataInputStream(fileInputStream);
        while(dataInputStream.available()>0){
            int docCount = dataInputStream.readInt();
            for(int i = 0; i<docCount; i++){
                int docId = dataInputStream.readInt();
                int termCount = dataInputStream.readInt();
                Wdt = (long) (1+Math.log(termCount));

                if(weightMap.containsKey(docId)){
                    Wdt += weightMap.get(docId);
                }
                weightMap.put(docId,Wdt);
                dataInputStream.skipBytes(4*termCount);
            }
        }
        storeWeight(weightMap);
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
            }
            posting.add(new Posting(docId,position));
        }
        return posting;
    }

    private void storeWeight(Map weightMap) throws IOException {
        fileOutputStream = new FileOutputStream("corpus/index/docWeights.bin");
        dataOutputStream = new DataOutputStream(fileOutputStream);

        weightMap.forEach((k,v) ->{
            int docId = (int) k;
            System.out.println("aaa" + docId);
            Wdt = (long) v;
            Ld = (long) Math.sqrt(Math.pow(Wdt,2));
            try {
                dataOutputStream.writeLong(docId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                dataOutputStream.writeLong(Ld);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public long getWeight(int docId) throws IOException {
        long weight = 0;
        fileInputStream = new FileInputStream( "corpus/index/docWeights.bin");
        dataInputStream = new DataInputStream(fileInputStream);
        while(dataInputStream.available()>0){
            int id = (int) dataInputStream.readLong();
            if(id == docId){
                return dataInputStream.readLong();
            }else{
                dataInputStream.skipBytes(8);
            }
        }
        return weight;
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
