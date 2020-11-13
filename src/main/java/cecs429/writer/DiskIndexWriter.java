package cecs429.writer;

import cecs429.index.Index;
import cecs429.index.Posting;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;


import java.io.*;
import java.nio.file.Path;
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
            int lastDocId = 0;
            for(Posting p : index.getPostings(s)){
                int lastPosition = 0;
                if(p.getDocumentId() == index.getPostings(s).get(0).getDocumentId()){
                    currentPosition = dataOutputStream.size();
                    dataOutputStream.writeInt(index.getPostings(s).size());
                }
                dataOutputStream.writeInt(p.getDocumentId() - lastDocId);
                dataOutputStream.writeInt(p.getPosition().size());

                lastDocId = p.getDocumentId();
                for(Integer position : p.getPosition()){
                    dataOutputStream.writeInt(position - lastPosition);
                    lastPosition = position;
                }
            }

            map.put(s,currentPosition);

        }
        dataOutputStream.close();
//        db.close();
        return map;
    }
}
