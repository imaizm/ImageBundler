package imaizm.imagebundler;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class InputFileHandlerTest {
	
	@Nested
	@DisplayName("コンストラクタに対するテスト")
	class Constructor {
	
		@Test
		@DisplayName("対象：ディレクトリ")
		void test01() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/InputFileHandlerTest/constructor/test01");
			InputFileHandler inputFileHandler = new InputFileHandler(testDataPath.toFile());
			List<String> inputFileNameList = Arrays.stream(inputFileHandler.getInputFiles()).map(file -> file.getName()).collect(Collectors.toList());
			assertAll("inputFiles",
				() -> assertTrue(inputFileNameList.contains("480x320.jpg")),
				() -> assertTrue(inputFileNameList.contains("480x320.jpeg")),
				() -> assertTrue(inputFileNameList.contains("480x320.png")),
				() -> assertFalse(inputFileNameList.contains("480x320.gif")));
		}
		
		@Test
		@DisplayName("対象：zipファイル")
		void test02() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/InputFileHandlerTest/constructor/test02/data.zip");
			InputFileHandler inputFileHandler = new InputFileHandler(testDataPath.toFile());
			List<String> inputFileNameList = Arrays.stream(inputFileHandler.getInputFiles()).map(file -> file.getName()).collect(Collectors.toList());
			assertAll("inputFiles",
				() -> assertTrue(inputFileNameList.contains("480x320.jpg")),
				() -> assertTrue(inputFileNameList.contains("480x320.jpeg")),
			//	() -> assertTrue(inputFileNameList.contains("480x320.png")),
				() -> assertFalse(inputFileNameList.contains("480x320.gif")));
		}
		
		@Test
		@DisplayName("対象：未対応ファイル")
		void test03() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/InputFileHandlerTest/constructor/test03/dummy.txt");
			RuntimeException e =
				assertThrows(
					RuntimeException.class,
					() -> new InputFileHandler(testDataPath.toFile()));
			assertEquals(e.getMessage(), "未対応のファイル形式です。");
		}
	}
}
