import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.ClassificationIndex;
import cecs429.index.Posting;
import cecs429.text.EnglishTokenStream;
import cecs429.text.ImprovedTokenProcessor;
import cecs429.text.TokenStream;

import java.nio.file.Paths;
import java.util.*;


public class IndexBuilder {

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Scanner sc = new Scanner(System.in);
        Index indexH;
        Index indexJ;
        Index indexM;

        System.out.println("Please enter the directory of the file: ");
        String directory = sc.nextLine();
        DocumentCorpus corpusH = DirectoryCorpus.loadDirectory(Paths.get(directory + "/HAMILTON"), ".json", ".txt");

        DocumentCorpus corpusJ = DirectoryCorpus.loadDirectory(Paths.get(directory + "/JAY"), ".json", ".txt");

        DocumentCorpus corpusM = DirectoryCorpus.loadDirectory(Paths.get(directory + "/MADISON"), ".json", ".txt");

        DocumentCorpus corpusD = DirectoryCorpus.loadDirectory(Paths.get(directory + "/DISPUTED"), ".json", ".txt");

        TreeSet<String> wordSets = new TreeSet<>();
        Map<String, Float> termScores;
        PriorityQueue<Map.Entry<String, Float>> pq = new PriorityQueue<>(((o1, o2) -> o2.getValue().compareTo(o1.getValue())));


        indexH = indexCorpus(corpusH, wordSets);
        indexJ = indexCorpus(corpusJ, wordSets);
        indexM = indexCorpus(corpusM, wordSets);

        Map<String, Index> categoryIndex = new HashMap<>();
        categoryIndex.put("HAMILTON", indexH);
        categoryIndex.put("JAY", indexJ);
        categoryIndex.put("MADISON", indexM);

        Map<String, DocumentCorpus> categoryCorpus = new HashMap<>();
        categoryCorpus.put("HAMILTON", corpusH);
        categoryCorpus.put("JAY", corpusJ);
        categoryCorpus.put("MADISON", corpusM);

        termScores = scoreTerms(wordSets, categoryIndex);
        for (Map.Entry<String, Float> entry : termScores.entrySet()) {
            if (entry.getValue() <= 0) {
                entry.setValue((float) 0);
            }
            pq.offer(entry);
        }

        Map<String, Float> Top50Term = new LinkedHashMap<>();
        for (int i = 0; i < 50; i++) {
            assert pq.peek() != null;
            String term = pq.peek().getKey();
            Float score = pq.poll().getValue();
            Top50Term.put(term, score);
            if (i == 9) { // print top10 terms
                for (Map.Entry<String, Float> entry : Top50Term.entrySet())
                    System.out.println(entry.toString());
            }
        }
        List<Map<String, Float>> termPtcScore = calculatePtc(categoryIndex, Top50Term.keySet());
        for (Document doc : corpusD.getDocuments()) {
            Set<String> vocab = new HashSet<>();
            TokenStream stream = new EnglishTokenStream(doc.getContent());
            Iterable<String> token = stream.getTokens();
            for (String t : token)
                vocab.add(t.replaceAll("\\W", "").toLowerCase());
            System.out.println(doc.getFileTitle() + " is mostly likely to be in category " + calculateClass(vocab, categoryIndex, termPtcScore));
        }
        List<Float> centroidH;
        List<Float> centroidJ;
        List<Float> centroidM;
        for(String s : wordSets){
            for(Index index : categoryIndex.values()){


            }

        }


    }

    private static Map<String, Float> scoreTerms(TreeSet<String> wordSets, Map<String, Index> categoryIndex) {
        Map<String, Float> scoreMap = new HashMap<>();
        for (Index category : categoryIndex.values()) {
            for (String word : wordSets) {
                float N11;
                float N1x = 0;
                float Nx1;
                float N10;
                float Nx0;
                float N01;
                float N0x;
                float N00;
                float N = 0;

                Nx1 = category.getDocCount();

                List<Integer> categoryID = new ArrayList<>();

                for (Posting p : category.getPostings(word)) {
                    categoryID.add(p.getDocumentId());
                }
                N11 = categoryID.size();

                // calucate the total number of the categories ABC contains term word
                for (Index cate : categoryIndex.values()) {
                    for (Posting p : cate.getPostings(word)) {
                        N1x = N1x + 1;
                    }
                    N = cate.getDocCount() + N;
                }

                N10 = N1x - N11;
                N01 = Nx1 - N11;

                N00 = N - N10 - N01 - N11;
                Nx0 = N10 + N00;
                N0x = N01 + N00;

                float Iscore = (N11 / N) * log2((N * N11) / (N1x * Nx1)) + (N10 / N) * (log2((N * N10) / (N1x * Nx0))) + (N01 / N) * (log2((N * N01) / (N0x * Nx1)))
                        + (N00 / N) * (log2((N * N00) / (N0x * Nx0)));
                if (Float.isNaN(Iscore)) {
                    Iscore = 0;
                }
                if (scoreMap.containsKey(word)) {
                    float s = scoreMap.get(word);
                    scoreMap.put(word, Math.max(s, Iscore));
                } else {
                    scoreMap.put(word, Iscore);
                }
            }
        }

        return scoreMap;
    }


    private static float log2(float num) {
        return (float) (Math.log(num) / Math.log(2));
    }

    private static List<Map<String, Float>> calculatePtc(Map<String, Index> categoryList, Set<String> top50Terms) {
        List<Map<String, Float>> result = new ArrayList<>();
        for (Index index : categoryList.values()) {
            Map<String, Float> termScore = new HashMap<>();
            float score;
            for (String s : top50Terms) {
                int termCount = 0;
                for (Posting p : index.getPostings(s)) {
                    termCount = termCount + p.getPosition().size();
                }
                score = (float) (termCount + 1) / (index.getTokens() + 50);
                termScore.put(s, score);
            }
            result.add(termScore);
        }
        return result;
    }

    private static String calculateClass(Set<String> testSetVocab, Map<String, Index> categoryIndex, List<Map<String, Float>> termPtcScore) {
        Map<String, Float> resultMap = new HashMap<>();

        int docAllClass = 0;
        for (Index index : categoryIndex.values()) {
            docAllClass += index.getDocCount();
        }
        int categoryId = 0;
        for (Map.Entry<String, Index> entry : categoryIndex.entrySet()) {
            float totalTermScore = (float) 0;
            float result;
            for (String s : testSetVocab) {
                if (termPtcScore.get(categoryId).containsKey(s)) {
                    totalTermScore += (float) Math.log(termPtcScore.get(categoryId).get(s));
                }
            }
            result = (float) Math.log(((float) categoryIndex.get(entry.getKey()).getDocCount() / docAllClass)) + totalTermScore;
            resultMap.put(entry.getKey(), result);
            categoryId++;
        }
        Float maximum = (float) -999;
        String category = null;
        for (Map.Entry<String, Float> entry : resultMap.entrySet()) {
            if (entry.getValue() > maximum) {
                maximum = entry.getValue();
                category = entry.getKey();
            }
        }
        return category;
    }

    private static Index indexCorpus(DocumentCorpus corpus, TreeSet<String> wordSets) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        ImprovedTokenProcessor processor = new ImprovedTokenProcessor();
        ClassificationIndex index = new ClassificationIndex();
        Set<String> totalTokens = new HashSet<>();
        for (Document sDocument : corpus.getDocuments()) {
            TokenStream stream = new EnglishTokenStream(sDocument.getContent());
            Iterable<String> token = stream.getTokens();
            int position = 1;
            for (String t : token) {
                totalTokens.add(t);
                t = t.replaceAll("\\W", "").toLowerCase();
                if(t.length()==0)
                    continue;
                List<String> word = processor.processToken(getStem(t));
                if (word.size() > 0) {
                    for (String s : word) {
                        if (wordSets != null) {
                            wordSets.add(s);
                        }
                        index.addTerm(s, sDocument.getId(), position);
                    }
                    position++;
                }
            }
        }
        index.setDocCount(corpus.getCorpusSize());
        index.setTokens(totalTokens.size());
        return index;
    }


    public static String getStem(String input) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        ImprovedTokenProcessor processor2 = new ImprovedTokenProcessor();
        return processor2.stem(input);
    }

}
