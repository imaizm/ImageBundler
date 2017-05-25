package imaizm.imagebundler;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

public class ImageConverter {
	
	public static enum ContraAspectMode {
		PLAIN,
		SPLIT,
		ROTATE
	}
	
	public static List<BufferedImage> convert(
		BufferedImage srcImage, int width, int height, ContraAspectMode mode) {
		
		LinkedList<BufferedImage> bufferedImageList = new LinkedList<BufferedImage>();
		
		if (width > height && srcImage.getWidth() < srcImage.getHeight() ||
			width < height && srcImage.getWidth() > srcImage.getHeight()) {
			
			// TODO 暫定的にモード固定
			if (mode == ContraAspectMode.SPLIT) {
				
				if (width > height) {
					int halfValue = srcImage.getHeight() / 2;
					int offsetValue = (srcImage.getHeight() % 2 == 0) ? 0 : 1;
					
					bufferedImageList.add(srcImage.getSubimage(0, 0, srcImage.getWidth(), halfValue));
					bufferedImageList.add(srcImage.getSubimage(0, halfValue, srcImage.getWidth(), halfValue + offsetValue));
				} else {
					int halfValue = srcImage.getWidth() / 2;
					int offsetValue = (srcImage.getWidth() % 2 == 0) ? 0 : 1;
					
					bufferedImageList.add(srcImage.getSubimage(0, 0, halfValue, srcImage.getHeight()));
					bufferedImageList.add(srcImage.getSubimage(halfValue, 0, halfValue + offsetValue, srcImage.getHeight()));
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

		for (BufferedImage bufferedImage : bufferedImageList) {
			// 縮小後の幅・高さ値を取得
			Dimension dimension = getScaledDimension(width, height, bufferedImage.getWidth(), bufferedImage.getHeight());
			bufferedImage = resize(bufferedImage, dimension.width, dimension.height);
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
		int scaledWidth = (int) ((double) width * scale);
		int scaledHeight = (int) ((double) height * scale);
		// 縮小済みの幅・高さをDimensionインスタンスとして返却
		return new Dimension(scaledWidth, scaledHeight);
	}

	private static BufferedImage resize(BufferedImage srcImage, int width, int height) {
		
		BufferedImage newImage =
			new BufferedImage(width, height, srcImage.getType());

		Graphics newImageGraphics = newImage.getGraphics();
		newImageGraphics.drawImage(
			srcImage.getScaledInstance(width, height, 16),
			0, 0, width, height, null);
		newImageGraphics.dispose();
		
		return newImage;
	}

}
