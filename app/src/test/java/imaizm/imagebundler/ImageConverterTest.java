package imaizm.imagebundler;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.*;

/**
 * {@link ImageConverter} クラスのテストクラスです。
 * <p>
 * 主に {@link ImageConverter#convert(BufferedImage, int, int, ImageConverter.BindingSide, ImageConverter.CenterClipOption, ImageConverter.ContraAspectMode)}
 * メソッドの様々な条件下での動作を検証します。
 * </p>
 */
class ImageConverterTest {

	@Nested
	@DisplayName("convertメソッドに対するテスト")
	/**
	 * {@link ImageConverter#convert(BufferedImage, int, int, ImageConverter.BindingSide, ImageConverter.CenterClipOption, ImageConverter.ContraAspectMode)}
	 * メソッドのテストケースをグループ化するネストクラスです。
	 * <p>
	 * 様々な入力画像、出力サイズ、および変換オプションの組み合わせに対して、
	 * 期待される出力画像の数、サイズ、および内容（テストデータとしてファイルに保存）を検証します。
	 * </p>
	 */
	class Convert {

		/**
		 * 横長の入力画像（480x320ピクセル）を、指定された出力サイズ（240x320ピクセル）に合わせて
		 * 縦方向に2分割するテストです。
		 * <p>
		 * 設定：
		 * <ul>
		 *   <li>綴じ方向: 右 ({@link ImageConverter.BindingSide#RIGHT}) - 分割時、右側の画像がリストの最初に来る</li>
		 *   <li>中央切り抜き: オフ ({@link ImageConverter.CenterClipOption#OFF})</li>
		 *   <li>縦横比処理モード: 分割 ({@link ImageConverter.ContraAspectMode#SPLIT})</li>
		 * </ul>
		 * 期待される結果：
		 * <ul>
		 *   <li>2枚の画像が出力される。</li>
		 *   <li>各画像のサイズは240x320ピクセルである。</li>
		 *   <li>出力された画像は、指定されたパスに "01_01.jpg", "01_02.jpg" として保存される。</li>
		 * </ul>
		 * </p>
		 * @throws IOException テストデータの読み込みまたは出力画像の書き込み中にエラーが発生した場合。
		 */
		@Test
		@DisplayName("480x320の画像を240x320の画像２枚に縦分割(右->左)")
		void test01_01() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/ImageConverterTest/Convert/test01");
			Path inputFilePath = testDataPath.resolve("480x320.jpg");
			BufferedImage inputImage = ImageIO.read(inputFilePath.toFile());
			List<BufferedImage> outputImageList = ImageConverter.convert(inputImage, 240, 320,
				ImageConverter.BindingSide.RIGHT,
				ImageConverter.CenterClipOption.OFF,
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

		/**
		 * 横長の入力画像（480x320ピクセル）を、指定された出力サイズ（240x320ピクセル）に合わせて
		 * 縦方向に2分割するテストです。
		 * <p>
		 * 設定：
		 * <ul>
		 *   <li>綴じ方向: 左 ({@link ImageConverter.BindingSide#LEFT}) - 分割時、左側の画像がリストの最初に来る</li>
		 *   <li>中央切り抜き: オフ ({@link ImageConverter.CenterClipOption#OFF})</li>
		 *   <li>縦横比処理モード: 分割 ({@link ImageConverter.ContraAspectMode#SPLIT})</li>
		 * </ul>
		 * 期待される結果：
		 * <ul>
		 *   <li>2枚の画像が出力される。</li>
		 *   <li>各画像のサイズは240x320ピクセルである。</li>
		 *   <li>出力された画像は、指定されたパスに "02_01.jpg", "02_02.jpg" として保存される。</li>
		 * </ul>
		 * </p>
		 * @throws IOException テストデータの読み込みまたは出力画像の書き込み中にエラーが発生した場合。
		 */
		@Test
		@DisplayName("480x320の画像を240x320の画像２枚に縦分割(左->右)")
		void test01_02() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/ImageConverterTest/Convert/test01");
			Path inputFilePath = testDataPath.resolve("480x320.jpg");
			BufferedImage inputImage = ImageIO.read(inputFilePath.toFile());
			List<BufferedImage> outputImageList = ImageConverter.convert(inputImage, 240, 320,
				ImageConverter.BindingSide.LEFT,
				ImageConverter.CenterClipOption.OFF,
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

		/**
		 * 横長の入力画像（480x320ピクセル）を、指定された出力サイズ（120x160ピクセル）に合わせて
		 * 縦方向に2分割するテストです。このケースでは、出力画像の縦横比が入力画像の半分（分割後）の縦横比と等しくなります。
		 * <p>
		 * 設定：
		 * <ul>
		 *   <li>綴じ方向: 左 ({@link ImageConverter.BindingSide#LEFT})</li>
		 *   <li>中央切り抜き: オフ ({@link ImageConverter.CenterClipOption#OFF})</li>
		 *   <li>縦横比処理モード: 分割 ({@link ImageConverter.ContraAspectMode#SPLIT})</li>
		 * </ul>
		 * 期待される結果：
		 * <ul>
		 *   <li>2枚の画像が出力される。</li>
		 *   <li>各画像のサイズは120x160ピクセルである（アスペクト比を維持して縮小される）。</li>
		 *   <li>出力された画像は、指定されたパスに "03_01.jpg", "03_02.jpg" として保存される。</li>
		 * </ul>
		 * </p>
		 * @throws IOException テストデータの読み込みまたは出力画像の書き込み中にエラーが発生した場合。
		 */
		@Test
		@DisplayName("480x320の画像を120x160の画像２枚に縦分割(指定サイズは120x160=等アスペクト比)")
		void test01_03() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/ImageConverterTest/Convert/test01");
			Path inputFilePath = testDataPath.resolve("480x320.jpg");
			BufferedImage inputImage = ImageIO.read(inputFilePath.toFile());
			List<BufferedImage> outputImageList = ImageConverter.convert(inputImage, 120, 160,
				ImageConverter.BindingSide.LEFT,
				ImageConverter.CenterClipOption.OFF,
				ImageConverter.ContraAspectMode.SPLIT);
			assertAll("outputImageList-width-height",
				() -> assertEquals(120, outputImageList.get(0).getWidth()),
				() -> assertEquals(160, outputImageList.get(0).getHeight()),
				() -> assertEquals(120, outputImageList.get(1).getWidth()),
				() -> assertEquals(160, outputImageList.get(1).getHeight()));
			Path outputDirPath = testDataPath.resolve("outputFiles");
			ImageConverter.writeJpegFile(outputImageList.get(0), outputDirPath.resolve("03_01.jpg").toAbsolutePath().toString(), 30);
			ImageConverter.writeJpegFile(outputImageList.get(1), outputDirPath.resolve("03_02.jpg").toAbsolutePath().toString(), 30);
		}
		
		/**
		 * 横長の入力画像（480x320ピクセル）を、指定された出力サイズ（幅120ピクセル、高さ320ピクセル）に合わせて
		 * 縦方向に2分割するテストです。このケースでは、出力画像の縦横比が入力画像の半分（分割後）の縦横比と異なります。
		 * <p>
		 * 設定：
		 * <ul>
		 *   <li>綴じ方向: 左 ({@link ImageConverter.BindingSide#LEFT})</li>
		 *   <li>中央切り抜き: オフ ({@link ImageConverter.CenterClipOption#OFF})</li>
		 *   <li>縦横比処理モード: 分割 ({@link ImageConverter.ContraAspectMode#SPLIT})</li>
		 * </ul>
		 * 期待される結果：
		 * <ul>
		 *   <li>2枚の画像が出力される。</li>
		 *   <li>各画像のサイズは120x160ピクセルである。
		 *       これは、入力画像（分割後240x320）を、指定された出力幅120に合わせてアスペクト比を維持して縮小した結果です。
		 *       （高さは320が指定されているが、アスペクト比維持のため160になる）</li>
		 *   <li>出力された画像は、指定されたパスに "04_01.jpg", "04_02.jpg" として保存される。</li>
		 * </ul>
		 * </p>
		 * @throws IOException テストデータの読み込みまたは出力画像の書き込み中にエラーが発生した場合。
		 */
		@Test
		@DisplayName("480x320の画像を120x160の画像２枚に縦分割(指定サイズは120x320=不等アスペクト比)")
		void test01_04() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/ImageConverterTest/Convert/test01");
			Path inputFilePath = testDataPath.resolve("480x320.jpg");
			BufferedImage inputImage = ImageIO.read(inputFilePath.toFile());
			List<BufferedImage> outputImageList = ImageConverter.convert(inputImage, 120, 320,
				ImageConverter.BindingSide.LEFT,
				ImageConverter.CenterClipOption.OFF,
				ImageConverter.ContraAspectMode.SPLIT);
			assertAll("outputImageList-width-height",
				() -> assertEquals(120, outputImageList.get(0).getWidth()),
				() -> assertEquals(160, outputImageList.get(0).getHeight()),
				() -> assertEquals(120, outputImageList.get(1).getWidth()),
				() -> assertEquals(160, outputImageList.get(1).getHeight()));
			Path outputDirPath = testDataPath.resolve("outputFiles");
			ImageConverter.writeJpegFile(outputImageList.get(0), outputDirPath.resolve("04_01.jpg").toAbsolutePath().toString(), 30);
			ImageConverter.writeJpegFile(outputImageList.get(1), outputDirPath.resolve("04_02.jpg").toAbsolutePath().toString(), 30);
		}
		
		/**
		 * 横長の入力画像（480x320ピクセル）を、指定された出力サイズ（320x480ピクセル）に合わせて
		 * 反時計回りに90度回転させるテストです。
		 * <p>
		 * 設定：
		 * <ul>
		 *   <li>綴じ方向: 左 ({@link ImageConverter.BindingSide#LEFT}) - このテストでは影響しない</li>
		 *   <li>中央切り抜き: オフ ({@link ImageConverter.CenterClipOption#OFF}) - このテストでは影響しない</li>
		 *   <li>縦横比処理モード: 回転 ({@link ImageConverter.ContraAspectMode#ROTATE})</li>
		 * </ul>
		 * 期待される結果：
		 * <ul>
		 *   <li>1枚の画像が出力される。</li>
		 *   <li>画像のサイズは320x480ピクセルである（入力画像を回転させ、指定サイズにリサイズ）。</li>
		 *   <li>出力された画像は、指定されたパスに "05.jpg" として保存される。</li>
		 * </ul>
		 * </p>
		 * @throws IOException テストデータの読み込みまたは出力画像の書き込み中にエラーが発生した場合。
		 */
		@Test
		@DisplayName("480x320の画像を反時計回りに90°回転させ320x480の画像に変換")
		void test01_05() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/ImageConverterTest/Convert/test01");
			Path inputFilePath = testDataPath.resolve("480x320.jpg");
			BufferedImage inputImage = ImageIO.read(inputFilePath.toFile());
			List<BufferedImage> outputImageList = ImageConverter.convert(inputImage, 320, 480,
				ImageConverter.BindingSide.LEFT,
				ImageConverter.CenterClipOption.OFF,
				ImageConverter.ContraAspectMode.ROTATE);
			assertAll("outputImageList-width-height",
				() -> assertEquals(320, outputImageList.get(0).getWidth()),
				() -> assertEquals(480, outputImageList.get(0).getHeight()));
			Path outputDirPath = testDataPath.resolve("outputFiles");
			ImageConverter.writeJpegFile(outputImageList.get(0), outputDirPath.resolve("05.jpg").toAbsolutePath().toString(), 30);
		}

		/**
		 * 縦長の入力画像（320x480ピクセル）を、指定された出力サイズ（320x240ピクセル）に合わせて
		 * 横方向に2分割するテストです。
		 * <p>
		 * 設定：
		 * <ul>
		 *   <li>綴じ方向: 右 ({@link ImageConverter.BindingSide#RIGHT}) - このテストでは影響しない（横分割のため）</li>
		 *   <li>中央切り抜き: オフ ({@link ImageConverter.CenterClipOption#OFF})</li>
		 *   <li>縦横比処理モード: 分割 ({@link ImageConverter.ContraAspectMode#SPLIT})</li>
		 * </ul>
		 * 期待される結果：
		 * <ul>
		 *   <li>2枚の画像が出力される。</li>
		 *   <li>各画像のサイズは320x240ピクセルである。リストの最初の画像が元画像の上半分、2番目が下半分に対応する。</li>
		 *   <li>出力された画像は、指定されたパスに "01_01.jpg", "01_02.jpg" として保存される。</li>
		 * </ul>
		 * </p>
		 * @throws IOException テストデータの読み込みまたは出力画像の書き込み中にエラーが発生した場合。
		 */
		@Test
		@DisplayName("320x480の画像を320x240の画像２枚に横分割(上->下)")
		void test02() throws IOException {
			Path testDataPath = Paths.get("src/test/resources/imaizm/imagebundler/ImageConverterTest/Convert/test02");
			Path inputFilePath = testDataPath.resolve("320x480.jpg");
			BufferedImage inputImage = ImageIO.read(inputFilePath.toFile());
			List<BufferedImage> outputImageList = ImageConverter.convert(inputImage, 320, 240,
				ImageConverter.BindingSide.RIGHT, // 横分割の場合、綴じ方向は影響しない
				ImageConverter.CenterClipOption.OFF,
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
