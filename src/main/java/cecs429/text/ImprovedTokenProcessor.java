package main.java.cecs429.text;

public class ImprovedTokenProcessor implements TokenProcessor {
	@Override
	public String processToken(String token) {
		return token.replaceAll("\\W", "").toLowerCase();
	}

}
