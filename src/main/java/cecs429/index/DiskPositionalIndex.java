package cecs429.index;

import cecs429.encode.VariableByteEncoding;
import org.mapdb.BTreeMap;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class DiskPositionalIndex implements Index{
    FileInputStream fileInputStream = null;
    DataInputStream dataInputStream = null;

    FileOutputStream fileOutputStream = null;
    DataOutputStream dataOutputStream = null;
    BTreeMap<String, Integer> map;

    public void loadMap(BTreeMap<String, Integer> map) {

        this.map = map;
    }

    private ArrayList<Posting> seek(Integer index, boolean checker) throws IOException {

        ArrayList<Posting> posting = new ArrayList<>();
        fileInputStream = new FileInputStream( "corpus/index/postings.bin");
        dataInputStream = new DataInputStream(fileInputStream);
        VariableByteEncoding a = new VariableByteEncoding();
        dataInputStream.skipBytes(index);
        int docCount = a.decode(findNumber(dataInputStream)).get(0);
        int docId = 0;
        for (int i = 0; i < docCount; i++) {
            int pos = 0;
            ArrayList<Integer> position = new ArrayList<>();
            docId += a.decode(findNumber(dataInputStream)).get(0);
            int termCount = a.decode(findNumber(dataInputStream)).get(0);
            if (checker) {
                for (int j = 0; j < termCount; j++) {
                    pos += a.decode(findNumber(dataInputStream)).get(0);
                    position.add(pos);
                }
            } else {
                position.add(termCount);
                for (int j = 0; j < termCount; j++) {
                    findNumber(dataInputStream);
                }
            }
            posting.add(new Posting(docId, position));

        }
        return posting;
    }

    public void storeWeight(ArrayList<weightPosting> wp) throws IOException {
        fileOutputStream = new FileOutputStream("corpus/index/docWeights.bin");
        dataOutputStream = new DataOutputStream(fileOutputStream);
        for (weightPosting p : wp) {
            dataOutputStream.writeDouble(p.getDocumentID());
            dataOutputStream.writeDouble(p.getLd());
            dataOutputStream.writeDouble(p.getDocLengthD());
            dataOutputStream.writeDouble(p.getByteSize());
            dataOutputStream.writeDouble(p.getAveTfd());
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
        int jump = (docId) * 40;
        dataInputStream.skipBytes(jump);
        try {
            dataInputStream.readDouble();
            for (int i = 0; i < 4; i++)
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
        b = inputStream.readByte();
        buf.put(b);
        if(buf.get(0) == 0x00)
            return new byte[]{0};
        else{
            while ((b & 0x80) == 0){//top-most bit of 0
                b = inputStream.readByte();
                buf.put(b);
            }
        }
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
