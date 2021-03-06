package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import util.Utility;
import view.SearchContent;

public class NewSearchRecord extends Thread implements Utility{
	private double requiredTime;
	private String keyWord;
	private Semaphore semaphore;
	private StringBuffer resultString;
	private SearchContent SearchContentRequired;
	private NewSearch Search;

	public NewSearchRecord(String pKeyWord){
		this.requiredTime = 0.0;
		this.keyWord = pKeyWord;
		semaphore = new Semaphore(1);
		SearchContentRequired = new SearchContent();
		Search = new NewSearch();
	}

	public void run(){
		try {
			try {
				semaphore.acquire();
				double startTime = System.nanoTime();
				sendGet();
				double endTime = System.nanoTime();
				requiredTime = (endTime - startTime) / 1000000;
				
				Search.addObserver(SearchContentRequired);
				Search.searchComplete(keyWord, resultString.toString(), requiredTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally{
				semaphore.release();
			}					
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendGet () throws ClientProtocolException, IOException{
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet request = new HttpGet(URL_ADRESS + keyWord);
		request.addHeader("User-Agent", USER_AGENT);
		CloseableHttpResponse response1 = httpclient.execute(request);

		HttpEntity entity1 = response1.getEntity();

		try {	
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(entity1.getContent()));

			resultString = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				resultString.append(line);
			}	
		} finally {
			EntityUtils.consume(entity1);
			response1.close();
			httpclient.close();
		}
	}
	
	public SearchContent getSearchContentRequired() {
		return SearchContentRequired;
	}

	public void setSearchContentRequired(SearchContent searchContentRequired) {
		SearchContentRequired = searchContentRequired;
	}
}
