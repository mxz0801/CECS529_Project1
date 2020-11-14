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
        DiskPositionalIndex dIndex = new DiskPositionalIndex();
        DB db = DBMaker
                .fileDB("file.db")
                .fileMmapEnable()
                .closeOnJvmShutdown()
                .make();
        BTreeMap<String, Integer> map= db.treeMap("mapsl", Serializer.STRING, Serializer.INTEGER).createOrOpen();;
        System.out.println("Please enter the directory of the file: ");
        String directory = sc.nextLine();
        DocumentCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get(directory), ".json", ".txt"); ;
        System.out.println("1. Build index ");
        System.out.println("2. Query index ");
        String choice = sc.nextLine();
        switch (Integer.parseInt(choice)) {
            case 1:
                long startTime = System.currentTimeMillis();
                ArrayList<weightPosting> wp = new ArrayList<>();
                System.out.println("Timer started");
                KgramIndex kGramIndex = new KgramIndex();
                index = indexCorpus(corpus, kGramIndex, dIndex, wp);
                DiskIndexWriter writer = new DiskIndexWriter();
                map = writer.writeIndex(index,map,Paths.get(directory));
                dIndex.storeWeight(wp);
                db.close();
                System.out.println("Done!");
            case 2:
                db.close();
                db = DBMaker
                        .fileDB("file.db")
                        .fileMmapEnable()
                        .closeOnJvmShutdown()
                        .make();
                map= db.treeMap("mapsl", Serializer.STRING, Serializer.INTEGER).createOrOpen();
                corpus.getCorpusSize();
                System.out.println("Select modes: ");
                System.out.println("1. Boolean query mode");
                System.out.println("2. Ranked query mode");
                String mode = sc.nextLine();
                dIndex.loadMap(map);
                switch (Integer.parseInt(mode)) {
                    case 1:
                        while (true) {
                            System.out.print("Pleas enter the term to search for: ");
                            String query = sc.nextLine();
                            if (query.equals("quit")) {
                                System.out.println("Exit the search.");
                                break;
                            } else if (query.contains(":stem")) {
                                String[] spliter = query.split(" ");
                                String stemToken = spliter[1];
                                ImprovedTokenProcessor processor2 = new ImprovedTokenProcessor();
                                String processedToken = processor2.stem(stemToken);
                                System.out.println(stemToken + "-->" + processedToken);
                            } else if (query.equals(":vocab")) {
                                for (int i = 0; i < 1000; i++) {
                                    System.out.println(dIndex.getVocabulary().get(i));
                                }
                                System.out.println(dIndex.getVocabulary().size());
                            } else {
                                try {
                                    query = processQuery(query);
                                    System.out.println(query);
                                    BooleanQueryParser parser = new BooleanQueryParser();
                                    Query queryPosting = parser.parseQuery(query);
                                    ArrayList<Posting> result = new ArrayList<>(queryPosting.getPostings(dIndex));
                                    for (Posting p : result) {
                                        Reader read = corpus.getDocument(p.getDocumentId()).getContent();
                                        System.out.println("Document: " + corpus.getDocument(p.getDocumentId()).getFileTitle());
                                        read.close();
                                    }
                                    System.out.println(result.size());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    case 2:
                        ArrayList<topKPosting> topK;
                        WeightModeFactory weightF = new WeightModeFactory();
                        System.out.println("Pleas enter the mode: ");
                        System.out.println("1. Default ");
                        System.out.println("2. tf-idf ");
                        System.out.println("3. Okapi BM25 ");
                        System.out.println("4. Wacky ");
                        String weight = sc.nextLine();
                        while (true) {
                            System.out.print("Pleas enter the term to search for: ");
                            String query = sc.nextLine();
                            if (query.equals("quit")) {
                                System.out.println("Exit the search.");
                                db.close();
                                break;
                            } else {
                                query = getStem(query);
                                Strategy weightMode = weightF.getMode(weight);
                                topK = score(weightMode, query, dIndex, corpus.getCorpusSize(), 10);

                            }
                            for (topKPosting tp : topK) {
                                Reader read = corpus.getDocument(tp.getDocumentId()).getContent();
                                System.out.print("Title: " + corpus.getDocument(tp.getDocumentId()).getFileTitle());
                                System.out.println(" Score: " + tp.getScore());
                                read.close();
                            }
                        }
                        break;
                }
        }



    }

    private static Index indexCorpus(DocumentCorpus corpus, KgramIndex kgramIndex,DiskPositionalIndex dIndex, ArrayList<weightPosting> wp) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        ImprovedTokenProcessor processor = new ImprovedTokenProcessor();
        PositionalInvertedIndex index = new PositionalInvertedIndex();
        Set<String> totalTokens = new HashSet<>();
        for (Document sDocument : corpus.getDocuments()) {
            TokenStream stream = new EnglishTokenStream(sDocument.getContent());
            Iterable<String> token = stream.getTokens();
            int byteSize = 0, tfCount = 0;
            int position = 1;
            HashMap<String, Integer> docVocabFreq = new HashMap<>();
            Set<String> docTokens = new HashSet<>();
            for (String t : token) {
                byteSize += t.length();
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
                    tfCount++;
                }
            }
            double Ld = 0;
            for(double ld : docVocabFreq.values()){
                double wdt = 1 + Math.log(ld);
                Ld += Math.pow(wdt,2);
            }
            Ld = Math.sqrt(Ld);
            weightPosting w = new weightPosting(sDocument.getId(),Ld, docTokens.size(), byteSize,((double)tfCount/docVocabFreq.size()));
            wp.add(w);
            stream.close();
        }

        dIndex.storeDocLength(totalTokens.size()/ corpus.getCorpusSize());
        return index;
    }


    private static ArrayList<topKPosting> score(Strategy weighMode, String query, DiskPositionalIndex dIndex, Integer corpusSize, Integer k) throws IOException {
        Map<Integer, Float> accumulators = new HashMap<>();
        for (String s : query.split(" ")) {
            List<Posting> temp = dIndex.getPostings(s,false);
            Float wqt = weighMode.getWqt(corpusSize, temp.size());
           // System.out.println("doing wqt" );
            for (Posting p : temp) {
                Float wdt = weighMode.getWdt(p.getPosition().get(0),
                        dIndex.getWeight(p.getDocumentId()).get(1),
                        dIndex.getDocLength(),
                        dIndex.getWeight(p.getDocumentId()).get(3));
                //System.out.println("doing WDT");

                if (!accumulators.containsKey(p.getDocumentId())) {
                    accumulators.put(p.getDocumentId(), wdt * wqt);
                    //System.out.println("PUTTING");

                } else {
                    Float newWeight = accumulators.get(p.getDocumentId()) + wdt * wqt;
                    accumulators.put(p.getDocumentId(), newWeight);
                }
            }
        }
        for (Integer i : accumulators.keySet()) {
            Float acc = accumulators.get(i) / weighMode.getLd(dIndex.getWeight(i).get(0), dIndex.getWeight(i).get(2));
            accumulators.put(i, acc);
        }
        PriorityQueue<Map.Entry<Integer, Float>> pq = new PriorityQueue<>(k, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        ArrayList<topKPosting> results = new ArrayList<>();
        for (Map.Entry<Integer, Float> entry : accumulators.entrySet()) {
            pq.offer(entry);
        }
        int count =0;
        while (!pq.isEmpty() && count <k) {
            topKPosting tp = new topKPosting(pq.peek().getKey(), pq.poll().getValue());
            results.add(tp);
            count++;
        }
        return results;
    }
    public static String processQuery(String query) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        String[] str = query.split(" ");
        StringBuilder newQuery = new StringBuilder();
        for(String s : str){
            s = s.toLowerCase();
            if(s.equals(str[str.length-1])) {
                if(s.contains("\"")){
                    s = s.substring(0, s.length() -1);
                    newQuery.append(getStem(s)).append("\"");
                }
                else if(s.contains("*"))
                    newQuery.append(s);
                else
                    newQuery.append(getStem(s));
            }
            else if(s.contains("*"))
                newQuery.append(s).append(" ");
            else
                newQuery.append(getStem(s)).append(" ");
        }

        return newQuery.toString();

    }
    public static String getStem(String input) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        ImprovedTokenProcessor processor2 = new ImprovedTokenProcessor();
        return processor2.stem(input);
    }

}
