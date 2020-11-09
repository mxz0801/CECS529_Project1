import java.io.*;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import cecs429.documents.*;
import cecs429.index.*;
import cecs429.query.BooleanQueryParser;
import cecs429.query.Query;
import cecs429.text.*;
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
                            System.out.println(index.getVocabulary().get(i));
                        }
                        System.out.println(index.getVocabulary().size());
                    } else {
                        System.out.println(index.getVocabulary());

                        try {
                            query = processQuery(query);
                            System.out.println(query);
                            BooleanQueryParser parser = new BooleanQueryParser();
                            Query queryPosting = parser.parseQuery(query);
                            if (query.contains("*")) {
                                List<Posting> wildcardResult = new ArrayList<>(queryPosting.getPostings(index, kGramIndex));
                                for (Posting p : wildcardResult) {
                                    System.out.println("Document: " + corpus.getDocument(p.getDocumentId()).getFileTitle());

                                }
                                System.out.println(wildcardResult.size());
                            } else {
                                for (Posting p : queryPosting.getPostings(index)) {
                                    System.out.println("Document: " + corpus.getDocument(p.getDocumentId()).getFileTitle());

                                }
                                System.out.println(queryPosting.getPostings(index).size());

                            }
                        } catch (Exception e) {
                        }
                    }
                    System.out.println("Done");
                }


            case 2:
                while (true) {
                    HashMap<Integer, Float> topK;
                    System.out.print("Pleas enter the term to search for: ");
                    String query = sc.nextLine();
                    if (query.equals("quit")) {
                        System.out.println("Exit the search.");
                        break;
                    } else {
                        topK = score(query, dIndex, corpus.getCorpusSize());
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

    public static String processQuery(String query) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        String[] str = query.split(" ");
        StringBuilder newQuery = new StringBuilder();
        for (String s : str) {
            if (s.equals(str[str.length - 1])) {
                if (s.contains("\"")) {
                    s = s.substring(0, s.length() - 1);
                    newQuery.append(getStem(s)).append("\"");
                } else if (s.contains("*"))
                    newQuery.append(s);
                else
                    newQuery.append(getStem(s));
            } else if (s.contains("*"))
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

    private static HashMap<Integer, Float> score(String query, DiskPositionalIndex dIndex, Integer corpusSize) throws IOException {
        HashMap<Integer, Float> accumulators = new HashMap<>();
        for (String s : query.split(" ")) {
            for (Posting p : dIndex.getPostings(s)) {
                Float wqt = (float) Math.log(1 + corpusSize / dIndex.getPostings(s).size());
                Float wdt = (float) (1 + Math.log(p.getPosition().size()));
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
            Float acc = (float) (accumulators.get(i) / dIndex.getWeight(i));
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
