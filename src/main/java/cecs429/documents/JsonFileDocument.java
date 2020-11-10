package cecs429.documents;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Represents a document that is saved as a json file in the local file system.
 */
public class JsonFileDocument implements FileDocument{
	private int mDocumentId;
	private Path mFilePath;
    private GsonDoc gsonDoc;
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
	public  Reader getContent() {
		try {
			Gson gson=new Gson();

			Reader reader = Files.newBufferedReader(mFilePath);

			gsonDoc = gson.fromJson(reader,GsonDoc.class);
			gsonDoc.setFileName(mFilePath.getFileName().toString());
			reader.close();
			return Files.newBufferedReader(mFilePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getFileTitle() {
		return gsonDoc.getTitle() + " ("+mFilePath.getFileName().toString()+ ")";
	}



	public String getJsonTitle() {
		return gsonDoc.getTitle();
	}
	@Override
	public GsonDoc getJson() {
		return gsonDoc;
	}

	public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
		return new JsonFileDocument(documentId, absolutePath);
	}
}
