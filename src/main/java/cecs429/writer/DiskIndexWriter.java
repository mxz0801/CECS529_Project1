package cecs429.writer;

import cecs429.index.Index;
import cecs429.index.Posting;
import org.mapdb.DB;
import org.mapdb.DBMaker;


import java.io.*;
import java.nio.file.Path;

import java.util.concurrent.ConcurrentMap;

public class DiskIndexWriter {
    FileOutputStream fileOutputStream = null;
    DataOutputStream dataOutputStream = null;

    public ConcurrentMap writeIndex(Index index, Path corpusAbsolutePath) throws IOException {
        DB db = DBMaker.fileDB("file.db")
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();

        ConcurrentMap map = db.hashMap("map").createOrOpen();

        Integer currentPosition = 0;
        fileOutputStream = new FileOutputStream(corpusAbsolutePath + "//index//postings.bin");
        dataOutputStream = new DataOutputStream(fileOutputStream);
        for(String s : index.getVocabulary()){
            int lastDocId = 0;
            int lastPosition = 0;
            for(Posting p : index.getPostings(s)){
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

        db.close();

        return map;
    }
}
