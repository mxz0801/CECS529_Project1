import java.io.*;

import java.nio.file.Paths;
import java.util.*;

import cecs429.documents.*;
import cecs429.index.*;
import cecs429.query.BooleanQueryParser;
import cecs429.query.Query;
import cecs429.text.*;
import cecs429.weight.Strategy;
import cecs429.weight.WeightModeFactory;
import cecs429.writer.DiskIndexWriter;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;


public class IndexBuilder {

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        Scanner sc = new Scanner(System.in);
        Index index = null;
        Index indexH = null;
        Index indexJ = null;
        Index indexM = null;
        Index indexD = null;

        DB db = DBMaker
                .fileDB("file.db")
                .fileMmapEnable()
                .closeOnJvmShutdown()
                .make();
        BTreeMap<String, Integer> map= db.treeMap("mapsl", Serializer.STRING, Serializer.INTEGER).createOrOpen();;
        System.out.println("Please enter the directory of the file: ");
        String directory = sc.nextLine();
        DocumentCorpus corpusH = DirectoryCorpus.loadDirectory(Paths.get(directory+"/HAMILTON"), ".json", ".txt"); ;
        DocumentCorpus corpusJ = DirectoryCorpus.loadDirectory(Paths.get(directory+"/JAY"), ".json", ".txt"); ;
        DocumentCorpus corpusM = DirectoryCorpus.loadDirectory(Paths.get(directory+"/MADISON"), ".json", ".txt"); ;
        DocumentCorpus corpusD = DirectoryCorpus.loadDirectory(Paths.get(directory+"/DISPUTED"), ".json", ".txt"); ;
        Map<String,Integer> word = new HashMap();
        DocumentCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get(directory), ".json", ".txt"); ;
        System.out.println("1. Build index ");
        String choice = sc.nextLine();
        switch (Integer.parseInt(choice)) {
            case 1:
                long startTime = System.currentTimeMillis();
                ArrayList<weightPosting> wp = new ArrayList<>();
                System.out.println("Timer started");

                indexH = indexCorpus(corpusH,word);
                indexJ = indexCorpus(corpusJ,word);
                indexM = indexCorpus(corpusM,word);

                System.out.println(word.size());

        }



    }

    private static Index indexCorpus(DocumentCorpus corpus, Map<String,Integer> wordMap) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        ImprovedTokenProcessor processor = new ImprovedTokenProcessor();
        PositionalInvertedIndex index = new PositionalInvertedIndex();
        Set<String> totalTokens = new HashSet<>();
        int DocumentCount = 0;
        for (Document sDocument : corpus.getDocuments()) {
            DocumentCount++;
            TokenStream stream = new EnglishTokenStream(sDocument.getContent());
            Iterable<String> token = stream.getTokens();
            int position = 1;
            HashMap<String, Integer> docVocabFreq = new HashMap<>();
            Set<String> docTokens = new HashSet<>();
            for (String t : token) {
                totalTokens.add(t);
                docTokens.add(t);
                t.replaceAll("\\W", "").toLowerCase();
                String newT = getStem(t);


                if(docVocabFreq.containsKey(newT)){
                    Integer buff= docVocabFreq.get(newT);
                    buff++;
                    docVocabFreq.put(newT,buff);
                }
                else{
                    docVocabFreq.put(newT,1);
                }
                List<String> word = processor.processToken(t);
                if (word.size() > 0) {
                    for (String s : word) {
                        index.addTerm(s, sDocument.getId(), position);
                    }
                    position++;
                }
            }
        }
        index.addDocumentCount(DocumentCount);
        return index;
    }


    public static String getStem(String input) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        ImprovedTokenProcessor processor2 = new ImprovedTokenProcessor();
        return processor2.stem(input);
    }

}
