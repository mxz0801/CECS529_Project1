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
	private List<GsonDoc> file = new ArrayList<>();

	List<String> processed = new ArrayList<>();


	//public List<GsonDoc> search (String dir, String input) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		// TODO Auto-generated method stub
		//	file.clear();
		ImprovedTokenProcessor processor = new ImprovedTokenProcessor();
		String word = "ddd" ;
		//String word1 = processor.processToken(word);
		//System.out.println(word1);
		System.out.println("Please enter the directory of the file: ");
		//	String directory = dir;
		Scanner sc = new Scanner(System.in);
		String directory = sc.nextLine();
		long startTime = System.currentTimeMillis();

		System.out.println("Timer started");
		DocumentCorpus corpusJs = DirectoryCorpus.loadJsonDirectory(Paths.get(directory), ".json", ".txt");   //read json file
		//DocumentCorpus corpusTxt = DirectoryCorpus.loadTextDirectory(Paths.get(directory), ".txt");  //read txt file
//		DocumentCorpus combinedCorpus = DirectoryCorpus.loadDirctory(corpusJs,corpusTxt);

		//System.out.println(corpusJs.getCorpusSize());
		KgramIndex kGramIndex = new KgramIndex();
		Index indexJs = indexCorpus(corpusJs, kGramIndex);
		//Index indexTxt = indexCorpus(corpusTxt);
		long endTime = System.currentTimeMillis();
		System.out.println("It took " + (endTime - startTime) + " milliseconds to index");

		while(true){
			System.out.print("Pleas enter the term to search for: ");
			String query = sc.nextLine();
			if(query.equals("quit")) {
				System.out.println("Exit the search.");
				break;
			}else if(query.contains(":stem")){
				String[] spliter = query.split(" ");
				String stemToken = spliter[1];
				ImprovedTokenProcessor processor2 = new ImprovedTokenProcessor();
				String processedToken = processor2.stem(stemToken);
				System.out.println(stemToken + "-->" + processedToken);
			}else if(query.equals(":vocab")){
				for(int i=0;i<1000;i++){
					System.out.println(indexJs.getVocabulary().get(i));
				}
				System.out.println(indexJs.getVocabulary().size());
			}
			else{
				//System.out.println(indexJs.getVocabulary());

				//	vocab(indexJs.getVocabulary());
				//System.out.println(indexJs.getVocabulary().size());
				try {
					String[] str = query.split(" ");
					String newQuery = "";
					for(String s : str){
						if(s.equals(str[str.length-1]))
							newQuery += getStem(s);
						else
							newQuery += getStem(s) + " ";
					}
					System.out.println(newQuery);
					BooleanQueryParser parser = new BooleanQueryParser();
					Query queryPosting = parser.parseQuery(newQuery);
					if (newQuery.contains("*")) {
						for (Posting p : queryPosting.getPostings(indexJs, kGramIndex)) {
							System.out.println("Document: " + corpusJs.getDocument(p.getDocumentId()).getFileTitle());
							System.out.println(queryPosting.getPostings(indexJs, kGramIndex).size());

						}
					} else {
						for (Posting p : queryPosting.getPostings(indexJs)) {
							System.out.println("Document: " + corpusJs.getDocument(p.getDocumentId()).getFileTitle());
						}
						System.out.println(queryPosting.getPostings(indexJs).size());
					}
				} catch (Exception e) {
				}
			}
			System.out.println("Done");
		}
			//return file;

	}




	private static Index indexCorpus(DocumentCorpus corpus, KgramIndex kgramIndex) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		Set<String> vocab = new HashSet<>();
		ImprovedTokenProcessor processor = new ImprovedTokenProcessor();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		for(Document sDocument : corpus.getDocuments()) {
			TokenStream stream = new EnglishTokenStream(sDocument.getContent());
			//System.out.println("Indexing file " + sDocument.getFileTitle());
			Iterable<String> token = stream.getTokens();
			int position = 1;
			for (String t : token) {
				vocab.add(t.replaceAll("\\W", "").toLowerCase());
				List<String> word = processor.processToken(t);
				if (word.size() > 0) {
					for (int i = 0; i < word.size(); i++)
						index.addTerm(word.get(i), sDocument.getId(), position);
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
//	public void stem(String input) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
//
//		//processed.add(processedToken);
//	}

	public static String getStem(String input) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		ImprovedTokenProcessor processor2 = new ImprovedTokenProcessor();
		String processedToken = processor2.stem(input);
		return processedToken;
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
