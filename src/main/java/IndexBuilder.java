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
                System.out.println("Timer started");
                indexH = indexCorpus(corpusH, wordSets);
                indexJ = indexCorpus(corpusJ, wordSets);
                indexM = indexCorpus(corpusM, wordSets);
                indexD = indexCorpus(corpusD,null);

                scores = Tscore(indexD,indexM,scores);
                scores = Tscore(indexD,indexH,scores);
                scores = Tscore(indexD,indexJ,scores);

                for(Map.Entry<String,Float> entry:scores.entrySet()){
                    if(entry.getValue()>0){
                    }else{
                        entry.setValue((float) 0);
                    }
                    pq.offer(entry);
                }
                List<String> Tterm = new ArrayList<>();
                for(int i = 0; i< 10; i++){
                    System.out.println(pq.peek().getValue());
                    Tterm.add(pq.poll().getKey());
                }
                System.out.println(wordSets.size());
                System.out.println(Tterm.toString());
                System.out.println(scores.get("upon"));

        }
    }

    private static Map<String, Float> Tscore(Index termIndex, Index categoryIndex, Map<String, Float> scoreMap){
        List<String> termIndexVocabulary= termIndex.getVocabulary();
        for(int i = 0; i < termIndexVocabulary.size(); i++){
            float N11 = 0;
            float N1x=0;
            float Nx1=0;
            float N10=0;
            float Nx0=0;
            float N01=0;
            float N0x=0;
            float N00=0;
            float N=0;
            String term = termIndexVocabulary.get(i);
            int termDocNum = termIndex.getDocNum();
            int categoryDocNum = categoryIndex.getDocNum();
            List<Integer> termID = new ArrayList<>();
            List<Integer> categoryID = new ArrayList<>();
            for(Posting p:termIndex.getPostings(term)){
                termID.add(p.getDocumentId());
            }
            N1x = termID.size();
            for(Posting p :categoryIndex.getPostings(term)){
                categoryID.add(p.getDocumentId());
            }
            Nx1 = categoryID.size();
            for(int t = 0; t<termID.size();t++){
                for(int c = 0; c<categoryID.size();c++){
                    if(termID.get(t)==categoryID.get(c)){
                        N11 = N11+1;
                    }
                }
            }

            N10 = termID.size()-N11;
            N01 = categoryID.size()-N11;
            N = termDocNum + categoryDocNum;
            N00 = N - N10 - N01 - N11;
            Nx0 = N10 + N00;
            N0x = N01 + N00;

            float Iscore = (N11/N)*log2((N*N11)/(N1x*Nx1)) + (N10/N)*(log2((N*N10)/(N1x*Nx0))) + (N01/N)*(log2((N*N01)/(N0x*Nx1)))
                    + (N00/N)*(log2((N*N00)/(N0x*Nx0)));
            if(Iscore > 0){

            }else{
                Iscore = 0;
            }
            if(scoreMap.containsKey(term)){
                float s = scoreMap.get(term);
                scoreMap.put(term,Math.max(s,Iscore));
            }else{
                scoreMap.put(term,Iscore);
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
        int DocumentCount = 0;
        for (Document sDocument : corpus.getDocuments()) {
            DocumentCount++;
            TokenStream stream = new EnglishTokenStream(sDocument.getContent());
            Iterable<String> token = stream.getTokens();
            int position = 1;
            for (String t : token) {
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
        index.addDocumentCount(DocumentCount);
        return index;
    }


    public static String getStem(String input) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        ImprovedTokenProcessor processor2 = new ImprovedTokenProcessor();
        return processor2.stem(input);
    }

}
