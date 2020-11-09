import java.io.*;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import cecs429.documents.*;
import cecs429.index.*;
import cecs429.query.BooleanQueryParser;
import cecs429.query.Query;
import cecs429.text.*;
import cecs429.weight.Context;
import cecs429.weight.Default;
import cecs429.weight.Strategy;
import cecs429.weight.WeightModeFactory;
import cecs429.writer.DiskIndexWriter;

public class IndexBuilder {
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        System.out.println("Please enter the directory of the file: ");
        Scanner sc = new Scanner(System.in);
        String directory = sc.nextLine();
        long startTime = System.currentTimeMillis();
        System.out.println("Timer started");
        DocumentCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get(directory), ".json", ".txt");   //read json file
        KgramIndex kGramIndex = new KgramIndex();
        Index index = indexCorpus(corpus, kGramIndex);
        System.out.println("Done!");
        DiskIndexWriter writer = new DiskIndexWriter();
        ConcurrentMap map = writer.writeIndex(index, Paths.get(directory));
        DiskPositionalIndex dIndex = new DiskPositionalIndex();
        dIndex.loadMap(map);
        dIndex.docWeight();

        System.out.println("Select modes: ");
        System.out.println("1. Boolean query mode");
        System.out.println("2. Ranked query mode");
        String mode = sc.nextLine();
        switch (Integer.parseInt(mode)) {
            case 1:



            case 2:
                while (true) {
                    HashMap<Integer, Float> topK;
                    System.out.println("1. Default ");
                    System.out.println("2. tf-idf ");
                    System.out.println("3. Okapi BM25 ");
                    System.out.println("4. Wacky ");
                    System.out.print("Pleas enter the mode: ");
                    String weight = sc.nextLine();
                    System.out.print("Pleas enter the term to search for: ");
                    String query = sc.nextLine();
                    if (query.equals("quit")) {
                        System.out.println("Exit the search.");
                        break;
                    } else {
                        Strategy weightMode = WeightModeFactory.getMode(weight);
                        topK = score(weightMode,query, dIndex, corpus.getCorpusSize());

                    }
                    for (Integer i : topK.keySet()) {
                        System.out.println(corpus.getDocument(i).getFileTitle());
                        System.out.println(topK.get(i));
                    }
                }
                break;

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


    private static HashMap<Integer, Float> score(Strategy weighMode, String query, DiskPositionalIndex dIndex, Integer corpusSize) throws IOException {
        HashMap<Integer, Float> accumulators = new HashMap<>();
        for (String s : query.split(" ")) {
            for (Posting p : dIndex.getPostings(s)) {
                Float wqt = weighMode.getWqt(corpusSize, dIndex.getPostings(s).size());
                Float wdt = weighMode.getWdt(p.getPosition().size(),2,3,4);
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
            Float test = weighMode.getLd(dIndex.getWeight(i),5);
            Float acc = (float) (accumulators.get(i) / weighMode.getLd(dIndex.getWeight(i), 5));
            accumulators.put(i, acc);
        }
        return findTopK(accumulators, 10);
    }

    private static HashMap<Integer, Float> findTopK(HashMap<Integer, Float> acc, Integer k) {
        PriorityQueue<Map.Entry<Integer, Float>> pq = new PriorityQueue<>(k);
        HashMap<Integer, Float> results = null;
        for (Map.Entry<Integer, Float> entry : acc.entrySet()) {
            if (pq.size() < k)
                pq.offer(entry);
            else if (pq.peek().getValue() < entry.getValue()) {
                pq.poll();
                pq.offer(entry);
            }
        }
        while (!pq.isEmpty())
            results.put(pq.poll().getKey(), pq.poll().getValue());
        return results;
    }
}
