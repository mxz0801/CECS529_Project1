package cecs429.documents;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;

/**
 * Represents a document that is saved as a json file in the local file system.
 */
public class JsonFileDocument implements Json {
	private int mDocumentId;
	private Path mFilePath;
    private GsonDoc mJsonObject;
	/**
	 * Constructs a JsonFileDocument with the given document ID representing the file at the given
	 * absolute file path.
	 */
	public JsonFileDocument(int id, Path absoluteFilePath) {
		mDocumentId = id;
		mFilePath = absoluteFilePath;
	}
	
	@Override
	public Path getFilePath() {
		return mFilePath;
	}
	
	@Override
	public int getId() {
		return mDocumentId;
	}
	
	@Override
	public Reader getContent() {
		try {
			BufferedReader reader = Files.newBufferedReader(mFilePath);
			//convert to json object
			StringBuffer sb = new StringBuffer();
			String line = null;
			while((line = reader.readLine()) != null) {
				sb.append(line);
			}
			Gson gson=new Gson();
			mJsonObject = gson.fromJson(sb.toString(), GsonDoc.class);
			return Files.newBufferedReader(mFilePath); //TO REVISE: Bad way. It reads twice!
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String getFileTitle() {
		return mFilePath.getFileName().toString();
	}
	
	@Override
	public String getJsonTitle() {
		return mJsonObject.getTitle();
	}
	
	@Override
	public String getJsonBody() {
		return mJsonObject.getBody();
	}
	
	public static FileDocument loadTextFileDocument(Path absolutePath, int documentId) {
		return new JsonFileDocument(documentId, absolutePath);
	}
}
