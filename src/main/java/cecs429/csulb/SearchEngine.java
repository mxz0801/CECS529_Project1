package cecs429.csulb;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import cecs429.documents.*;
import cecs429.index.*;
import cecs429.query.BooleanQueryParser;
import cecs429.query.Query;
import cecs429.text.*;


public class SearchEngine {
    static int corpusIndex = 0;


    public static void main(String[] args) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
		// TODO Auto-generated method stub
		String abc ="dddddDHKJHF";
		abc.toLowerCase();
		System.out.println(abc);
		List<Integer> result = new ArrayList<>();
		result.add(1);
		result.add(2);
		List<Integer> bufferList = new ArrayList<>(result);
		result.clear();
		System.out.println(bufferList);
		bufferList.add(333);
		System.out.println(result);
		System.out.println(bufferList);
		System.out.println("Please enter the directory of the file: ");
		Scanner sc = new Scanner(System.in);
		String directory = sc.nextLine();


        long startTime = System.currentTimeMillis();
		System.out.println("Timer started");
		DocumentCorpus corpusJs = DirectoryCorpus.loadJsonDirectory(Paths.get(directory), ".json");   //read json file
		DocumentCorpus corpusTxt = DirectoryCorpus.loadTextDirectory(Paths.get(directory), ".txt");  //read txt file
		//System.out.println(corpusJs.getCorpusSize());
		Index indexJs = indexCorpus(corpusJs,corpusTxt) ;
		long endTime = System.currentTimeMillis();
		System.out.println("It took " + (endTime - startTime) + " milliseconds to index");
		while(true){
			System.out.print("Pleas enter the term to search for: ");
			String query = sc.next();
			if(query.equals("quit")) {
				System.out.println("Exit the search.");
				break;
			}else if(query.equals(":stem")){
				String stemToken = sc.next();
				ImprovedTokenProcessor processor2 = new ImprovedTokenProcessor();
				String processedToken = processor2.stem(stemToken);
				System.out.println(stemToken + "-->" + processedToken);
			}else if(query.equals(":vocab")){
				for(int i=0;i<100;i++){
					System.out.println(indexJs.getVocabulary().get(i));
				}
				System.out.println(indexJs.getVocabulary().size());
			}
			else {
				try {
				    boolean TxtCorpus = false;
                    boolean JsCorpus = false;
                    String str = query.toLowerCase();
					BooleanQueryParser parser = new BooleanQueryParser();
					Query queryPosting = parser.parseQuery(str);
					for (Posting p : queryPosting.getPostings(indexJs)) {
					   for(int i = 0; i < p.getPosition().size();i++){
                           if(p.getPosition().get(i)>corpusIndex){
                               TxtCorpus = true;
                           }else{
                               JsCorpus = true;
                           }
                       }
                        if(TxtCorpus){
                            System.out.println("Document: " + corpusTxt.getDocument(p.getDocumentId()).getFileTitle());
                            System.out.println(p.getPosition());
                        }
                        if(JsCorpus){
                            System.out.println("Document: " + corpusJs.getDocument(p.getDocumentId()).getFileTitle());
                            System.out.println(p.getPosition());
                        }
					}
				}catch (Exception e) {
				    System.out.println("Didn't find in the corpus, please try again");
				}
//				try{
//					String str = query.toLowerCase();
//					BooleanQueryParser parser = new BooleanQueryParser();
//					Query queryPosting = parser.parseQuery(str);
//					for(Posting pTxt : queryPosting.getPostings(indexTxt)){
//						System.out.println("Document: " + corpusTxt.getDocument(pTxt.getDocumentId()).getFileTitle());
//						System.out.println(pTxt.getPosition());
//					}
//				}catch (Exception e) {
//				}
			}

		}
		sc.close();
	}

	private static Index indexCorpus(DocumentCorpus corpus,DocumentCorpus corpus2) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		ImprovedTokenProcessor processor = new ImprovedTokenProcessor();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
        int position = 1;

        for(Document sDocument : corpus.getDocuments() ){
				TokenStream stream = new EnglishTokenStream(sDocument.getContent());
				System.out.println("Indexing file " + sDocument.getFileTitle());

			Iterable<String> token = stream.getTokens();
				for(String t : token) {
					List<String> word = processor.processToken(t);
					if (word.size() > 0) {
						for(int i=0;i<word.size();i++){
							index.addTerm(word.get(i), sDocument.getId(), position);
							position++;
						}

					}
				}
                corpusIndex = position;
				stream.close();
			}
        for(Document TDocument : corpus2.getDocuments() ){
            TokenStream stream = new EnglishTokenStream(TDocument.getContent());
            System.out.println("Indexing file " + TDocument.getFileTitle());

            Iterable<String> token = stream.getTokens();
            for(String t : token) {
                List<String> word = processor.processToken(t);
                if (word.size() > 0) {
                    for(int i=0;i<word.size();i++){
                        index.addTerm(word.get(i), TDocument.getId(), position);
                        position++;
                    }

                }
            }
            stream.close();
        }

		return index;
	}


}
