package runspl;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JavaUtility {
	public static final JavaUtility INSTANCE = new JavaUtility();


	public void writeToFile(File file, String content) throws IOException
	{
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false));
		bufferedWriter.write(content);
		bufferedWriter.close();		
	} 

	public void writeToFile(String filename, String content)
	{
		writeToFile(filename, content, false);		
	}

	public void writeToFile(String filename, String content, boolean append)
	{
		try
		{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename, append));
			bufferedWriter.write(content);
			bufferedWriter.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * A utility method to read a file
	 */
	public String getFileContents(String filename)
	{
		StringBuffer fileContents = null;
		
		try
		{
			fileContents = new StringBuffer();
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
			final int bufLength = 1024;
			char[] buf = new char[bufLength];
			int charsRead = -1;
	
			while((charsRead = bufferedReader.read(buf, 0, bufLength)) != -1)
			{
				// Eliminate null characters
				//int lastNonNullIndex = bufLength-1; 
				//while(lastNonNullIndex >= 0 && buf[lastNonNullIndex] == '\0');
				//	lastNonNullIndex--;
				//if(lastNonNullIndex >= 0)
	
				fileContents.append(buf, 0, charsRead);
				buf = new char[bufLength];
			}
			bufferedReader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return fileContents.toString();
	}

	public void copyFile2(File srcFile, File dstFile, boolean append)
	{
		InputStream inStream = null;
		OutputStream outStream = null;
		
		try
		{
			inStream = new FileInputStream(srcFile);
			outStream = new FileOutputStream(dstFile, append);

			byte[] buffer = new byte[1024];

			int length;
			//copy the file content in bytes 
			while ((length = inStream.read(buffer)) > 0){
				outStream.write(buffer, 0, length);
			}			
		}catch(IOException e){
			e.printStackTrace();
		}		
		finally{
			try {
				inStream.close();
				outStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void copyFile(String sourcePath, String targetPath, boolean append)
	{
		copyFile2(new File(sourcePath), new File(targetPath), append);
	}
}
