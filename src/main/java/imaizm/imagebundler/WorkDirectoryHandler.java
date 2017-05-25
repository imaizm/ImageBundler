package imaizm.imagebundler;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WorkDirectoryHandler {

	/** 作業用ディレクトリのパスを格納 */
	private String tempDirectoryPath;
	/** 作業量ディレクトリに作成する仮ディレクトリのディレクトリ名を格納 */
	private String workDirectoryName;
	/** 仮ディレクトリ操作用のFileオブジェクト*/
	private File workDirectory;

	public WorkDirectoryHandler() {
		
		// 作業ディレクトリ：実行時OSのTEMPディレクトリを取得
		this.tempDirectoryPath = System.getProperty("java.io.tmpdir");
		// 仮ディレクトリ：日時＋乱数５桁
		this.workDirectoryName = 
			(new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date()) +
			(new DecimalFormat("#####")).format((int) (Math.random() * 100000D));
		// 仮ディレクトリ操作用Fileオブジェクト
		this.workDirectory = new File(tempDirectoryPath + File.separator + workDirectoryName);
		// 仮ディレクトリの作成
		this.workDirectory.mkdir();
		
		try {
			System.out.println("input file info...");
			System.out.println("File#getAbsoluteFile (work) : " + workDirectory.getAbsoluteFile());
		//	System.out.println("File#getAbsolutePath  : " + workDirectory.getAbsolutePath());
		//	System.out.println("File#getCanonicalFile : " + workDirectory.getCanonicalFile());
		//	System.out.println("File#getCanonicalPath : " + workDirectory.getCanonicalPath());
		//	System.out.println("File#getName          : " + workDirectory.getName());
		//	System.out.println("File#getParent        : " + workDirectory.getParent());
		//	System.out.println("File#getParentFile    : " + workDirectory.getParentFile());
		//	System.out.println("File#getPath          : " + workDirectory.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public File getWorkDirectory() {
		return this.workDirectory;
	}
}
