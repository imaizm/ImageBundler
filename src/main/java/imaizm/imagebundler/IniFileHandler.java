package imaizm.imagebundler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IniFileHandler {
	
	private Path iniFilePath;
	private Path workDirectoryPathOfLastTime;
	
	public Path getWorkDirectoryPathOfLastTime() {
		return this.workDirectoryPathOfLastTime;
	}
	
	public IniFileHandler() throws IOException {
		//
		// ファイル選択ダイアログの起点を定める
		//
		
		// ユーザディレクトリ直下からiniファイルを探す
		String currentDirectoryPath = System.getProperty("user.dir");
		this.iniFilePath = Paths.get(currentDirectoryPath, Constants.APPLICATION_NAME + ".ini");
		
		// iniファイルが存在した場合
		if (Files.exists(this.iniFilePath)) {
			try (BufferedReader bufferedReader = Files.newBufferedReader(this.iniFilePath)) {
				String line = bufferedReader.readLine();
				if (line != null) {
					this.workDirectoryPathOfLastTime = this.getCurrentDirectoryPath(line);
				}
			} catch (IOException e) {
				throw e;
			}
		
		// ImageConverterForPda.iniファイルが存在しない場合
		} else {
			// ユーザディレクトリにImageConverterForPda.iniファイルを作成する
			Files.createFile(this.iniFilePath);
		}
		
	}
	
	/**
	 * path文字列に指定されたディレクトリパスを検証し、存在すればそのパスのFileオブジェクトを、
	 * 存在しなければパスを一つづつ遡っていき、最初に発見した存在するパスのFileオブジェクトを返す。
	 * @param pathString 存在の検証をするパス文字列
	 * @return 存在したパスのFileオブジェクト
	 */
	private Path getCurrentDirectoryPath(String pathString) {

		Path pathForReturn = Paths.get(pathString);
		if (Files.notExists(pathForReturn)) {
			while (
				pathForReturn != null &&
				Files.notExists(pathForReturn)) {
				pathForReturn = pathForReturn.getParent();
			}				
		}
		
		return pathForReturn;
	}
	
	public void writeWorkDirectoryOfLastTime(String filePath) throws IOException {
		
		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.iniFilePath)) {
			bufferedWriter.write(filePath);
		} catch (IOException e) {
			throw e;
		}
		
	}
}
