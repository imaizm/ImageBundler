package imaizm.imagebundler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.*;

/**
 * {@link ZipFileHandler} クラスのテストクラスです。
 * <p>
 * 主に {@link ZipFileHandler#inflate(Path, Path)} メソッドの動作を検証します。
 * </p>
 */
class ZipFileHandlerTest {
	
	@Nested
	@DisplayName("inflateメソッドに対するテスト")
	/**
	 * {@link ZipFileHandler#inflate(Path, Path)} メソッドのテストケースをグループ化するネストクラスです。
	 */
	class Inflate {

		/**
		 * {@link ZipFileHandler#inflate(Path, Path)} メソッドが、指定されたZIPファイルを正しく解凍し、
		 * JPEGファイルのみを展開することを確認するテストです。
		 * <p>
		 * 手順：
		 * <ol>
		 *   <li>テスト用のZIPファイル (data.zip) と、解凍先の一時ディレクトリ (inflated) を準備します。</li>
		 *   <li>{@code ZipFileHandler.inflate} メソッドを呼び出し、ZIPファイルを解凍します。</li>
		 *   <li>解凍されたファイル名のリストを取得し、以下の点を確認します：
		 *     <ul>
		 *       <li>"480x320.jpg" が含まれていること。</li>
		 *       <li>"480x320.jpeg" が含まれていること。</li>
		 *       <li>"480x320.png" が含まれていないこと（現状のZipFileHandlerはJPG/JPEGのみを対象とするため）。
		 *           テストコード内ではこのアサーションはコメントアウトされています。</li>
		 *       <li>"480x320.gif" が含まれていないこと。</li>
		 *     </ul>
		 *   </li>
		 *   <li>テスト後、解凍された個々のファイルと一時ディレクトリを削除します。</li>
		 * </ol>
		 * </p>
		 * @throws IOException テストファイルの準備、ZIPファイルの解凍、または一時ファイルの削除中にエラーが発生した場合。
		 */
		@Test
		@DisplayName("実際にZipファイルを解凍し中身を確認するテスト")
		void test01() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/ZipFileHandlerTest/Inflate/test01/data.zip");
			Path tempInflatePath = testDataPath.getParent().resolve("inflated");
			Files.createDirectory(tempInflatePath);
			try {
				List<Path> inflatedFilePathList = ZipFileHandler.inflate(testDataPath, tempInflatePath);
				List<String> inflatedFileNameList =
					Files.list(tempInflatePath)
						.map(path -> path.getFileName().toString())
						.collect(Collectors.toList());
				assertAll("inputFiles",
					() -> assertTrue(inflatedFileNameList.contains("480x320.jpg")),
					() -> assertTrue(inflatedFileNameList.contains("480x320.jpeg")),
				//	() -> assertTrue(inflatedFileNameList.contains("480x320.png")), // ZipFileHandlerは現状JPG/JPEGのみ対応
					() -> assertFalse(inflatedFileNameList.contains("480x320.gif")));
				
				for (Path inflatedFilePath : inflatedFilePathList) {
					Files.delete(inflatedFilePath);
				}
				
			} finally {
				Files.delete(tempInflatePath);
			}
		}
	}
}
