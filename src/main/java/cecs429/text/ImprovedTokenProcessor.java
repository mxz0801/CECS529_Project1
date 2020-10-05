package cecs429.text;

import org.tartarus.snowball.SnowballStemmer;

import java.util.*;

public class ImprovedTokenProcessor implements TokenProcessor {
	@Override
	public List<String> processToken(String token) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		//Class stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
		//SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
		List<String> processedToken = new ArrayList<>();
		if(token.contains("-")){
			processedToken.add(token.replaceAll("\\-",""));
			String[] temp = token.split("-");
			for(int i=0;i<temp.length;i++){
				processedToken.add(temp[i]);
			}
		}else{
			processedToken.add(token);
		}
		int size = processedToken.size();
		for(int i=0;i<size;i++){
			String tempProcess = processedToken.get(i).replaceAll("\\W", "")
					.replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", "").replaceAll("['\"]", "").toLowerCase();
			//tempProcess = tempProcess.replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", "").toLowerCase();
			//tempProcess = tempProcess.replaceAll("['\"]", "");
			//stemmer.setCurrent(tempProcess);
			//stemmer.stem();
			processedToken.remove(i);
			processedToken.add(i,tempProcess);
			processedToken.add(stem(tempProcess));
		}
		//all non-alphanumeric characters from the beginning and end of the token

		//processedToken.add(processedToken.replaceAll("['\"]", ""));
		//remove apostropes and quotation marks


		return processedToken;


	}


	public String stem(String token) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		Class stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
		SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
		stemmer.setCurrent(token);
		stemmer.stem();
		return stemmer.getCurrent();
	}

}
