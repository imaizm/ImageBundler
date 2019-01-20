package imaizm.imagebundler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkDirectoryHandler {

	/** 作業用ディレクトリのパスを格納 */
	private String tempDirectoryPath;
	/** 作業量ディレクトリに作成する仮ディレクトリのディレクトリ名を格納 */
	private String workDirectoryName;
	/** 仮ディレクトリ操作用のPathオブジェクト*/
	private Path workDirectoryPath;

	public WorkDirectoryHandler() throws IOException {
		
		// 作業ディレクトリ：実行時OSのTEMPディレクトリを取得
		this.tempDirectoryPath = System.getProperty("java.io.tmpdir");
		// 仮ディレクトリ：日時＋乱数５桁
		this.workDirectoryName = 
			(DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())) +
			(new DecimalFormat("#####")).format((int) (Math.random() * 100000D));
		
		// 仮ディレクトリの作成
		this.workDirectoryPath = Files.createDirectory(Paths.get(tempDirectoryPath, workDirectoryName));
	}
	
	public File getWorkDirectory() {
		return this.workDirectoryPath.toFile();
	}
	
	public Path getWorkDirectoryPath() {
		return this.workDirectoryPath;
	}
}
