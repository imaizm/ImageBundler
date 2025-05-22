package imaizm.imagebundler;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 入力ファイルまたはディレクトリを処理し、対象となる画像ファイルのリストを管理するクラスです。
 * <p>
 * このクラスは、以下の3つのケースで入力ソースを扱います。
 * <ul>
 *   <li>入力がディレクトリの場合：ディレクトリ内の画像ファイル（JPG, JPEG, PNG）をリストアップします。</li>
 *   <li>入力がZIPファイルの場合：ZIPファイルを一時ディレクトリに解凍し、解凍されたファイルの中から画像ファイルをリストアップします。</li>
 *   <li>上記以外の場合：未対応のファイル形式として実行時例外をスローします。</li>
 * </ul>
 * ZIPファイルが処理された場合、{@link #close()} メソッドを呼び出すことで、解凍時に作成された一時ファイルおよびディレクトリが削除されます。
 * </p>
 */
public class InputFileHandler {
	
	/** 処理対象となる画像ファイルのパスのリスト。 */
	private List<Path> inputFilePathList;
	/**
	 * 処理対象となる画像ファイルのパスのリストを取得します。
	 * <p>
	 * このリストは、コンストラクタで指定された入力ソースに基づいて設定されます。
	 * 入力ソースがディレクトリの場合はその中の画像ファイル、ZIPファイルの場合は解凍された画像ファイルが含まれます。
	 * </p>
	 * @return 処理対象の画像ファイルの {@link Path} のリスト。
	 */
	public List<Path> getInputFilePathList() {
		return this.inputFilePathList;
	}
	
	/** ZIPファイルが入力された場合に、ファイルを解凍するための一時ディレクトリのパス。 */
	private Path extractDirectoryPath;
	/**
	 * ZIPファイルが解凍された一時ディレクトリのパスを取得します。
	 * <p>
	 * このパスは、入力ソースがZIPファイルの場合にのみ設定されます。
	 * それ以外の場合は null になります。
	 * </p>
	 * @return ZIP解凍用の一時ディレクトリの {@link Path}。該当しない場合は null。
	 */
	public Path getExtractDirectoryPath() {
		return this.extractDirectoryPath;
	}
	/** ZIPファイルが入力された場合に、解凍されたファイルのパスのリスト。 */
	private List<Path> extractedFilePathList;
	
	/**
	 * {@code InputFileHandler} オブジェクトを構築し、指定された入力パスに基づいて処理対象ファイルを初期化します。
	 * <p>
	 * 入力パス {@code inputFilePath} がディレクトリであるか、ZIPファイルであるか、
	 * またはサポートされていないファイル形式であるかを判断します。
	 * <ul>
	 *   <li><b>ディレクトリの場合:</b>
	 *     指定されたディレクトリ内にあるJPG, JPEG, PNGファイルを検索し、
	 *     それらのファイルのパスを {@link #inputFilePathList} に格納します。
	 *     大文字・小文字を区別しないファイル拡張子でマッチングします。
	 *   </li>
	 *   <li><b>ZIPファイルの場合:</b>
	 *     まず、{@link WorkDirectoryHandler} を使用して一時的な作業ディレクトリを作成し、
	 *     そのパスを {@link #extractDirectoryPath} に格納します。
	 *     次に、指定されたZIPファイルをこの一時ディレクトリに解凍します（{@link ZipFileHandler#inflate(Path, Path)} を使用）。
	 *     解凍されたファイルのパスのリストは {@link #extractedFilePathList} に格納され、
	 *     このリストが {@link #inputFilePathList} としても使用されます。
	 *   </li>
	 *   <li><b>上記以外の場合:</b>
	 *     サポートされていないファイル形式であると判断し、"未対応のファイル形式です。" というメッセージと共に
	 *     {@link RuntimeException} をスローします。
	 *   </li>
	 * </ul>
	 * ファイル拡張子のマッチングには、大文字・小文字を区別しない正規表現ベースの {@link PathMatcher} を使用します。
	 * コメントアウトされたglobベースのPathMatcherは現在使用されていません。
	 * </p>
	 *
	 * @param inputFilePath 処理対象のファイルまたはディレクトリのパス。
	 * @throws IOException ファイルの読み込み、ディレクトリのリスト、またはZIPファイルの解凍中にI/Oエラーが発生した場合。
	 */
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
			
			// 入力ソースのディレクトリからjpg/jpeg/pngファイルを取得
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
	
	/**
	 * この {@code InputFileHandler} が使用したリソースを解放します。
	 * <p>
	 * 主に、入力ソースがZIPファイルであった場合に作成された一時ファイルをクリーンアップするために使用されます。
	 * <ul>
	 *   <li>{@link #extractedFilePathList} が null でない場合（つまり、ZIPファイルが解凍された場合）：
	 *     <ul>
	 *       <li>{@link #inputFilePathList} を null に設定します。</li>
	 *       <li>{@link #extractedFilePathList} に含まれるすべての解凍されたファイルを削除します。</li>
	 *     </ul>
	 *   </li>
	 *   <li>{@link #extractDirectoryPath} が null でない場合（つまり、解凍用の一時ディレクトリが作成された場合）：
	 *     <ul>
	 *       <li>その一時ディレクトリを削除します。</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 * このメソッドは、処理が完了した後、不要になった一時ファイルを確実に削除するために呼び出す必要があります。
	 * </p>
	 *
	 * @throws IOException ファイルまたはディレクトリの削除中にI/Oエラーが発生した場合。
	 */
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
