package cecs429.index;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class DiskPositionalIndex implements Index{
    FileInputStream fileInputStream = null;
    DataInputStream dataInputStream = null;

    FileOutputStream fileOutputStream = null;
    DataOutputStream dataOutputStream = null;
    Map<Integer, Double> weightMap = new HashMap<>();
    Double Ld;
    Double Wdt;

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
                if(termCount!=0){
                    Wdt = (1+Math.log(termCount));
                    Wdt = Math.pow(Wdt,2);

                    if(weightMap.containsKey(docId)){
                        Wdt += weightMap.get(docId);
                    }
                    weightMap.put(docId,Wdt);
                }
                dataInputStream.skipBytes(4*termCount);
            }
        }
        storeWeight(weightMap);
    }

    private ArrayList<Posting> seek(Integer index) throws IOException {

        ArrayList<Posting> posting = new ArrayList<>();
        fileInputStream = new FileInputStream( "corpus/index/postings.bin");
        dataInputStream = new DataInputStream(fileInputStream);

        dataInputStream.skipBytes(index);
        int docCount = dataInputStream.readInt();
        int docId = 0;
        for(int i = 0; i<docCount; i++){
            int pos = 0;
            ArrayList<Integer> position = new ArrayList<>();
            docId += dataInputStream.readInt();
            int termCount = dataInputStream.readInt();

            for(int j = 0; j<termCount; j++){
                pos += dataInputStream.readInt();
                position.add(pos);
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
            Wdt = (double) v;
            Ld = Math.sqrt(Wdt);
            try {
                dataOutputStream.writeDouble(docId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                dataOutputStream.writeDouble(Ld);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public double getWeight(int docId) throws IOException {
        double weight = 0;
        fileInputStream = new FileInputStream( "corpus/index/docWeights.bin");
        dataInputStream = new DataInputStream(fileInputStream);
        while(dataInputStream.available()>0){
            int id = (int) dataInputStream.readDouble();
            if(id == docId){
                return dataInputStream.readDouble();
            }else{
                dataInputStream.skipBytes(8);
            }
        }
        return weight;
    }

    @Override
    public ArrayList<Posting> getPostings(String term) {
        ArrayList<Posting> p = new ArrayList<>();
        if (map.containsKey(term)) {
            Integer index = (Integer) map.get(term);
            try {
                p = seek(index);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return p;
    }

    @Override
    public List<String> getVocabulary() {

    return null;
    }

}
