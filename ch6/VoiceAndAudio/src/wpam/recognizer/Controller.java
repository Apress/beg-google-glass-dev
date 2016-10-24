package wpam.recognizer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.os.AsyncTask;
import android.util.Log;


public class Controller 
{
	private boolean started;
	
	private RecordTask recordTask;	
	private RecognizerTask recognizerTask;	
	private MainActivity mainActivity;
	BlockingQueue<DataBlock> blockingQueue;

	private Character lastValue;
		
	public Controller(MainActivity mainActivity)
	{
		this.mainActivity = mainActivity;
	}

	public void changeState() 
	{
		System.out.println("changeState:"+(started?"true":"false"));
		if (started == false)
		{
			
			lastValue = ' ';
			
			blockingQueue = new LinkedBlockingQueue<DataBlock>();
			
			mainActivity.start();
			
			recordTask = new RecordTask(this,blockingQueue);			
			recognizerTask = new RecognizerTask(this,blockingQueue);
			
			// needed for Glass with API Level 15 or 19 (> 12 HONEYCOMB)
			// http://developer.android.com/reference/android/os/AsyncTask.html
			// Starting with HONEYCOMB, tasks are executed on a single thread to 
			// avoid common application errors caused by parallel execution.
			// If you truly want parallel execution, you can invoke executeOnExecutor(java.util.concurrent.Executor, 
			// Object[]) with THREAD_POOL_EXECUTOR.
			recordTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			recognizerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);			
//			recordTask.execute();
//			recognizerTask.execute();
			
			started = true;
		} else {
			
			mainActivity.stop();
			
			recognizerTask.cancel(true);
			recordTask.cancel(true);
			
			started = false;
		}
	}

	public void clear() {
		mainActivity.clearText();
	}

	public boolean isStarted() {
		return started;
	}


	public int getAudioSource()
	{
		return mainActivity.getAudioSource();
	}
	
	public void spectrumReady(Spectrum spectrum) 
	{
		mainActivity.drawSpectrum(spectrum);
	}

	public void keyReady(char key) 
	{
		mainActivity.setAciveKey(key);
		
		if(key != ' ')
			if(lastValue != key)
				mainActivity.addText(key);
		
		lastValue = key;
	}
	
	public void debug(String text) 
	{
		mainActivity.setText(text);
	}
}
