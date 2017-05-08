package imaizm.imagebundler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class IniFileHandler {
	
	private File iniFile;
	private File workDirectoryOfLastTime;
	
	public File getWorkDirectoryOfLastTime() {
		return this.workDirectoryOfLastTime;
	}
	
	public IniFileHandler() throws IOException {
		//
		// ファイル選択ダイアログの起点を定める
		//
		
		// ユーザディレクトリ直下からiniファイルを探す
		String currentDirectoryPath = System.getProperty("user.dir");
		String iniFileName = currentDirectoryPath + File.separator + Constants.APPLICATION_NAME + ".ini";
		this.iniFile = new File(iniFileName);
		
		// iniファイルが存在した場合
		if (iniFile.exists()) {
			BufferedReader bufferedReader = null;
			try {
				bufferedReader = new BufferedReader(new FileReader(iniFile));
				String line = bufferedReader.readLine();
				if (line != null) {
					this.workDirectoryOfLastTime = this.getCurrentDirectory(line);
				}
			} catch (IOException e) {
				throw e;
			} finally {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					throw e;
				}
			}
		
		// ImageConverterForPda.iniファイルが存在しない場合
		} else {
			// ユーザディレクトリにImageConverterForPda.iniファイルを作成する
			this.iniFile.createNewFile();
		}
		
	}
	
	/**
	 * path文字列に指定されたディレクトリパスを検証し、存在すればそのパスのFileオブジェクトを、
	 * 存在しなければパスを一つづつ遡っていき、最初に発見した存在するパスのFileオブジェクトを返す。
	 * @param path 存在の検証をするパス文字列
	 * @return 存在したパスのFileオブジェクト
	 */
	private File getCurrentDirectory(String path) {
		
		File fileForReturn = null;
		
		fileForReturn = new File(path);
		if (! fileForReturn.exists()) {
			
			while (
				fileForReturn != null &&
				fileForReturn.exists() == false) {
				fileForReturn = fileForReturn.getParentFile();
			}
		}
		
		return fileForReturn;
	}
	
	public void writeWorkDirectoryOfLastTime(String filePath) throws IOException {
		PrintWriter printWriter = null;
		try {
			
			printWriter = new PrintWriter(iniFile);
			printWriter.print(filePath);
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				printWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
