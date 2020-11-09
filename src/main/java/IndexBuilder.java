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
		dIndex.docWeight();

		System.out.println("Select modes: ");
		System.out.println("1. Boolean query mode");
		System.out.println("2. Ranked query mode");
		String mode = sc.nextLine();
		switch (Integer.parseInt(mode)){
			case 1:


			case 2:
				while(true) {
					HashMap<Integer, Float> topK;
					System.out.print("Pleas enter the term to search for: ");
					String query = sc.nextLine();
					if (query.equals("quit")) {
						System.out.println("Exit the search.");
						break;
					}
					else {
						topK = score(query, dIndex, corpus.getCorpusSize());
					}
					for(Integer i : topK.keySet()){
						System.out.println(corpus.getDocument(i).getFileTitle());
						System.out.println(topK.get(i));
					}
				}
				break;

		}
		System.out.println(dIndex.getPostings("me").size());

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
	private static HashMap<Integer, Float> score(String query, DiskPositionalIndex dIndex, Integer corpusSize) throws IOException {
		HashMap<Integer, Float> accumulators = new HashMap<>();
		for(String s : query.split(" ")){
			for(Posting p : dIndex.getPostings(s)){
				Float wqt = (float)Math.log(1 + corpusSize/ dIndex.getPostings(s).size());
				Float wdt = (float) (1 + Math.log(p.getPosition().size()));
				Float newWeight;
				if(accumulators.get(p.getDocumentId())==null){
					accumulators.put(p.getDocumentId(), wdt * wqt);
				}
				else{
					newWeight = accumulators.get(p.getDocumentId()) + wdt * wqt;
					accumulators.put(p.getDocumentId(), newWeight);
				}
			}
		}
		for(Integer i : accumulators.keySet()){
			Float acc = (float)(accumulators.get(i)/dIndex.getWeight(i));
			accumulators.put(i, acc);
		}

		return  findTopK(accumulators, 10);
	}
	private static HashMap<Integer, Float> findTopK(HashMap<Integer, Float> acc, Integer k){
		PriorityQueue<Map.Entry<Integer, Float>> pq = new PriorityQueue<>(k);
		HashMap<Integer, Float> results = null;
		for(Map.Entry<Integer, Float> entry: acc.entrySet()){
			if(pq.size() < k)
				pq.offer(entry);
			else if(pq.peek().getValue() < entry.getValue()){
				pq.poll();
				pq.offer(entry);
			}
		}
		while(!pq.isEmpty())
			results.put(pq.poll().getKey(), pq.poll().getValue());
		return results;
	}
}
