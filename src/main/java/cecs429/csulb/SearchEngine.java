package cecs429.csulb;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import cecs429.documents.*;
import cecs429.index.*;
import cecs429.text.*;


public class SearchEngine {

	public static void main(String[] args) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
		// TODO Auto-generated method stub
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
					for (Posting p : indexJs.getPostings(query)) {
						System.out.println("Document " + corpusJs.getDocument(p.getDocumentId()).getFileTitle());
						System.out.println(p.getPosition());
					}
					for(Posting pTxt:indexTxt.getPostings(query)){
						System.out.println("Document " + corpusTxt.getDocument(pTxt.getDocumentId()).getFileTitle());
						System.out.println(pTxt.getPosition());
					}
				}catch (Exception e){
					System.out.println("Doesn't exist");
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
