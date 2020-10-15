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
			Collections.addAll(processedToken, temp);
		}else{
			processedToken.add(token);
		}
		int size = processedToken.size();
		for(int i=0;i<size;i++){
			String tempProcess = processedToken.get(i).replaceAll("\\W", "")
					.replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", "").replaceAll("['\"]", "").toLowerCase();
			processedToken.remove(i);
			processedToken.add(i,tempProcess);
			if(!stem(tempProcess).equals(tempProcess))
				processedToken.add(stem(tempProcess));
		}
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
