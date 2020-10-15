package cecs429.csulb;

import java.io.IOException;

import java.nio.file.Paths;
import java.util.*;

import cecs429.documents.*;
import cecs429.index.*;
import cecs429.query.BooleanQueryParser;
import cecs429.query.Query;
import cecs429.text.*;


public class SearchEngine {
	private List<String> result = new ArrayList<>();
	private List<Integer> ID = new ArrayList<>();
	private List<String> word = new ArrayList<>();
	private KgramIndex kGramIndex = new KgramIndex();
	private List<GsonDoc> file = new ArrayList<>();
	String directory;
	private DocumentCorpus corpusJs;
	private Index indexJs;

	public Index indexing(String dir) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		if (!directory.equals(dir)) {
			directory = dir;
			long startTime = System.currentTimeMillis();

			System.out.println("Timer started");
			corpusJs = DirectoryCorpus.loadJsonDirectory(Paths.get(directory), ".json", ".txt");   //read json file

			indexJs = indexCorpus(corpusJs, kGramIndex);
			long endTime = System.currentTimeMillis();
			System.out.println("It took " + (endTime - startTime) + " milliseconds to index");
			vocab(indexJs.getVocabulary());
		}
		return indexJs;

	}
	public List<GsonDoc> search (Index indexJs, String input) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
		// TODO Auto-generated method stub
		file.clear();
		String query = input;
		try {
			query = processQuery(query);
			System.out.println(query);
			BooleanQueryParser parser = new BooleanQueryParser();
			Query queryPosting = parser.parseQuery(query);
			if (query.contains("*")) {
				List<Posting> wildcardResult = new ArrayList<>(queryPosting.getPostings(indexJs, kGramIndex));
				for (Posting p : wildcardResult) {
					System.out.println("Document: " + corpusJs.getDocument(p.getDocumentId()).getFileTitle());
					file.add(corpusJs.getDocument(p.getDocumentId()).getJson());
				}
				System.out.println(wildcardResult.size());
			} else {
				for (Posting p : queryPosting.getPostings(indexJs)) {
					System.out.println("Document: " + corpusJs.getDocument(p.getDocumentId()).getFileTitle());
					file.add(corpusJs.getDocument(p.getDocumentId()).getJson());
				}
				System.out.println(queryPosting.getPostings(indexJs).size());
			}
		} catch (Exception e) {
		}
		return file;

	}

	private static Index indexCorpus(DocumentCorpus corpus, KgramIndex kgramIndex) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		Set<String> vocab = new HashSet<>();
		ImprovedTokenProcessor processor = new ImprovedTokenProcessor();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		for(Document sDocument : corpus.getDocuments()) {
			TokenStream stream = new EnglishTokenStream(sDocument.getContent());
			Iterable<String> token = stream.getTokens();
			int position = 1;
			for (String t : token) {
				vocab.add(t.replaceAll("\\W", "").toLowerCase());
				List<String> word = processor.processToken(t);
				if (word.size() > 0) {
					for (String s : word)
						index.addTerm(s, sDocument.getId(), position);
					position++;
				}
			}
			stream.close();
		}

		for(String s: vocab){
			kgramIndex.addTerm(s);
		}
		
		return index;
	}

	public void setID(List<Integer> ID){
		this.ID = ID;
	}
	public List<Integer> getID(){
		return ID;
	}
	public static String processQuery(String query) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		String[] str = query.split(" ");
		StringBuilder newQuery = new StringBuilder();
		for(String s : str){
			if(s.equals(str[str.length-1])) {
				if(s.contains("\"")){
					s = s.substring(0, s.length() -1);
					newQuery.append(getStem(s)).append("\"");
				}
				else if(s.contains("*"))
					newQuery.append(s);
				else
					newQuery.append(getStem(s));
			}
			else if(s.contains("*"))
				newQuery.append(s).append(" ");
			else
				newQuery.append(getStem(s)).append(" ");
		}
		return newQuery.toString();

	}

	public static String getStem(String input) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		ImprovedTokenProcessor processor2 = new ImprovedTokenProcessor();
		return processor2.stem(input);
	}
	public void vocab(List<String> word){
		if(word.size()>1000)
		{
			word = word.subList(0,100);
		}
		this.word = word;
	}
	public List<String> getVocab(){
			return word;
	}
}

//	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
//	// TODO Auto-generated method stub
//		System.out.println("Please enter the directory of the file: ");
//		Scanner sc = new Scanner(System.in);
//		String directory = sc.nextLine();
//		long startTime = System.currentTimeMillis();
//
//		System.out.println("Timer started");
//		DocumentCorpus corpusJs = DirectoryCorpus.loadJsonDirectory(Paths.get(directory), ".json", ".txt");   //read json file
//
//		KgramIndex kGramIndex = new KgramIndex();
//		Index indexJs = indexCorpus(corpusJs, kGramIndex);
//		//Index indexTxt = indexCorpus(corpusTxt);
//		long endTime = System.currentTimeMillis();
//		System.out.println("It took " + (endTime - startTime) + " milliseconds to index");
//
//			while(true){
//			System.out.print("Pleas enter the term to search for: ");
//			String query = sc.nextLine();
//			if(query.equals("quit")) {
//				System.out.println("Exit the search.");
//				break;
//			}else if(query.contains(":stem")){
//				String[] spliter = query.split(" ");
//				String stemToken = spliter[1];
//				ImprovedTokenProcessor processor2 = new ImprovedTokenProcessor();
//				String processedToken = processor2.stem(stemToken);
//				System.out.println(stemToken + "-->" + processedToken);
//			}else if(query.equals(":vocab")){
//				for(int i=0;i<1000;i++){
//					System.out.println(indexJs.getVocabulary().get(i));
//				}
//				System.out.println(indexJs.getVocabulary().size());
//			}
//			else{
//			System.out.println(indexJs.getVocabulary());
//
//		try {
//		query = processQuery(query);
//		System.out.println(query);
//		BooleanQueryParser parser = new BooleanQueryParser();
//		Query queryPosting = parser.parseQuery(query);
//		if (query.contains("*")) {
//			List<Posting> wildcardResult = new ArrayList<>(queryPosting.getPostings(indexJs, kGramIndex));
//			for (Posting p : wildcardResult) {
//				System.out.println("Document: " + corpusJs.getDocument(p.getDocumentId()).getFileTitle());
//
//			}
//			System.out.println(wildcardResult.size());
//		} else {
//			for (Posting p : queryPosting.getPostings(indexJs)) {
//				System.out.println("Document: " + corpusJs.getDocument(p.getDocumentId()).getFileTitle());
//
//			}
//			System.out.println(queryPosting.getPostings(indexJs).size());
//
//		}
//	} catch (Exception e) {
//	}
//			}
//	System.out.println("Done");
//		}
//
//}