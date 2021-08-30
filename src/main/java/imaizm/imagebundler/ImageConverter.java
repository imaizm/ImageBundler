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

public class ImageConverter {
	
	public static enum ContraAspectMode {
		PLAIN,
		SPLIT,
		ROTATE
	}

	public static enum BindingSide {
		RIGHT,
		LEFT
	}

	public static enum CenterClipOption {
		ON,
		OFF
	}
	public static int CenterClipWidth = 1520;
	public static int CenterClipHeight = 1080;

	public static List<BufferedImage> convert(
		BufferedImage srcImage,
		int width,
		int height) {
		return convert(srcImage, width, height, BindingSide.RIGHT);
	}
	
	public static List<BufferedImage> convert(
		BufferedImage srcImage,
		int width,
		int height,
		BindingSide side) {
		return convert(srcImage, width, height, side, CenterClipOption.ON);
	}

	public static List<BufferedImage> convert(
		BufferedImage srcImage,
		int width,
		int height,
		BindingSide side,
		CenterClipOption centerClipOption) {
		return convert(srcImage, width, height, side, centerClipOption, ContraAspectMode.SPLIT);
	}

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
	 * 入力された幅・高さ値を、アスペクト比を保ちつつ、指定された最大幅・高さ以内に縮小した値に変換し返す。
	 * @param maxWidth 最大幅
	 * @param maxHeight 最大高さ
	 * @param width 入力幅
	 * @param height 入力高さ
	 * @return 最大幅・高さ以内に縮小された幅・高さ値
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

	private static BufferedImage resize(BufferedImage srcImage, int width, int height) {

		int newImageType = (srcImage.getType() == 0) ? BufferedImage.TYPE_4BYTE_ABGR_PRE : srcImage.getType();

		BufferedImage newImage =
//			new BufferedImage(width, height, srcImage.getType());
			new BufferedImage(width, height, newImageType);

		Graphics newImageGraphics = newImage.getGraphics();
		newImageGraphics.drawImage(
			srcImage.getScaledInstance(width, height, 16),
			0, 0, width, height, null);
		newImageGraphics.dispose();
		
		return newImage;
	}

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
		ImageWriter imageWriter;
		ImageWriteParam imageWriteParam;
		
		for (
			Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName("jpg");
			imageWriters.hasNext();
			imageWriter.write(null, new IIOImage(inputBufferedImage, null, null), imageWriteParam)) {
			
			ImageOutputStream imageOutputStream =
				ImageIO.createImageOutputStream(outputFile);
			imageWriter = imageWriters.next();
			imageWriter.setOutput(imageOutputStream);
			imageWriteParam = imageWriter.getDefaultWriteParam();
			imageWriteParam.setCompressionMode(2);
			imageWriteParam.setCompressionQuality(compressionQuality);
		}

		return outputFile;
	}

	// 透過情報をfillColorに置き換えたBufferedImageを返却
	public static BufferedImage fillTransparentPixels(
		BufferedImage inputBufferdImage, 
		Color fillColor) {
		int w = inputBufferdImage.getWidth();
		int h = inputBufferdImage.getHeight();
		BufferedImage outputBufferdImage =
			new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = outputBufferdImage.createGraphics();
		g.setColor(fillColor);
		g.fillRect(0,0,w,h);
		g.drawRenderedImage(inputBufferdImage, null);
		g.dispose();
		return outputBufferdImage;
	}
	
}
