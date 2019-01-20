package imaizm.imagebundler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.*;

class IniFileHandlerTest {

	@Nested
	@DisplayName("コンストラクタに対するテスト")
	class Constructor {

		@Nested
		@DisplayName("システムプロパティ「user.dir」を変更するテストケース群")
		class CasesOfSetUserDirSystemProperty {
			
			private String originalUserDir;
			
			@BeforeEach
			void beforeEach() {
				this.originalUserDir = System.getProperty("user.dir");
			}
			
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
					iniFileHandler.getWorkDirectoryOfLastTime().getAbsolutePath());
				
				Files.delete(tempIniFile);
			}
			
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
					iniFileHandler.getWorkDirectoryOfLastTime().getAbsolutePath());
				
				Files.delete(tempIniFile);
			}

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

			@AfterEach
			void afterEach() {
				System.setProperty("user.dir", this.originalUserDir);
			}
		}
	}
}
