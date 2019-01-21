package imaizm.imagebundler;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.*;

class ImageConverterTest {

	@Nested
	@DisplayName("convertメソッドに対するテスト")
	class Convert {

		@Test
		@DisplayName("480x320の画像を240x320の画像２枚に縦分割(右->左)")
		void test01_01() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/ImageConverterTest/Convert/test01");
			Path inputFilePath = testDataPath.resolve("480x320.jpg");
			BufferedImage inputImage = ImageIO.read(inputFilePath.toFile());
			List<BufferedImage> outputImageList = ImageConverter.convert(inputImage, 240, 320,
				ImageConverter.BindingSide.RIGHT,
				ImageConverter.ContraAspectMode.SPLIT);
			assertAll("outputImageList-width-height",
				() -> assertEquals(240, outputImageList.get(0).getWidth()),
				() -> assertEquals(320, outputImageList.get(0).getHeight()),
				() -> assertEquals(240, outputImageList.get(1).getWidth()),
				() -> assertEquals(320, outputImageList.get(1).getHeight()));
			Path outputDirPath = testDataPath.resolve("outputFiles");
			ImageConverter.writeJpegFile(outputImageList.get(0), outputDirPath.resolve("01_01.jpg").toAbsolutePath().toString(), 30);
			ImageConverter.writeJpegFile(outputImageList.get(1), outputDirPath.resolve("01_02.jpg").toAbsolutePath().toString(), 30);
		}

		@Test
		@DisplayName("480x320の画像を240x320の画像２枚に縦分割(左->右)")
		void test01_02() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/ImageConverterTest/Convert/test01");
			Path inputFilePath = testDataPath.resolve("480x320.jpg");
			BufferedImage inputImage = ImageIO.read(inputFilePath.toFile());
			List<BufferedImage> outputImageList = ImageConverter.convert(inputImage, 240, 320,
				ImageConverter.BindingSide.LEFT,
				ImageConverter.ContraAspectMode.SPLIT);
			assertAll("outputImageList-width-height",
				() -> assertEquals(240, outputImageList.get(0).getWidth()),
				() -> assertEquals(320, outputImageList.get(0).getHeight()),
				() -> assertEquals(240, outputImageList.get(1).getWidth()),
				() -> assertEquals(320, outputImageList.get(1).getHeight()));
			Path outputDirPath = testDataPath.resolve("outputFiles");
			ImageConverter.writeJpegFile(outputImageList.get(0), outputDirPath.resolve("02_01.jpg").toAbsolutePath().toString(), 30);
			ImageConverter.writeJpegFile(outputImageList.get(1), outputDirPath.resolve("02_02.jpg").toAbsolutePath().toString(), 30);
		}

		@Test
		@DisplayName("480x320の画像を240x320の画像２枚に縦分割(左->右)")
		void test01_03() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/ImageConverterTest/Convert/test01");
			Path inputFilePath = testDataPath.resolve("480x320.jpg");
			BufferedImage inputImage = ImageIO.read(inputFilePath.toFile());
			List<BufferedImage> outputImageList = ImageConverter.convert(inputImage, 120, 160,
				ImageConverter.BindingSide.LEFT,
				ImageConverter.ContraAspectMode.SPLIT);
			Path outputDirPath = testDataPath.resolve("outputFiles");
			ImageConverter.writeJpegFile(outputImageList.get(0), outputDirPath.resolve("03_01.jpg").toAbsolutePath().toString(), 30);
			ImageConverter.writeJpegFile(outputImageList.get(1), outputDirPath.resolve("03_02.jpg").toAbsolutePath().toString(), 30);
			assertAll("outputImageList-width-height",
				() -> assertEquals(120, outputImageList.get(0).getWidth()),
				() -> assertEquals(160, outputImageList.get(0).getHeight()),
				() -> assertEquals(120, outputImageList.get(1).getWidth()),
				() -> assertEquals(160, outputImageList.get(1).getHeight()));
		}

		@Test
		@DisplayName("320x480の画像を320x240の画像２枚に横分割(上->下)")
		void test02() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/ImageConverterTest/Convert/test02");
			Path inputFilePath = testDataPath.resolve("320x480.jpg");
			BufferedImage inputImage = ImageIO.read(inputFilePath.toFile());
			List<BufferedImage> outputImageList = ImageConverter.convert(inputImage, 320, 240,
				ImageConverter.BindingSide.RIGHT,
				ImageConverter.ContraAspectMode.SPLIT);
			assertAll("outputImageList-width-height",
				() -> assertEquals(320, outputImageList.get(0).getWidth()),
				() -> assertEquals(240, outputImageList.get(0).getHeight()),
				() -> assertEquals(320, outputImageList.get(1).getWidth()),
				() -> assertEquals(240, outputImageList.get(1).getHeight()));
			Path outputDirPath = testDataPath.resolve("outputFiles");
			ImageConverter.writeJpegFile(outputImageList.get(0), outputDirPath.resolve("01_01.jpg").toAbsolutePath().toString(), 30);
			ImageConverter.writeJpegFile(outputImageList.get(1), outputDirPath.resolve("01_02.jpg").toAbsolutePath().toString(), 30);
		}
	}
}
