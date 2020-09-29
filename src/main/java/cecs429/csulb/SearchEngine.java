package cecs429.csulb;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import cecs429.documents.*;
import cecs429.index.*;
import cecs429.text.*;


public class SearchEngine {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Please enter the directory of the file: ");
		Scanner sc = new Scanner(System.in);
		String directory = sc.nextLine();
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory), ".json");
		System.out.println(corpus.getCorpusSize());
		Index index = indexCorpus(corpus) ;
		System.out.println(index.getVocabulary());
		while(true){
			System.out.print("Pleas enter the term to search for: ");
			String query = sc.nextLine();
			if(query.equals("quit")) {
				System.out.println("Exit the search.");
				break;
			}
			else {
				for (Posting p : index.getPostings(query)) {
					System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getFileTitle());
					System.out.println(p.getPosition());
				}
			}
			
		}
		sc.close();
	}
	private static Index indexCorpus(DocumentCorpus corpus) throws IOException {
		BasicTokenProcessor processor = new BasicTokenProcessor();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		for(Document sDocument : corpus.getDocuments()) {
				System.out.println("Indexing file " + sDocument.getFileTitle());
				TokenStream stream = new EnglishTokenStream(sDocument.getContent());
				Iterable<String> token = stream.getTokens();
				int position = 1;
				for(String t : token) {
					String word = processor.processToken(t);
					if (word.length() > 0) {
						index.addTerm(word, sDocument.getId(), position);
						position++;
					}
				}
				stream.close();
			}
		
		return index;
	}

}
