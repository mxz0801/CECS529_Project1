import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.index.weightPosting;
import cecs429.text.EnglishTokenStream;
import cecs429.text.ImprovedTokenProcessor;
import cecs429.text.TokenStream;
import org.jetbrains.annotations.NotNull;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;



public class IndexBuilder {

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        Scanner sc = new Scanner(System.in);
        Index indexH = null;
        Index indexJ = null;
        Index indexM = null;
        Index indexD = null;

        System.out.println("Please enter the directory of the file: ");
        String directory = sc.nextLine();
        DocumentCorpus corpusH = DirectoryCorpus.loadDirectory(Paths.get(directory + "/HAMILTON"), ".json", ".txt");

        DocumentCorpus corpusJ = DirectoryCorpus.loadDirectory(Paths.get(directory + "/JAY"), ".json", ".txt");

        DocumentCorpus corpusM = DirectoryCorpus.loadDirectory(Paths.get(directory + "/MADISON"), ".json", ".txt");

        DocumentCorpus corpusD = DirectoryCorpus.loadDirectory(Paths.get(directory + "/DISPUTED"), ".json", ".txt");

        TreeSet<String> wordSets = new TreeSet<>();
        Map<String, Float> scores = new HashMap<>();
        PriorityQueue<Map.Entry<String, Float>> pq = new PriorityQueue<>(((o1, o2) -> o2.getValue().compareTo(o1.getValue())));


        System.out.println("1. Build index ");
        String choice = sc.nextLine();
        switch (Integer.parseInt(choice)) {
            case 1:
                indexH = indexCorpus(corpusH, wordSets);
                indexJ = indexCorpus(corpusJ, wordSets);
                indexM = indexCorpus(corpusM, wordSets);
                indexD = indexCorpus(corpusD,null);

                List<Index> categoryIndex = new ArrayList<>();
                categoryIndex.add(indexM);
                categoryIndex.add(indexH);
                categoryIndex.add(indexJ);

                scores = Tscore(wordSets,categoryIndex);
                //scores = Tscore(wordSets,indexH,scores);
                //scores = Tscore(wordSets,indexJ,scores);

                for(Map.Entry<String,Float> entry:scores.entrySet()){
                    if(entry.getValue()>0){
                    }else{
                        entry.setValue((float) 0);
                    }
                    pq.offer(entry);
                }
                Map<String,Float> Top10Term = new LinkedHashMap<>();
                for(int i = 0; i< 10; i++){
                    String term = pq.peek().getKey();
                    Float score = pq.poll().getValue();
                    Top10Term.put(term,score);
                }

                System.out.println(Top10Term.toString());


        }
    }

    private static Map<String, Float> Tscore(TreeSet<String> wordSets, List<Index> categoryList){
        Map<String, Float> scoreMap = new HashMap<>();
        for(Index category: categoryList){
            for(String word: wordSets){
                float N11 = 0;
                float N1x=0;
                float Nx1=0;
                float N10=0;
                float Nx0=0;
                float N01=0;
                float N0x=0;
                float N00=0;
                float N=0;

            int categoryDocNum = category.getDocNum();
            Nx1 = categoryDocNum;

            List<Integer> categoryID = new ArrayList<>();

                for(Posting p :category.getPostings(word)){
                    categoryID.add(p.getDocumentId());
                }
                N11 = categoryID.size();

                // calucate the total number of the categories ABC contains term word
                for(Index cate: categoryList){
                    for(Posting p :cate.getPostings(word)){
                        N1x = N1x+1;
                    }
                    N = cate.getDocNum()+N;
                }

                N10 = N1x - N11;
                N01 = Nx1 - N11;

                N00 = N - N10 - N01 - N11;
                Nx0 = N10 + N00;
                N0x = N01 + N00;

                float Iscore = (N11/N)*log2((N*N11)/(N1x*Nx1)) + (N10/N)*(log2((N*N10)/(N1x*Nx0))) + (N01/N)*(log2((N*N01)/(N0x*Nx1)))
                        + (N00/N)*(log2((N*N00)/(N0x*Nx0)));
                if(Float.isNaN(Iscore)){
                    Iscore = 0;
                }
                if(scoreMap.containsKey(word)){
                    float s = scoreMap.get(word);
                    scoreMap.put(word,Math.max(s,Iscore));
                }else{
                    scoreMap.put(word,Iscore);
                }
            }
        }

        return scoreMap;
    }

    private static float log2(float num){
        return (float) (Math.log(num)/Math.log(2));
    }

    private static Index indexCorpus(DocumentCorpus corpus, TreeSet<String> wordSets) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        ImprovedTokenProcessor processor = new ImprovedTokenProcessor();
        PositionalInvertedIndex index = new PositionalInvertedIndex();
        for (Document sDocument : corpus.getDocuments()) {
            TokenStream stream = new EnglishTokenStream(sDocument.getContent());
            Iterable<String> token = stream.getTokens();
            int position = 1;
            for (String t : token) {
                t=t.replaceAll("\\W", "").toLowerCase();
                List<String> word = processor.processToken(getStem(t));
                if (word.size() > 0) {
                    for (String s : word) {
                        if(wordSets!=null){
                            wordSets.add(s);
                        }
                        index.addTerm(s, sDocument.getId(), position);
                    }
                    position++;
                }
            }
        }
        index.setDocumentCount(corpus.getCorpusSize());
        return index;
    }


    public static String getStem(String input) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        ImprovedTokenProcessor processor2 = new ImprovedTokenProcessor();
        return processor2.stem(input);
    }

}
