package imaizm.imagebundler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.*;

/**
 * {@link IniFileHandler} クラスのテストクラスです。
 * <p>
 * 主に {@link IniFileHandler#IniFileHandler()} コンストラクタと
 * {@link IniFileHandler#writeWorkDirectoryOfLastTime(String)} メソッドの動作を、
 * 様々な条件下で検証します。特に、INIファイルの存在有無や内容、
 * およびシステムプロパティ "user.dir" の設定がテスト結果に与える影響を確認します。
 * </p>
 */
class IniFileHandlerTest {

	@Nested
	@DisplayName("コンストラクタに対するテスト")
	/**
	 * {@link IniFileHandler#IniFileHandler()} コンストラクタのテストケースをグループ化するネストクラスです。
	 */
	class Constructor {

		@Nested
		@DisplayName("システムプロパティ「user.dir」を変更するテストケース群")
		/**
		 * システムプロパティ "user.dir" (カレントワーキングディレクトリ) の値を変更して
		 * {@link IniFileHandler} のコンストラクタの動作をテストするケースをグループ化します。
		 * <p>
		 * 各テストの前後で "user.dir" の値を保存・復元することで、テスト間の影響を排除します。
		 * </p>
		 */
		class CasesOfSetUserDirSystemProperty {
			
			/** テスト実行前の "user.dir" システムプロパティの値を保持します。 */
			private String originalUserDir;
			
			/**
			 * 各テストメソッドの実行前に呼び出されます。
			 * 現在の "user.dir" システムプロパティの値を {@link #originalUserDir} に保存します。
			 */
			@BeforeEach
			void beforeEach() {
				this.originalUserDir = System.getProperty("user.dir");
			}
			
			/**
			 * INIファイルが存在し、その中に記録されている前回実行ディレクトリのパスが実在する場合のテストです。
			 * <p>
			 * 手順：
			 * <ol>
			 *   <li>"user.dir" をテストデータ用のパスに設定します。</li>
			 *   <li>テストデータパス直下に "ImageBundler.ini" を作成し、テストデータパス自身を書き込みます。</li>
			 *   <li>{@link IniFileHandler} をインスタンス化します。</li>
			 *   <li>{@link IniFileHandler#getWorkDirectoryPathOfLastTime()} がINIファイルに書き込んだパスと等しいことを表明します。</li>
			 *   <li>作成したINIファイルを削除します。</li>
			 * </ol>
			 * </p>
			 * @throws IOException テストファイルの作成または削除、INIファイルの読み書き中にエラーが発生した場合。
			 */
			@Test
			@DisplayName("iniファイルが存在するケース(前回実行dirが実在path)")
			void test01() throws IOException {
				Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/IniFileHandlerTest/Constructor/test01");
				System.setProperty(
					"user.dir",
					testDataPath.toAbsolutePath().toString());
				
				Path tempIniFile = testDataPath.resolve("ImageBundler.ini");
				try (BufferedWriter writer = Files.newBufferedWriter(tempIniFile)) {
					writer.write(testDataPath.toAbsolutePath().toString());
				}
				
				IniFileHandler iniFileHandler = new IniFileHandler();
				assertEquals(
					testDataPath.toAbsolutePath().toString(),
					iniFileHandler.getWorkDirectoryPathOfLastTime().toAbsolutePath().toString());
				
				Files.delete(tempIniFile);
			}
			
			/**
			 * INIファイルが存在し、その中に記録されている前回実行ディレクトリのパスが、
			 * 実在するパスと実在しないパスの組み合わせである場合のテストです。
			 * （例： `/actual_path/non_existent_path1/non_existent_path2`）
			 * <p>
			 * 手順：
			 * <ol>
			 *   <li>"user.dir" をテストデータ用のパスに設定します。</li>
			 *   <li>テストデータパス直下に "ImageBundler.ini" を作成し、
			 *       テストデータパスの下に存在しないサブディレクトリ "aaa/bbb" を含むパスを書き込みます。</li>
			 *   <li>{@link IniFileHandler} をインスタンス化します。</li>
			 *   <li>{@link IniFileHandler#getWorkDirectoryPathOfLastTime()} が、
			 *       INIファイルに書き込んだパスのうち、実際に存在する最も深い階層のパス（この場合はテストデータパス自身）
			 *       と等しいことを表明します。</li>
			 *   <li>作成したINIファイルを削除します。</li>
			 * </ol>
			 * </p>
			 * @throws IOException テストファイルの作成または削除、INIファイルの読み書き中にエラーが発生した場合。
			 */
			@Test
			@DisplayName("iniファイルが存在するケース(前回実行dirが実在path+非実在path)")
			void test02() throws IOException {
				Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/IniFileHandlerTest/Constructor/test02");
				System.setProperty(
					"user.dir",
					testDataPath.toAbsolutePath().toString());
				
				Path tempIniFile = testDataPath.resolve("ImageBundler.ini");
				try (BufferedWriter writer = Files.newBufferedWriter(tempIniFile)) {
					writer.write(
						testDataPath
							.resolve("aaa")
							.resolve("bbb")
							.toAbsolutePath().toString());
				}
				
				IniFileHandler iniFileHandler = new IniFileHandler();
				assertEquals(
					testDataPath.toAbsolutePath().toString(),
					iniFileHandler.getWorkDirectoryPathOfLastTime().toAbsolutePath().toString());
				
				Files.delete(tempIniFile);
			}

			/**
			 * INIファイルが存在しない場合のテストです。
			 * <p>
			 * 手順：
			 * <ol>
			 *   <li>"user.dir" をテストデータ用のパスに設定します。</li>
			 *   <li>テストデータパス直下に "ImageBundler.ini" が存在しないことを確認します。</li>
			 *   <li>{@link IniFileHandler} をインスタンス化します。</li>
			 *   <li>{@link IniFileHandler#getWorkDirectoryPathOfLastTime()} が null であることを表明します（INIファイルが存在しなかったため）。</li>
			 *   <li>コンストラクタの実行により、"ImageBundler.ini" が空のファイルとして新規作成されたことを表明します。</li>
			 *   <li>{@link IniFileHandler#writeWorkDirectoryOfLastTime(String)} を使用してテストデータパスをINIファイルに書き込みます。</li>
			 *   <li>INIファイルを読み込み、書き込まれた内容がテストデータパスと一致することを表明します。</li>
			 *   <li>作成したINIファイルを削除します。</li>
			 * </ol>
			 * </p>
			 * @throws IOException テストファイルの作成または削除、INIファイルの読み書き中にエラーが発生した場合。
			 */
			@Test
			@DisplayName("iniファイルが存在しないケース")
			void test03() throws IOException {
				Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/IniFileHandlerTest/Constructor/test03");
				System.setProperty(
					"user.dir",
					testDataPath.toAbsolutePath().toString());
				
				Path tempIniFile = testDataPath.resolve("ImageBundler.ini");
				assertTrue(Files.notExists(tempIniFile));
				
				IniFileHandler iniFileHandler = new IniFileHandler();
				assertEquals(
					null,
					iniFileHandler.getWorkDirectoryPathOfLastTime());
				assertTrue(Files.exists(tempIniFile));
				
				iniFileHandler.writeWorkDirectoryOfLastTime(testDataPath.toAbsolutePath().toString());
				try (BufferedReader reader = Files.newBufferedReader(tempIniFile)) {
					String line = reader.readLine();
					assertEquals(
						testDataPath.toAbsolutePath().toString(),
						line);
				}
				
				Files.delete(tempIniFile);
			}

			/**
			 * 各テストメソッドの実行後に呼び出されます。
			 * "user.dir" システムプロパティの値を、テスト実行前に保存した元の値 ({@link #originalUserDir}) に復元します。
			 */
			@AfterEach
			void afterEach() {
				System.setProperty("user.dir", this.originalUserDir);
			}
		}
	}
}
