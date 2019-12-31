package imaizm.imagebundler;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;

public class InputFileHandler {
	
	private List<Path> inputFilePathList;
	public List<Path> getInputFilePathList() {
		return this.inputFilePathList;
	}
	
	private Path extractDirectoryPath;
	public Path getExtractDirectoryPath() {
		return this.extractDirectoryPath;
	}
	private List<Path> extractedFilePathList;
	
	public InputFileHandler(Path inputFilePath) throws IOException {
		
//		PathMatcher pmZip = FileSystems.getDefault().getPathMatcher("glob:**.zip");
//		PathMatcher pmJpg = FileSystems.getDefault().getPathMatcher("glob:**.jpg");
//		PathMatcher pmJpeg = FileSystems.getDefault().getPathMatcher("glob:**.jpeg");
//		PathMatcher pmPng = FileSystems.getDefault().getPathMatcher("glob:**.png");
		PathMatcher pmZip = FileSystems.getDefault().getPathMatcher("regex:.+\\.(?i)zip");
		PathMatcher pmJpg = FileSystems.getDefault().getPathMatcher("regex:.+\\.(?i)jpg");
		PathMatcher pmJpeg = FileSystems.getDefault().getPathMatcher("regex:.+\\.(?i)jpeg");
		PathMatcher pmPng = FileSystems.getDefault().getPathMatcher("regex:.+\\.(?i)png");

		// 入力ソースがディレクトリだった場合
		if (Files.isDirectory(inputFilePath)) {
			
			// 入力ソースのディレクトリからJpegファイルを取得
			this.inputFilePathList = Files.list(inputFilePath)
				.filter(listFilePath -> (!Files.isDirectory(listFilePath))
					&& (pmJpg.matches(listFilePath) ||
						pmJpeg.matches(listFilePath) ||
						pmPng.matches(listFilePath)))
				.collect(Collectors.toList());

		// 入力ソースがZipファイルだった場合
		} else if (pmZip.matches(inputFilePath)) {
			
			// 解凍用仮ディレクトリの作成
			this.extractDirectoryPath = (new WorkDirectoryHandler()).getWorkDirectoryPath();
			
			// 入力ソースのZipファイルを解凍し、解凍されたファイル群からJpegファイルを取得
			try {
				this.extractedFilePathList = ZipFileHandler.inflate(inputFilePath, this.extractDirectoryPath);
				this.inputFilePathList = this.extractedFilePathList;
			} catch (IOException e) {
				throw e;
			}
			
		// 入力ソースがディレクトリでもZipファイルでもなかった場合
		} else {
			
			// 異常終了
			throw new RuntimeException("未対応のファイル形式です。");
		}	
	}
	
	public void close() throws IOException {
		if (this.extractedFilePathList != null) {
			this.inputFilePathList = null;
			
			// 処理済みの解凍ファイルを削除
			for(Path extractedFilePath : this.extractedFilePathList) {
				Files.delete(extractedFilePath);
			}
		}
		
		if (this.extractDirectoryPath != null) {
			Files.delete(extractDirectoryPath);
		}
	}
}
