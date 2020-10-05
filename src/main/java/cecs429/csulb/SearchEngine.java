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
		DocumentCorpus corpusJs = DirectoryCorpus.loadJsonDirectory(Paths.get(directory), ".json");   //read json file
		DocumentCorpus corpusTxt = DirectoryCorpus.loadTextDirectory(Paths.get(directory), ".txt");  //read txt file
		System.out.println(corpusJs.getCorpusSize());
		Index indexJs = indexCorpus(corpusJs) ;
		Index indexTxt = indexCorpus(corpusTxt) ;

		System.out.println(indexJs.getVocabulary());
		while(true){
			System.out.print("Pleas enter the term to search for: ");
			String query = sc.nextLine();
			if(query.equals("quit")) {
				System.out.println("Exit the search.");
				break;
			}
			else {
				try {
					String str = query.toLowerCase();
					BooleanQueryParser parser = new BooleanQueryParser();
					Query queryPosting = parser.parseQuery(str);
					for (Posting p : queryPosting.getPostings(indexJs)) {
						System.out.println("Document " + corpusJs.getDocument(p.getDocumentId()).getFileTitle());
						System.out.println(p.getPosition());
					}

				}catch (Exception e) {
				}
				try{
					String str = query.toLowerCase();
					BooleanQueryParser parser = new BooleanQueryParser();
					Query queryPosting = parser.parseQuery(str);
					for(Posting pTxt : queryPosting.getPostings(indexTxt)){
						System.out.println("Document " + corpusTxt.getDocument(pTxt.getDocumentId()).getFileTitle());
						System.out.println(pTxt.getPosition());
					}
				}catch (Exception e) {
				}

			}
			
		}
		sc.close();
	}

	private static Index indexCorpus(DocumentCorpus corpus) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		ImprovedTokenProcessor processor = new ImprovedTokenProcessor();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		for(Document sDocument : corpus.getDocuments()) {
				System.out.println("Indexing file " + sDocument.getFileTitle());
				TokenStream stream = new EnglishTokenStream(sDocument.getContent());



			Iterable<String> token = stream.getTokens();
				int position = 1;
				for(String t : token) {
					List<String> word = processor.processToken(t);
					if (word.size() > 0) {
						for(int i=0;i<word.size();i++){
							index.addTerm(word.get(i), sDocument.getId(), position);
							position++;
						}

					}
				}
				stream.close();
			}
		
		return index;
	}

}
