package imaizm.imagebundler;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class InputFileHandler {
	
	private File[] inputFiles;
	public File[] getInputFiles() {
		return this.inputFiles;
	}
	
	private File extractDirectory;
	private File[] extractedFiles;
	
	public InputFileHandler(File inputFile, File workDirectory) throws IOException {
		// 入力ソースの拡張子を取得
		String inputFileExtension =
			inputFile.getName().substring(inputFile.getName().length() - 3, inputFile.getName().length());

		// 入力ソースがディレクトリだった場合
		if (inputFile.isDirectory()) {
			
			System.out.println("work directory : " + workDirectory.getAbsolutePath());

			// 入力ソースのディレクトリからJpegファイルを取得
			this.inputFiles = inputFile.listFiles(
				new FileFilter() {
					public boolean accept(File file) {
						if (file.isFile() && (
								file.getName().toLowerCase().endsWith("jpg") ||
								file.getName().toLowerCase().endsWith("jpeg") ||
								file.getName().toLowerCase().endsWith("png"))) {
							return true;
						} else {
							return false;
						}
					}
				});

		// 入力ソースがZipファイルだった場合
		} else if (inputFileExtension.equalsIgnoreCase("zip")) {
			
			// 解答用仮ディレクトリの作成
			this.extractDirectory = new File(workDirectory.getPath() + "_extract");
			this.extractDirectory.mkdir();
			
			// 入力ソースのZipファイルを解凍し、解凍されたファイル群からJpegファイルを取得
			try {
				this.extractedFiles = ZipFileHandler.inflate(inputFile, extractDirectory);
			} catch (IOException e) {
				throw e;
			}
			
		// 入力ソースがディレクトリでもZipファイルでもなかった場合
		} else {
			
			// 異常終了
			throw new RuntimeException("未対応のファイル形式です。");
		}	
	}
	
	public void close() {
		if (this.extractedFiles != null) {
			// 処理済みの解凍ファイルを削除
			for (int i=0; i<this.extractedFiles.length; i++) {
				this.extractedFiles[i].delete();
			}
		}
		
		if (this.extractDirectory != null) {
			extractDirectory.delete();
		}
	}
}
