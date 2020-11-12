package cecs429.index;

import org.mapdb.BTreeMap;

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

    ConcurrentMap<String, Integer> map;


    public void loadMap(ConcurrentMap<String, Integer> map) {

        this.map = map;
    }

    public void docWeight(int countTokens) throws IOException {
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
        storeWeight(weightMap,countTokens);
    }

    private ArrayList<Posting> seek(Integer index, boolean checker) throws IOException {

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
            if(checker){
                for(int j = 0; j<termCount; j++){
                    pos += dataInputStream.readInt();
                    position.add(pos);
                }
            }else{
                position.add(termCount);
                dataInputStream.skipBytes(4*termCount);
            }
            posting.add(new Posting(docId,position));

        }
        return posting;
    }

    private void storeWeight(Map weightMap,int countTokens) throws IOException {
        fileOutputStream = new FileOutputStream("corpus/index/docWeights.bin");
        dataOutputStream = new DataOutputStream(fileOutputStream);

        weightMap.forEach((k,v) ->{
            int docId = (int) k;
            Wdt = (double) v;
            Ld = Math.sqrt(Wdt);
            try {
                dataOutputStream.writeDouble(docId);
                dataOutputStream.writeDouble(Ld);
                dataOutputStream.writeDouble(countTokens);
                dataOutputStream.writeDouble(countTokens);
                dataOutputStream.writeDouble(countTokens);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public double getWeight(int docId) throws IOException {
        double weight = 0;
        List<Double> result = new ArrayList<>();
        fileInputStream = new FileInputStream( "corpus/index/docWeights.bin");
        dataInputStream = new DataInputStream(fileInputStream);
        while(dataInputStream.available()>0){
            int id = (int) dataInputStream.readDouble();
            if(id == docId){
                for(int i=0;i<4;i++)
                    result.add(dataInputStream.readDouble()); //ld
                return dataInputStream.readDouble();
            }else{
                dataInputStream.skipBytes(32);
            }
        }
        return weight;
    }

    @Override
    public ArrayList<Posting> getPostings(String term) {
        ArrayList<Posting> p = new ArrayList<>();
        if (map.containsKey(term)) {
            Integer index =  map.get(term);
            try {
                p = seek(index,true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return p;
    }

    @Override
    public List<Posting> getPostings(String term, boolean check) {
        ArrayList<Posting> p = new ArrayList<>();
        if (map.containsKey(term)) {
            Integer index =  map.get(term);
            try {
                p = seek(index,check);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return p;
    }

    @Override
    public List<String> getVocabulary() {
        List<String> vocab = new ArrayList<>();
        map.forEach((k,v) ->{
            vocab.add(k);
        });
        return vocab;
    }
}
