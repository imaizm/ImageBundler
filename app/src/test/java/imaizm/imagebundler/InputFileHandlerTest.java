package imaizm.imagebundler;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link InputFileHandler} クラスのテストクラスです。
 * <p>
 * 主に {@link InputFileHandler#InputFileHandler(Path)} コンストラクタと
 * {@link InputFileHandler#close()} メソッドの動作を、様々な入力条件のもとで検証します。
 * </p>
 */
public class InputFileHandlerTest {
	
	@Nested
	@DisplayName("コンストラクタに対するテスト")
	/**
	 * {@link InputFileHandler#InputFileHandler(Path)} コンストラクタのテストケースをグループ化するネストクラスです。
	 * <p>
	 * 入力対象がディレクトリの場合、ZIPファイルの場合、未対応ファイルの場合、
	 * およびファイル拡張子の大文字・小文字の区別に関してテストを行います。
	 * </p>
	 */
	class Constructor {
	
		/**
		 * 入力対象がディレクトリの場合の {@link InputFileHandler} のコンストラクタのテストです。
		 * <p>
		 * 指定されたディレクトリ内に存在するサポート対象の画像ファイル（.jpg, .jpeg, .png）が
		 * 正しく認識され、リストアップされることを確認します。
		 * サポート対象外のファイル（.gif）が含まれていないことも確認します。
		 * </p>
		 * @throws IOException テストデータの読み込み中にエラーが発生した場合。
		 */
		@Test
		@DisplayName("対象：ディレクトリ")
		void test01() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/InputFileHandlerTest/constructor/test01");
			InputFileHandler inputFileHandler = new InputFileHandler(testDataPath);
			List<String> inputFileNameList =
				inputFileHandler.getInputFilePathList()
					.stream()
					.map(file -> file.getFileName().toString())
					.collect(Collectors.toList());
			assertAll("inputFiles",
				() -> assertTrue(inputFileNameList.contains("480x320.jpg")),
				() -> assertTrue(inputFileNameList.contains("480x320.jpeg")),
				() -> assertTrue(inputFileNameList.contains("480x320.png")),
				() -> assertFalse(inputFileNameList.contains("480x320.gif")));
		}
		
		/**
		 * 入力対象がZIPファイルの場合の {@link InputFileHandler} のコンストラクタおよび {@link InputFileHandler#close()} メソッドのテストです。
		 * <p>
		 * 指定されたZIPファイルが正しく解凍され、サポート対象の画像ファイル（.jpg, .jpeg）が
		 * 処理対象としてリストアップされることを確認します。
		 * ZIPファイル内にPNGファイルも含まれていますが、現在のテストコードではコメントアウトされており、
		 * PNGが処理対象に含まれない（または含まれるべきだがテストされていない）状態であることを示唆しています。
		 * サポート対象外のファイル（.gif）がリストアップされていないことも確認します。
		 * さらに、{@link InputFileHandler#close()} メソッド呼び出し後に、解凍に使用された一時ディレクトリが
		 * 適切に削除されることを検証します。
		 * </p>
		 * @throws IOException テストデータの読み込み、ZIPファイルの解凍、または一時ディレクトリの操作中にエラーが発生した場合。
		 */
		@Test
		@DisplayName("対象：zipファイル")
		void test02() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/InputFileHandlerTest/constructor/test02/data.zip");
			InputFileHandler inputFileHandler = new InputFileHandler(testDataPath);
			List<String> inputFileNameList =
				inputFileHandler.getInputFilePathList()
					.stream()
					.map(file -> file.getFileName().toString())
					.collect(Collectors.toList());
			assertAll("inputFiles",
				() -> assertTrue(inputFileNameList.contains("480x320.jpg")),
				() -> assertTrue(inputFileNameList.contains("480x320.jpeg")),
			//	() -> assertTrue(inputFileNameList.contains("480x320.png")), // ZipFileHandlerは現状JPG/JPEGのみ対応
				() -> assertFalse(inputFileNameList.contains("480x320.gif")));
			Path extractDirectoryPath = inputFileHandler.getExtractDirectoryPath();
			List<String> beforeDeleteList =
				Files.list(extractDirectoryPath)
					.map(file -> file.getFileName().toString())
					.collect(Collectors.toList());;
			assertAll("extractedFiles",
					() -> assertTrue(beforeDeleteList.contains("480x320.jpg")),
					() -> assertTrue(beforeDeleteList.contains("480x320.jpeg")),
				//	() -> assertTrue(beforeDeleteList.contains("480x320.png")), // ZipFileHandlerは現状JPG/JPEGのみ対応
					() -> assertFalse(beforeDeleteList.contains("480x320.gif")));
			
			inputFileHandler.close();
			assertTrue(Files.notExists(extractDirectoryPath));
		}
		
		/**
		 * 入力対象がサポートされていないファイル形式（この場合は .txt ファイル）の場合の
		 * {@link InputFileHandler} のコンストラクタのテストです。
		 * <p>
		 * 未対応のファイル形式が指定された場合に、期待通り {@link RuntimeException} がスローされ、
		 * その例外メッセージが "未対応のファイル形式です。" であることを確認します。
		 * </p>
		 * @throws IOException テストデータの読み込み中にエラーが発生した場合（このテストでは通常発生しない）。
		 */
		@Test
		@DisplayName("対象：未対応ファイル")
		void test03() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/InputFileHandlerTest/constructor/test03/dummy.txt");
			RuntimeException e =
				assertThrows(
					RuntimeException.class,
					() -> new InputFileHandler(testDataPath));
			assertEquals(e.getMessage(), "未対応のファイル形式です。");
		}

		/**
		 * 入力対象がディレクトリの場合で、ファイル拡張子が大文字・小文字混合である場合の
		 * {@link InputFileHandler} のコンストラクタのテストです。
		 * <p>
		 * ファイル拡張子が大文字（.JPG, .JPEG, .PNG）であっても、
		 * 正しくサポート対象の画像ファイルとして認識され、リストアップされることを確認します。
		 * サポート対象外のファイル（.GIF）が含まれていないことも確認します。
		 * これは、ファイル拡張子のマッチングがケースインセンシティブに行われることを検証するものです。
		 * </p>
		 * @throws IOException テストデータの読み込み中にエラーが発生した場合。
		 */
		@Test
		@DisplayName("対象：ディレクトリ（拡張子の大文字小文字区別なし確認）")
		void test04() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/InputFileHandlerTest/constructor/test04");
			InputFileHandler inputFileHandler = new InputFileHandler(testDataPath);
			List<String> inputFileNameList =
					inputFileHandler.getInputFilePathList()
							.stream()
							.map(file -> file.getFileName().toString())
							.collect(Collectors.toList());
			assertAll("inputFiles",
					() -> assertTrue(inputFileNameList.contains("480x320.JPG")),
					() -> assertTrue(inputFileNameList.contains("480x320.JPEG")),
					() -> assertTrue(inputFileNameList.contains("480x320.PNG")),
					() -> assertFalse(inputFileNameList.contains("480x320.GIF")));
		}
	}
}
