package cecs429.writer;

import cecs429.encode.VariableByteEncoding;
import cecs429.index.Index;
import cecs429.index.Posting;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;


import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;


public class DiskIndexWriter {
    FileOutputStream fileOutputStream = null;
    DataOutputStream dataOutputStream = null;

    public BTreeMap writeIndex(Index index, BTreeMap<String, Integer> map, Path corpusAbsolutePath) throws IOException {
//        DB db = DBMaker.fileDB("file.db")
//                .transactionEnable()
//                .closeOnJvmShutdown()
//                .make();

//        BTreeMap<String, Integer> map = db.treeMap("map").keySerializer(Serializer.STRING).valueSerializer(Serializer.INTEGER).createOrOpen();

        Integer currentPosition = 0;
        fileOutputStream = new FileOutputStream(corpusAbsolutePath + "/index/postings.bin");
        dataOutputStream = new DataOutputStream(fileOutputStream);
        for(String s : index.getVocabulary()){
            VariableByteEncoding vbe = new VariableByteEncoding();
            ArrayList<Integer> allData = new ArrayList<>();
            int lastDocId = 0;
            for(Posting p : index.getPostings(s)){
                int lastPosition = 0;
                if(p.getDocumentId() == index.getPostings(s).get(0).getDocumentId()){
                    currentPosition = dataOutputStream.size();
                    allData.add(index.getPostings(s).size());
                }
                allData.add(p.getDocumentId() - lastDocId);
                allData.add(p.getPosition().size());
                lastDocId = p.getDocumentId();
                for(Integer position : p.getPosition()){
                    allData.add(position - lastPosition);
                    lastPosition = position;
                }
            }
            dataOutputStream.write(vbe.encode(allData));
            map.put(s,currentPosition);

        }
        dataOutputStream.close();
//        db.close();
        return map;
    }
}
