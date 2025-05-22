package imaizm.imagebundler;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * 画像の変換処理を行うユーティリティクラスです。
 * <p>
 * 主な機能は以下の通りです。
 * <ul>
 *   <li>画像の回転</li>
 *   <li>アスペクト比を維持した画像のリサイズ</li>
 *   <li>指定されたサイズへの画像リサイズ</li>
 *   <li>JPEGファイルへの書き出し（透過情報を持つ場合は白色で塗りつぶし）</li>
 *   <li>画像の透過ピクセルを指定色で塗りつぶし</li>
 *   <li>入力画像と出力サイズの縦横比が異なる場合の処理（分割、回転、サイズ入れ替え）</li>
 * </ul>
 * </p>
 */
public class ImageConverter {
	
	/**
	 * 入力画像と出力画像の縦横比が異なる場合の処理モードを定義します。
	 */
	public static enum ContraAspectMode {
		/** 画像を回転させず、指定された幅と高さの最大値を入れ替えて処理します。 */
		PLAIN,
		/** 画像を指定された縦横比に合わせて分割します。 */
		SPLIT,
		/** 画像を90度回転して処理します。 */
		ROTATE
	}

	/**
	 * 見開き処理時の綴じ方向を指定します。
	 */
	public static enum BindingSide {
		/** 右綴じ（画像が右から左へ配置されます）。 */
		RIGHT,
		/** 左綴じ（画像が左から右へ配置されます）。 */
		LEFT
	}

	/**
	 * 横長の画像を指定された縦横比で分割する際に、中央部分を切り抜くかどうかのオプションです。
	 */
	public static enum CenterClipOption {
		/** 中央部分を切り抜きます。 */
		ON,
		/** 中央部分を切り抜かず、そのまま分割します。 */
		OFF
	}
	/** {@link CenterClipOption#ON} の場合に切り抜く際の基準となる幅。デフォルトは1520ピクセル。 */
	public static int CenterClipWidth = 1520;
	/** {@link CenterClipOption#ON} の場合に切り抜く際の基準となる高さ。デフォルトは1080ピクセル。 */
	public static int CenterClipHeight = 1080;

	/**
	 * 指定された画像を、指定された幅と高さに変換します。
	 * <p>
	 * デフォルトでは、綴じ方向は右綴じ（{@link BindingSide#RIGHT}）、
	 * 中央切り抜きオプションはオン（{@link CenterClipOption#ON}）、
	 * 縦横比が異なる場合の処理モードは分割（{@link ContraAspectMode#SPLIT}）が使用されます。
	 * </p>
	 *
	 * @param srcImage 変換元の画像。
	 * @param width 変換後の画像の幅（ピクセル単位）。
	 * @param height 変換後の画像の高さ（ピクセル単位）。
	 * @return 変換後の画像のリスト。入力画像と出力サイズの縦横比によっては、複数の画像が返されることがあります。
	 */
	public static List<BufferedImage> convert(
		BufferedImage srcImage,
		int width,
		int height) {
		return convert(srcImage, width, height, BindingSide.RIGHT);
	}
	
	/**
	 * 指定された画像を、指定された幅、高さ、および綴じ方向に変換します。
	 * <p>
	 * デフォルトでは、中央切り抜きオプションはオン（{@link CenterClipOption#ON}）、
	 * 縦横比が異なる場合の処理モードは分割（{@link ContraAspectMode#SPLIT}）が使用されます。
	 * </p>
	 *
	 * @param srcImage 変換元の画像。
	 * @param width 変換後の画像の幅（ピクセル単位）。
	 * @param height 変換後の画像の高さ（ピクセル単位）。
	 * @param side 見開き処理時の綴じ方向。
	 * @return 変換後の画像のリスト。入力画像と出力サイズの縦横比によっては、複数の画像が返されることがあります。
	 */
	public static List<BufferedImage> convert(
		BufferedImage srcImage,
		int width,
		int height,
		BindingSide side) {
		return convert(srcImage, width, height, side, CenterClipOption.ON);
	}

	/**
	 * 指定された画像を、指定された幅、高さ、綴じ方向、および中央切り抜きオプションで変換します。
	 * <p>
	 * デフォルトでは、縦横比が異なる場合の処理モードは分割（{@link ContraAspectMode#SPLIT}）が使用されます。
	 * </p>
	 *
	 * @param srcImage 変換元の画像。
	 * @param width 変換後の画像の幅（ピクセル単位）。
	 * @param height 変換後の画像の高さ（ピクセル単位）。
	 * @param side 見開き処理時の綴じ方向。
	 * @param centerClipOption 横長画像を分割する際の中央切り抜きオプション。
	 * @return 変換後の画像のリスト。入力画像と出力サイズの縦横比によっては、複数の画像が返されることがあります。
	 */
	public static List<BufferedImage> convert(
		BufferedImage srcImage,
		int width,
		int height,
		BindingSide side,
		CenterClipOption centerClipOption) {
		return convert(srcImage, width, height, side, centerClipOption, ContraAspectMode.SPLIT);
	}

	/**
	 * 指定された画像を、指定された幅、高さ、綴じ方向、中央切り抜きオプション、および縦横比処理モードで変換します。
	 * <p>
	 * このメソッドは、画像変換処理の主要なロジックを含みます。
	 * 入力画像と出力サイズの縦横比が異なる場合、指定された {@code mode} に従って処理が行われます。
	 * <ul>
	 *   <li>{@link ContraAspectMode#SPLIT}: 画像を指定されたアスペクト比に合わせて分割します。
	 *     <ul>
	 *       <li>出力が横長サイズ指定で入力画像が縦長の場合：入力画像を上下に2分割します。</li>
	 *       <li>出力が縦長サイズ指定で入力画像が横長の場合：入力画像を左右に2分割します。この際、{@code centerClipOption} が {@link CenterClipOption#ON ON} であれば、中央部分を {@link #CenterClipWidth} と {@link #CenterClipHeight} で定義されるアスペクト比で切り抜いてから分割します。{@code side} （綴じ方向）によって分割された画像の順序が変わります。</li>
	 *     </ul>
	 *   </li>
	 *   <li>{@link ContraAspectMode#PLAIN}: 指定された出力の幅と高さを入れ替えた上で、入力画像をリサイズします。縦横比のミスマッチを回転させずに解決しようとします。</li>
	 *   <li>{@link ContraAspectMode#ROTATE}: 入力画像を反時計回りに90度回転させた後、指定された幅と高さにリサイズします。</li>
	 * </ul>
	 * 最終的に、すべての処理済み画像（分割された場合は各画像）は、目標の幅と高さに合うようにアスペクト比を維持してリサイズされます。
	 * </p>
	 *
	 * @param srcImage 変換元の画像。
	 * @param width 変換後の画像の目標幅（ピクセル単位）。
	 * @param height 変換後の画像の目標高さ（ピクセル単位）。
	 * @param side 見開き処理時の綴じ方向（主に {@link ContraAspectMode#SPLIT} で横長画像を分割する場合に使用）。
	 * @param centerClipOption 横長画像を分割する際の中央切り抜きオプション（主に {@link ContraAspectMode#SPLIT} で使用）。
	 * @param mode 入力画像と出力画像の縦横比が異なる場合の処理モード。
	 * @return 変換後の画像のリスト。モードや入力画像の特性によって、リストには1つまたは複数の画像が含まれます。
	 */
	public static List<BufferedImage> convert(
		BufferedImage srcImage,
		int width,
		int height,
		BindingSide side,
		CenterClipOption centerClipOption,
		ContraAspectMode mode) {
		
		LinkedList<BufferedImage> bufferedImageList = new LinkedList<BufferedImage>();
		
		// 変換サイズ指定が横長に対して入力画像が縦長の場合 もしくは
		// 変換サイズ指定が縦長に対して入力画像が横長の場合
		if (width > height && srcImage.getWidth() < srcImage.getHeight() ||
			width < height && srcImage.getWidth() > srcImage.getHeight()) {

			// 分割で対応する場合
			if (mode == ContraAspectMode.SPLIT) {
				
				// 変換サイズ指定が横長の場合
				if (width > height) {
					int halfValue = srcImage.getHeight() / 2;
					int offsetValue = (srcImage.getHeight() % 2 == 0) ? 0 : 1;
					
					bufferedImageList.add(srcImage.getSubimage(0, 0, srcImage.getWidth(), halfValue));
					bufferedImageList.add(srcImage.getSubimage(0, halfValue, srcImage.getWidth(), halfValue + offsetValue));

				// 変換サイズ指定が縦長の場合
				} else {
					BufferedImage leftImage = null;
					BufferedImage rightImage = null;

					// 中央切り抜きオプション：ON
					if (centerClipOption == CenterClipOption.ON) {
						int originalWidth = srcImage.getWidth();
						int originalHeight = srcImage.getHeight();
						int clippingWidth = (int)((double)originalHeight * ((double)CenterClipWidth / (double)CenterClipHeight));
						int leftPadding = (originalWidth - clippingWidth) / 2;

						int halfValue = clippingWidth / 2;
						int offsetValue = (clippingWidth % 2 == 0) ? 0 : 1;
						leftImage = srcImage.getSubimage(leftPadding, 0, halfValue, originalHeight);
						rightImage = srcImage.getSubimage(leftPadding + halfValue, 0, halfValue + offsetValue, originalHeight);
					// 中央切り抜きオプション：OFF
					} else {
						int halfValue = srcImage.getWidth() / 2;
						int offsetValue = (srcImage.getWidth() % 2 == 0) ? 0 : 1;
						leftImage = srcImage.getSubimage(0, 0, halfValue, srcImage.getHeight());
						rightImage = srcImage.getSubimage(halfValue, 0, halfValue + offsetValue, srcImage.getHeight());
					}
					if (side == BindingSide.RIGHT) {
						bufferedImageList.add(rightImage);
						bufferedImageList.add(leftImage);
					} else {
						bufferedImageList.add(leftImage);
						bufferedImageList.add(rightImage);
					}
				}
				
				
			} else if (mode == ContraAspectMode.PLAIN) {
				// 最大幅・高さ入れ替えモード
				
				int temp = width;
				width = height;
				height = temp;
				
				bufferedImageList.add(srcImage);
			} else {
				// 画像回転モード
				
				// 画像を1/4回転
				srcImage = rotate(srcImage);
				
				bufferedImageList.add(srcImage);
			}
		} else {
			bufferedImageList.add(srcImage);
		}


		for (int i=0; i<bufferedImageList.size(); i++) {
			BufferedImage bufferedImage = bufferedImageList.get(i);
			// 縮小後の幅・高さ値を取得
			Dimension dimension = getScaledDimension(width, height, bufferedImage.getWidth(), bufferedImage.getHeight());
			bufferedImageList.set(i, resize(bufferedImage, dimension.width, dimension.height));
		}

		
		return bufferedImageList;
	}

	/**
	 * 指定された画像を反時計回りに90度回転します。
	 * <p>
	 * 新しい {@link BufferedImage} を作成し、アフィン変換を使用して元の画像を回転させて描画します。
	 * 回転後の画像の幅と高さは、元の画像の高さと幅にそれぞれ対応します。
	 * </p>
	 *
	 * @param srcImage 回転する元の画像。
	 * @return 反時計回りに90度回転された新しい {@link BufferedImage}。
	 */
	private static BufferedImage rotate(BufferedImage srcImage) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		BufferedImage newImage = new BufferedImage(height, width, srcImage.getType());
		AffineTransform affineTransform =
			AffineTransform.getRotateInstance(
				(-Math.PI / 2), 0, 0);
		Graphics2D newImageGraphics2d = (Graphics2D) newImage.getGraphics();
		newImageGraphics2d.setTransform(affineTransform);
		newImageGraphics2d.drawImage(
			srcImage, -width, 0, null);

		newImageGraphics2d.dispose();
		return newImage;
	}

	/**
	 * 入力された幅と高さの値を、アスペクト比を維持しつつ、指定された最大の幅と高さの制約内に収まるように縮小します。
	 * <p>
	 * 幅と高さのそれぞれについて、入力値と最大値の比率を計算します。
	 * これら2つの比率のうち小さい方を共通の縮小率として採用し、入力された幅と高さに適用します。
	 * これにより、元の画像のアスペクト比を保ったまま、指定された最大寸法内に収まる新しい寸法が得られます。
	 * </p>
	 *
	 * @param maxWidth 許容される最大の幅（ピクセル単位）。
	 * @param maxHeight 許容される最大の高さ（ピクセル単位）。
	 * @param width 縮小対象の元の幅（ピクセル単位）。
	 * @param height 縮小対象の元の高さ（ピクセル単位）。
	 * @return アスペクト比を維持し、指定された最大幅と最大高さ以内に縮小された新しい {@link Dimension} オブジェクト。
	 */
	private static Dimension getScaledDimension(
		int maxWidth,
		int maxHeight,
		int width,
		int height) {
		
		// 幅・高さそれぞれの入力値と最大値の比率を取得する
		double widthScale = (double) maxWidth / (double) width;
		double heightScale = (double) maxHeight / (double) height;
		// 幅・高さそれぞれの比率のうち小さい方を共通の縮小率として採用
		double scale = widthScale >= heightScale ? heightScale : widthScale;
		// 共通の縮小率で幅・高さの縮小値を算出
		int scaledWidth = (int) (width * scale);
		int scaledHeight = (int) (height * scale);
		// 縮小済みの幅・高さをDimensionインスタンスとして返却
		return new Dimension(scaledWidth, scaledHeight);
	}

	/**
	 * 指定された画像を、指定された幅と高さにリサイズします。
	 * <p>
	 * 新しい {@link BufferedImage} を作成し、元の画像をスケーリングして描画します。
	 * 元の画像のタイプが不明（0）の場合は、{@code BufferedImage.TYPE_4BYTE_ABGR_PRE} を使用します。
	 * スケーリングアルゴリズムには、{@code Image.SCALE_SMOOTH} (16) が使用されます。
	 * </p>
	 *
	 * @param srcImage リサイズする元の画像。
	 * @param width リサイズ後の画像の幅（ピクセル単位）。
	 * @param height リサイズ後の画像の高さ（ピクセル単位）。
	 * @return 指定された幅と高さにリサイズされた新しい {@link BufferedImage}。
	 */
	private static BufferedImage resize(BufferedImage srcImage, int width, int height) {

		int newImageType = (srcImage.getType() == 0) ? BufferedImage.TYPE_4BYTE_ABGR_PRE : srcImage.getType();

		BufferedImage newImage =
//			new BufferedImage(width, height, srcImage.getType());
			new BufferedImage(width, height, newImageType);

		Graphics newImageGraphics = newImage.getGraphics();
		newImageGraphics.drawImage(
			srcImage.getScaledInstance(width, height, 16), // Image.SCALE_SMOOTH
			0, 0, width, height, null);
		newImageGraphics.dispose();
		
		return newImage;
	}

	/**
	 * 指定された {@link BufferedImage} をJPEGファイルとして書き込みます。
	 * <p>
	 * 画像が透過情報を持つ場合、透過部分を白色で塗りつぶしてからJPEGに変換します。
	 * JPEGの圧縮品質はパーセンテージで指定します（例: 75は75%の品質）。
	 * このメソッドは、利用可能な最初のJPEG {@link ImageWriter} を使用します。
	 * </p>
	 *
	 * @param inputBufferedImage 書き込む対象の画像データ。
	 * @param outputFileName 出力するJPEGファイルのパスとファイル名。
	 * @param compressionQualityPercentage JPEGの圧縮品質（0から100の範囲、100が最高品質）。
	 * @return 書き込まれたJPEGファイルの {@link File} オブジェクト。
	 * @throws IOException ファイル書き込み中にエラーが発生した場合。
	 */
	public static File writeJpegFile(
		BufferedImage inputBufferedImage,
		String outputFileName,
		int compressionQualityPercentage)
		throws IOException {
		
		// 変換元画像が透過情報を持っている場合、透過情報を白色に置き換える
		if (inputBufferedImage.getColorModel().getTransparency() != Transparency.OPAQUE) {
			inputBufferedImage = fillTransparentPixels(inputBufferedImage, Color.WHITE);
		}
		
		File outputFile = new File(outputFileName);
		float compressionQuality = compressionQualityPercentage / 100F;
		ImageWriter imageWriter = null; // 初期化
		ImageWriteParam imageWriteParam = null; // 初期化
		
		Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName("jpg");
		if (imageWriters.hasNext()) { // ImageWriterが存在するか確認
			imageWriter = imageWriters.next();
			ImageOutputStream imageOutputStream = null; // 初期化
			try {
				imageOutputStream = ImageIO.createImageOutputStream(outputFile);
				imageWriter.setOutput(imageOutputStream);
				imageWriteParam = imageWriter.getDefaultWriteParam();
				imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); // MODE_EXPLICIT = 2
				imageWriteParam.setCompressionQuality(compressionQuality);
				imageWriter.write(null, new IIOImage(inputBufferedImage, null, null), imageWriteParam);
			} finally {
				if (imageOutputStream != null) {
					try {
						imageOutputStream.close(); // imageOutputStreamをクローズ
					} catch (IOException e) {
						// クローズ時のエラーハンドリング（必要に応じてログ出力など）
						e.printStackTrace();
					}
				}
				if (imageWriter != null) {
					imageWriter.dispose(); // imageWriterを解放
				}
			}
		} else {
			throw new IOException("No JPEG ImageWriter found"); // JPEGライターが見つからない場合のエラー
		}

		return outputFile;
	}

	/**
	 * 指定された {@link BufferedImage} の透過ピクセルを指定された色で塗りつぶします。
	 * <p>
	 * 新しい {@code BufferedImage} を {@code BufferedImage.TYPE_INT_RGB} 形式で作成し、
	 * 指定された {@code fillColor} で背景全体を塗りつぶした後、元の画像をその上に描画します。
	 * これにより、元の画像が持っていた可能性のある透過情報（アルファチャンネルなど）は失われ、
	 * 透過していた領域は {@code fillColor} で置き換えられます。
	 * </p>
	 *
	 * @param inputBufferdImage 透過ピクセルを塗りつぶす対象の画像。
	 * @param fillColor 透過ピクセルを塗りつぶすために使用する色。
	 * @return 透過ピクセルが指定された色で塗りつぶされた新しい {@link BufferedImage}。
	 *         この新しい画像は常に {@code BufferedImage.TYPE_INT_RGB} 型です。
	 */
	public static BufferedImage fillTransparentPixels(
		BufferedImage inputBufferdImage, 
		Color fillColor) {
		int w = inputBufferdImage.getWidth();
		int h = inputBufferdImage.getHeight();
		BufferedImage outputBufferdImage =
			new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = outputBufferdImage.createGraphics();
		try {
			g.setColor(fillColor);
			g.fillRect(0,0,w,h);
			g.drawRenderedImage(inputBufferdImage, null);
		} finally {
			g.dispose();
		}
		return outputBufferdImage;
	}
	
}
