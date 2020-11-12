import java.io.*;

import java.nio.file.Paths;
import java.util.*;

import cecs429.documents.*;
import cecs429.index.*;
import cecs429.query.BooleanQueryParser;
import cecs429.query.Query;
import cecs429.text.*;
import cecs429.weight.Default;
import cecs429.weight.Strategy;
import cecs429.weight.WeightModeFactory;
import cecs429.writer.DiskIndexWriter;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

//TESTING
public class IndexBuilder {
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        Scanner sc = new Scanner(System.in);
        DiskPositionalIndex dIndex = new DiskPositionalIndex();
        BTreeMap<String, Integer> map = null;

        System.out.println("Please enter the directory of the file: ");
        String directory = sc.nextLine();
        DocumentCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get(directory), ".json", ".txt"); ;
        System.out.println("1. Build index: ");
        System.out.println("2. Query index: ");
        String choice = sc.nextLine();
        switch (Integer.parseInt(choice)) {
            case 1:
                long startTime = System.currentTimeMillis();
                System.out.println("Timer started");
                KgramIndex kGramIndex = new KgramIndex();
                Index index = indexCorpus(corpus, kGramIndex);
                DiskIndexWriter writer = new DiskIndexWriter();
                map = writer.writeIndex(index, Paths.get(directory));
                dIndex.docWeight();
                System.out.println("Done!");
            case 2:
                corpus.getDocuments();
                System.out.println("Select modes: ");
                System.out.println("1. Boolean query mode");
                System.out.println("2. Ranked query mode");
                String mode = sc.nextLine();
                switch (Integer.parseInt(mode)) {
                    case 1:


                    case 2:
                        ArrayList<topKPosting> topK;
                        dIndex.loadMap(map);
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
                                break;
                            } else {
                                Strategy weightMode = WeightModeFactory.getMode(weight);
                                topK = score(weightMode, query, dIndex, corpus.getCorpusSize(), 10);

                            }
                            for (topKPosting tp : topK) {
                                System.out.print("Title: " + corpus.getDocument(tp.getDocumentId()).getFileTitle());
                                System.out.println(" Score: " + tp.getScore());
                            }
                        }
                        break;
                }
        }



    }

    private static Index indexCorpus(DocumentCorpus corpus, KgramIndex kgramIndex) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        Set<String> vocab = new HashSet<>();
        ImprovedTokenProcessor processor = new ImprovedTokenProcessor();
        PositionalInvertedIndex index = new PositionalInvertedIndex();
        for (Document sDocument : corpus.getDocuments()) {
            TokenStream stream = new EnglishTokenStream(sDocument.getContent());
            Iterable<String> token = stream.getTokens();
            int position = 1;
            for (String t : token) {
                vocab.add(t.replaceAll("\\W", "").toLowerCase());
                List<String> word = processor.processToken(t);
                if (word.size() > 0) {
                    for (String s : word)
                        index.addTerm(s, sDocument.getId(), position);
                    position++;
                }
            }
            stream.close();
        }
        for (String s : vocab) {
            kgramIndex.addTerm(s);
        }
        return index;
    }


    private static ArrayList<topKPosting> score(Strategy weighMode, String query, DiskPositionalIndex dIndex, Integer corpusSize, Integer k) throws IOException {
        HashMap<Integer, Float> accumulators = new HashMap<>();
        for (String s : query.split(" ")) {
            for (Posting p : dIndex.getPostings(s,false)) {
                Float wqt = weighMode.getWqt(corpusSize, dIndex.getPostings(s,false).size());
                Float wdt = weighMode.getWdt(p.getPosition().get(0), 2, 3, 4);
                Float newWeight;
                if (accumulators.get(p.getDocumentId()) == null) {
                    accumulators.put(p.getDocumentId(), wdt * wqt);
                } else {
                    newWeight = accumulators.get(p.getDocumentId()) + wdt * wqt;
                    accumulators.put(p.getDocumentId(), newWeight);
                }
            }
        }
        for (Integer i : accumulators.keySet()) {
            Double ii = dIndex.getWeight(i);
            Float acc = (float) (accumulators.get(i) / weighMode.getLd(dIndex.getWeight(i), 5));
            accumulators.put(i, acc);
        }
        PriorityQueue<Map.Entry<Integer, Float>> pq = new PriorityQueue<>(k, new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
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

}
