package imaizm.imagebundler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.*;

class ZipFileHandlerTest {
	
	@Nested
	@DisplayName("inflateメソッドに対するテスト")
	class Inflate {

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
				//	() -> assertTrue(inflatedFileNameList.contains("480x320.png")),
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
