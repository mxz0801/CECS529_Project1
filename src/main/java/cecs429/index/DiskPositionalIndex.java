package cecs429.index;

import cecs429.encode.VariableByteEncoding;
import org.mapdb.BTreeMap;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class DiskPositionalIndex implements Index{
    FileInputStream fileInputStream = null;
    DataInputStream dataInputStream = null;

    FileOutputStream fileOutputStream = null;
    DataOutputStream dataOutputStream = null;
    BTreeMap<String, Integer> map;

    public void loadMap(BTreeMap<String, Integer> map) {

        this.map = map;
    }

//    public void docWeight(ArrayList<weightPosting> wp) throws IOException {
//        fileInputStream = new FileInputStream( "corpus/index/postings.bin");
//        dataInputStream = new DataInputStream(fileInputStream);
//        while(dataInputStream.available()>0){
//            int docCount = dataInputStream.readInt();
//            for(int i = 0; i<docCount; i++){
//                int docId = dataInputStream.readInt();
//                int termCount = dataInputStream.readInt();
//                if(termCount!=0){
//                    Wdt = (1+Math.log(termCount));
//                    Wdt = Math.pow(Wdt,2);
//
//                    if(weightMap.containsKey(docId)){
//                        Wdt += weightMap.get(docId);
//                    }
//                    weightMap.put(docId,Wdt);
//                }
//                dataInputStream.skipBytes(4*termCount);
//            }
//        }
//        storeWeight(weightMap,wp);
//    }

    private ArrayList<Posting> seek(Integer index, boolean checker) throws IOException {

        ArrayList<Posting> posting = new ArrayList<>();
        fileInputStream = new FileInputStream( "corpus/index/postings.bin");
        dataInputStream = new DataInputStream(fileInputStream);
        VariableByteEncoding a = new VariableByteEncoding();
        dataInputStream.skipBytes(index);
        int docCount = dataInputStream.readInt();
//        int docCount = a.decode(findNumber(dataInputStream)).get(0);
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

    public void storeWeight(ArrayList<weightPosting> wp) throws IOException {
        fileOutputStream = new FileOutputStream("corpus/index/docWeights.bin");
        dataOutputStream = new DataOutputStream(fileOutputStream);
                for(weightPosting p : wp){
                        dataOutputStream.writeDouble(p.getDocumentID());
                        dataOutputStream.writeDouble(p.getLd());
                        dataOutputStream.writeDouble(p.getDocLengthD());
                        dataOutputStream.writeDouble(p.getByteSize());
                        dataOutputStream.writeDouble(p.getAveTfd());
                        break;
                    }

    }
    public void storeDocLength(int totalTokens) throws IOException {
        fileOutputStream = new FileOutputStream("corpus/index/docLength.bin");
        dataOutputStream = new DataOutputStream(fileOutputStream);
        dataOutputStream.writeDouble(totalTokens);
    }

    public List<Double> getWeight(int docId) throws IOException {
        List<Double> result = new ArrayList<>();
        fileInputStream = new FileInputStream( "corpus/index/docWeights.bin");
        dataInputStream = new DataInputStream(fileInputStream);
        int jump = (docId)*40;
        dataInputStream.skipBytes(jump);
        try {
            int id = (int) dataInputStream.readDouble();
            for(int i=0; i < 4; i++)
                result.add(dataInputStream.readDouble());
            dataInputStream.close();
            fileInputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }

//        while(dataInputStream.available()>0){
//            int id = (int) dataInputStream.readDouble();
//            if(id == docId){
//                for(int i=0; i < 4; i++)
//                    result.add(dataInputStream.readDouble());
//                break;
//            }else{
//                dataInputStream.skipBytes(32);
//            }
//        }
        return result;
    }

    private byte[] findNumber(DataInputStream inputStream) throws IOException {
        Byte b;
        ByteBuffer buf = ByteBuffer.allocate((Integer.SIZE / Byte.SIZE));
        do{
            b = inputStream.readByte();
            buf.put(b);
        }while((b & 0x80) == 0);//top-most bit of 0
        buf.flip();
        byte[] result = new byte[buf.limit()];
        buf.get(result);
        return result;
    }
    public double getDocLength() throws IOException {
        fileInputStream = new FileInputStream( "corpus/index/docLength.bin");
        dataInputStream = new DataInputStream(fileInputStream);
        double result = dataInputStream.readDouble();
        dataInputStream.close();
        fileInputStream.close();
        return result;
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
