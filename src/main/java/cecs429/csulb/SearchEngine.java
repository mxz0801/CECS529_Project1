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


	public List<GsonDoc> search (String dir, String input) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
		// TODO Auto-generated method stub
//		String abc ="dddddDHKJHF";
//		abc.toLowerCase();
//		System.out.println(abc);
//		List<Integer> result = new ArrayList<>();
//		result.add(1);
//		result.add(2);
//		List<Integer> bufferList = new ArrayList<>(result);
//		result.clear();
//		System.out.println(bufferList);
//		bufferList.add(333);
//		System.out.println(result);
//		System.out.println(bufferList);
		file.clear();
		//System.out.println("Please enter the directory of the file: ");
		String directory = dir;

		long startTime = System.currentTimeMillis();
		System.out.println("Timer started");
		DocumentCorpus corpusJs = DirectoryCorpus.loadJsonDirectory(Paths.get(directory), ".json",".txt");   //read json file
		//DocumentCorpus corpusTxt = DirectoryCorpus.loadTextDirectory(Paths.get(directory), ".txt");  //read txt file
//		DocumentCorpus combinedCorpus = DirectoryCorpus.loadDirctory(corpusJs,corpusTxt);

		//System.out.println(corpusJs.getCorpusSize());
		Index indexJs = indexCorpus(corpusJs) ;
		//Index indexTxt = indexCorpus(corpusTxt);
		long endTime = System.currentTimeMillis();
		System.out.println("It took " + (endTime - startTime) + " milliseconds to index");

			//System.out.print("Pleas enter the term to search for: ");
			String query = input;
//			if(query.equals("quit")) {
//				System.out.println("Exit the search.");
//			}else if(query.contains(":stem")){
				String stemToken = input;
				ImprovedTokenProcessor processor2 = new ImprovedTokenProcessor();
				String processedToken = processor2.stem(stemToken);
				//stem(stemToken,processedToken);
//				System.out.println(stemToken + "-->" + processedToken);
//

				//System.out.println(indexJs.getVocabulary());

				vocab(indexJs.getVocabulary());
				//System.out.println(indexJs.getVocabulary().size());

				try {
					String str = query.toLowerCase();
					BooleanQueryParser parser = new BooleanQueryParser();
					Query queryPosting = parser.parseQuery(str);
					for (Posting p : queryPosting.getPostings(indexJs)) {
//						System.out.println("Document: " + corpusJs.getDocument(p.getDocumentId()).getFileTitle());
						file.add(corpusJs.getDocument(p.getDocumentId()).getJson());
						//System.out.println(p.getPosition());
					}

				}catch (Exception e) {
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

			return file;
		}





	private static Index indexCorpus(DocumentCorpus corpus) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		ImprovedTokenProcessor processor = new ImprovedTokenProcessor();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		for(Document sDocument : corpus.getDocuments()) {
				TokenStream stream = new EnglishTokenStream(sDocument.getContent());
				//System.out.println("Indexing file " + sDocument.getFileTitle());
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



	public void setID(List<Integer> ID){
		this.ID = ID;
	}
	public List<Integer> getID(){
		return ID;
	}
	public void stem(String input) throws IllegalAccessException, InstantiationException, ClassNotFoundException {

		//processed.add(processedToken);
	}

	public String getStem(String input) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
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
