package cecs429.csulb;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.InvertedIndex;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenStream;

public class SearchEngine {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Scanner sc = new Scanner(System.in);
		String directory = sc.nextLine();
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory), ".json");
		System.out.println(corpus.getCorpusSize());
		Index index = indexCorpus(corpus) ;
		System.out.println(Paths.get("").toAbsolutePath());
	}
	private static Index indexCorpus(DocumentCorpus corpus) throws IOException {
		BasicTokenProcessor processor = new BasicTokenProcessor();
		InvertedIndex index = new InvertedIndex();
		for(Document sDocument : corpus.getDocuments()) {
				System.out.println("Indexing file " + sDocument.getFileTitle());
				TokenStream stream = new EnglishTokenStream(sDocument.getContent());
				Iterable<String> token = stream.getTokens();
				for(String t : token) {
					String word = processor.processToken(t);
					if (word.length() > 0) {
						index.addTerm(word, sDocument.getId());
					}
				}
				stream.close();
			}
		return index;
	}

}
