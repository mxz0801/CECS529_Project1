import java.io.*;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import cecs429.documents.*;
import cecs429.index.*;
import cecs429.text.*;
import cecs429.writer.DiskIndexWriter;

public class IndexBuilder {
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		System.out.println("Please enter the directory of the file: ");
		Scanner sc = new Scanner(System.in);
		String directory = sc.nextLine();
		DocumentCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get(directory), ".txt");   //read json file
		Index index = indexCorpus(corpus);
		System.out.println("Done!");
		DiskIndexWriter writer = new DiskIndexWriter();
		ConcurrentMap map = writer.writeIndex(index, Paths.get(directory));
		DiskPositionalIndex dIndex = new DiskPositionalIndex();
		dIndex.loadMap(map);
		dIndex.getPostings("1");

	}



	private static Index indexCorpus(DocumentCorpus corpus) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		TokenProcessor processor = new BasicTokenProcessor();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		for (Document sDocument : corpus.getDocuments()) {
			TokenStream stream = new EnglishTokenStream(sDocument.getContent());
			Iterable<String> token = stream.getTokens();
			int position = 1;
			for (String t : token) {
				String word = processor.processToken(t);

				index.addTerm(word, sDocument.getId(), position);
				position++;
			}
			stream.close();
		}
		return index;
	}
}
