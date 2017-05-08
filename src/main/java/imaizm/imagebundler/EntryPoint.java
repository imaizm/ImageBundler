package imaizm.imagebundler;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.ProgressMonitor;

public class EntryPoint {
	/** 作業用ディレクトリのパスを格納 */
	private String tempDirectoryPath;
	/** 作業量ディレクトリに作成する仮ディレクトリのディレクトリ名を格納 */
	private String workDirectoryName;
	/** 仮ディレクトリ操作用のFileオブジェクト*/
	private File workDirectory;

	/**
	 * デフォルトコンストラクタ
	 */
	public EntryPoint() {
		
		// 作業ディレクトリ、仮ディレクトリのセットアップ
	//	tempDirectoryPath = System.getenv("TEMP");
		// 作業ディレクトリ：実行時OSのTEMPディレクトリを取得
		tempDirectoryPath = System.getProperty("java.io.tmpdir");
		// 仮ディレクトリ：日時＋乱数５桁
		workDirectoryName = 
			(new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date()) +
			(new DecimalFormat("#####")).format((int) (Math.random() * 100000D));
		// 仮ディレクトリ操作用Fileオブジェクト
		workDirectory = new File(tempDirectoryPath + File.separator + workDirectoryName);
		// 仮ディレクトリの作成
		workDirectory.mkdir();
		
		try {
			System.out.println("input file info...");
			System.out.println("File#getAbsoluteFile (work) : " + workDirectory.getAbsoluteFile());
		//	System.out.println("File#getAbsolutePath  : " + workDirectory.getAbsolutePath());
		//	System.out.println("File#getCanonicalFile : " + workDirectory.getCanonicalFile());
		//	System.out.println("File#getCanonicalPath : " + workDirectory.getCanonicalPath());
		//	System.out.println("File#getName          : " + workDirectory.getName());
		//	System.out.println("File#getParent        : " + workDirectory.getParent());
		//	System.out.println("File#getParentFile    : " + workDirectory.getParentFile());
		//	System.out.println("File#getPath          : " + workDirectory.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void convert(File inputFile, int width, int height)
		throws IOException {
		
		// 入力ソースの拡張子を取得
		String inputFileExtension =
			inputFile.getName().substring(inputFile.getName().length() - 3, inputFile.getName().length());
		
		// 入力ソースがディレクトリだった場合
		if (inputFile.isDirectory()) {
			
			System.out.println("work directory : " + workDirectory.getAbsolutePath());

			// 入力ソースのディレクトリからJpegファイルを取得
			File[] inputFiles = inputFile.listFiles(
				new FileFilter() {
					public boolean accept(File file) {
						if (file.isFile() && (
								file.getName().toLowerCase().endsWith("jpg") ||
								file.getName().toLowerCase().endsWith("jpeg"))) {
							return true;
						} else {
							return false;
						}
					}
				});
			
			convert(inputFile, inputFiles, width, height);
			
			// -------------
			String outputZipFileName =
					inputFile.getParent() +
					File.separator +
					inputFile.getName() +
					".zip";

				System.out.println("output zip file name : " + outputZipFileName);

				File outputZipFile =
					store(inputFiles, outputZipFileName);
			// -------------

			

		// 入力ソースがZipファイルだった場合
		} else if (inputFileExtension.equalsIgnoreCase("zip")) {
			
			// 解答用仮ディレクトリの作成
			File extractDirectory = new File(workDirectory.getPath() + "_extract");
			extractDirectory.mkdir();
			
			// 入力ソースのZipファイルを解凍し、解凍されたファイル群からJpegファイルを取得
			File[] inputFiles = inflate(inputFile, extractDirectory);
			
			// 対象となるJpegファイル群を変換
			convert(inputFile, inputFiles, width, height);
			
			// 処理済みの解凍ファイルを削除
			for (int i=0; i<inputFiles.length; i++) {
				inputFiles[i].delete();
			}
			extractDirectory.delete();
			
		// 入力ソースがディレクトリでもZipファイルでもなかった場合
		} else {
			
			// 異常終了
			throw new RuntimeException("未対応のファイル形式です。");
		}
	}
	
	private void convert(File inputFile, File[] inputFiles, int width, int height) throws IOException {
		
		ArrayList outputFileList = new ArrayList();
		int index = 1;
		DecimalFormat decimalFormat = createDecimalFormatObject(inputFiles.length);
		
		// 処理中ダイアログ
		ProgressMonitor progressMonitor = new ProgressMonitor(null, "変換処理中", "ノート", 0, inputFiles.length);
		progressMonitor.setMillisToDecideToPopup(0);
		
		for (int i = 0; i < inputFiles.length; i++) {
			
			progressMonitor.setNote((i+1) + " of " + inputFiles.length);
			
		//	System.out.println((i + 1) + " of " + inputFiles.length);

			BufferedImage bufferedImage = ImageIO.read(inputFiles[i]);
			if (bufferedImage != null) {
				
				// 
				if (width > height && bufferedImage.getWidth() < bufferedImage.getHeight() ||
					width < height && bufferedImage.getWidth() > bufferedImage.getHeight()) {
					
					// TODO 暫定的にモード固定
					if (true) {
						// 最大幅・高さ入れ替えモード
						
						int temp = width;
						width = height;
						height = width;
					} else {
						// 画像回転モード
						
						// 画像を1/4回転
						bufferedImage = rotate(bufferedImage);
					}
				}

				// 縮小後の幅・高さ値を取得
				Dimension dimension = getScaledDimension(width, height, bufferedImage.getWidth(), bufferedImage.getHeight());
				bufferedImage = resize(bufferedImage, dimension.width, dimension.height);
				String outputFileName = 
					workDirectory.getAbsolutePath() +
					File.separator +
					inputFiles[i].getName();
//					inputFile.getName() +
//					"_" +
//					decimalFormat.format(index++) + ".jpg";
				File outputFile = writeJpegFile(bufferedImage,
						outputFileName, 75);
				outputFileList.add(outputFile);
			}
			
			progressMonitor.setProgress(i+1);
		}

		String inputFileName;
		if (inputFile.isFile()) {
			inputFileName =
				inputFile.getName().substring(
					0,
					inputFile.getName().lastIndexOf("."));
		} else {
			inputFileName = inputFile.getName();
		}
		
		String outputZipFileName =
			inputFile.getParent() +
			File.separator +
			inputFileName +
			"_reduced.zip";

		System.out.println("output zip file name : " + outputZipFileName);

		File outputZipFile =
			store((File[])outputFileList.toArray(new File[0]), outputZipFileName);
		
		// 一時ファイルを削除
		for (Iterator it=outputFileList.iterator(); it.hasNext();) {
			((File)it.next()).delete();
		}
		workDirectory.delete();
	}

	private DecimalFormat createDecimalFormatObject(int length) {
		int digit = String.valueOf(length).length();
		StringBuffer formatStringBuffer = new StringBuffer();
		for (int i = 0; i < digit; i++)
			formatStringBuffer.append("0");

		DecimalFormat decimalFormat = new DecimalFormat(formatStringBuffer
				.toString());
		return decimalFormat;
	}

	private BufferedImage rotate(BufferedImage srcImage) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		BufferedImage newImage = new BufferedImage(height, width, srcImage.getType());
		double x = (double)width / 2D;
		double y = (double)height / 2D;
		AffineTransform affineTransform =
			AffineTransform.getRotateInstance(
				(-Math.PI / 2), 0, 0);
		Graphics2D newImageGraphics2d = (Graphics2D) newImage.getGraphics();
		newImageGraphics2d.setTransform(affineTransform);
		newImageGraphics2d.drawImage(
			srcImage, -width, 0, null);
	//	newImageGraphics2d.drawImage(srcImage, affineTransform, null);

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
	private Dimension getScaledDimension(
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

	private BufferedImage resize(BufferedImage srcImage, int width, int height) {
		BufferedImage newImage = new BufferedImage(width, height, srcImage
				.getType());
		Graphics newImageGraphics = newImage.getGraphics();
		newImageGraphics.drawImage(srcImage
				.getScaledInstance(width, height, 16), 0, 0, width, height,
				null);
		newImageGraphics.dispose();
		return newImage;
	}

	private File writeJpegFile(BufferedImage inputBufferedImage,
			String outputFileName, int compressionQualityPercentage)
			throws IOException {
		File outputFile = new File(outputFileName);
		float compressionQuality = (float) compressionQualityPercentage / 100F;
		ImageWriter imageWriter;
		ImageWriteParam imageWriteParam;
		for (
			Iterator imageWriters = ImageIO.getImageWritersByFormatName("jpg");
			imageWriters.hasNext();
			imageWriter.write(null, new IIOImage(inputBufferedImage, null, null), imageWriteParam)) {
			ImageOutputStream imageOutputStream =
				ImageIO.createImageOutputStream(outputFile);
			imageWriter = (ImageWriter) imageWriters.next();
			imageWriter.setOutput(imageOutputStream);
			imageWriteParam = imageWriter.getDefaultWriteParam();
			imageWriteParam.setCompressionMode(2);
			imageWriteParam.setCompressionQuality(compressionQuality);
		}

		return outputFile;
	}

	private File store(File targetFiles[], String outputFileName)
			throws IOException {
		File outputFile = new File(outputFileName);
		ZipArchiveOutputStream zipOutputStream =
			new ZipArchiveOutputStream(
				new BufferedOutputStream(
					new FileOutputStream(outputFile)));
		zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
		zipOutputStream.setEncoding("MS932");
		for (int i = 0; i < targetFiles.length; i++) {
			File targetFile = targetFiles[i];
			ZipArchiveEntry zipEntry = new ZipArchiveEntry(targetFile.getName());
			zipEntry.setTime(targetFile.lastModified());
			zipEntry.setSize(targetFile.length());
			CRC32 crc = new CRC32();
			BufferedInputStream bufferedInputStream =
				new BufferedInputStream(
						new FileInputStream(targetFile));

			byte buffer[] = new byte[4096];
			int readSize;
			int totalReadSize = 0;
			while ((readSize = bufferedInputStream.read(buffer)) != -1) {
				totalReadSize += readSize;
				crc.update(buffer, 0, readSize);
			}
			bufferedInputStream.close();
			
			zipEntry.setCrc(crc.getValue());
			zipEntry.setSize(totalReadSize);
			
			zipOutputStream.putArchiveEntry(zipEntry);
			
			bufferedInputStream =
				new BufferedInputStream(
					new FileInputStream(targetFile));
			
			while ((readSize = bufferedInputStream.read(buffer)) != -1) {
				zipOutputStream.write(buffer, 0, readSize);
			}
			bufferedInputStream.close();
			
			zipOutputStream.closeArchiveEntry();
		}

		zipOutputStream.close();
		return outputFile;
	}
	
	private File deflate(File targetFiles[], String outputFileName)
			throws IOException {
		File outputFile = new File(outputFileName);
		ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputFile)));
		zipOutputStream.setLevel(0);
		zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
		zipOutputStream.setEncoding("MS932");
		for (int i = 0; i < targetFiles.length; i++) {
			File targetFile = targetFiles[i];
			ZipArchiveEntry zipEntry = new ZipArchiveEntry(targetFile.getName());
			zipEntry.setTime(targetFile.lastModified());
			zipEntry.setSize(targetFile.length());
			zipEntry.setCrc(0L);
			zipOutputStream.putArchiveEntry(zipEntry);
			CRC32 crc = new CRC32();
			CheckedInputStream checkedInputStream =
				new CheckedInputStream(
					new FileInputStream(targetFile), crc);
			long sizeResult = 0L;
			byte buffer[] = new byte[4096];
			for (int readSize = 0; (readSize = checkedInputStream.read(buffer)) > 0;) {
				sizeResult += readSize;
				zipOutputStream.write(buffer, 0, readSize);
			}

			checkedInputStream.close();
			zipEntry.setCompressedSize(sizeResult);
			zipEntry.setCrc(crc.getValue());
			zipOutputStream.closeArchiveEntry();
		}

		zipOutputStream.close();
		return outputFile;
	}
	
	
	private File[] inflate(File targetZipFile, File outputDirectory) throws IOException {
		List outputFileList = new ArrayList();
		
		ZipFile zipFile = new ZipFile(targetZipFile, "MS932");
		Enumeration zipEntries = zipFile.getEntries();
		while (zipEntries.hasMoreElements()) {
			ZipArchiveEntry zipEntry = (ZipArchiveEntry)zipEntries.nextElement();
			if (! zipEntry.isDirectory()) {
				if (zipEntry.getName().toLowerCase().endsWith("jpg") ||
					zipEntry.getName().toLowerCase().endsWith("jpeg")) {
					
					String outputFileName =
						zipEntry.getName().substring(
							zipEntry.getName().lastIndexOf("/") + 1);
					
					File outputFile = new File(outputDirectory, outputFileName);
					outputFileList.add(outputFile);
					FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
					
					InputStream inputStream = zipFile.getInputStream(zipEntry);
					byte[] buffer = new byte[1024];
					int readSize;
					while ((readSize = inputStream.read(buffer)) != -1) {
						fileOutputStream.write(buffer, 0, readSize);
					}
					
					inputStream.close();
					fileOutputStream.close();
				}
			}
		}
		
		return (File[])outputFileList.toArray(new File[0]);
	}

	public static void main(String args[]) {
		// デフォルトリターンコード＝１
		int returnCode = Constants.RETURN_CODE_ERROR;
		
		try {
			returnCode = execute(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(returnCode);
	}

	public static int execute(String args[]) throws IOException {
		// デフォルトリターンコード＝１
		int returnCode = Constants.RETURN_CODE_ERROR;
		
		//
		// 対象ファイルの取得
		//
		String filePath;
		
		// 引数にて対象ファイルの指定がなかった場合
		if (args.length == 0) {

			// iniファイルが有ればそこから前回の作業ディレクトリを取得
			IniFileHandler iniFileHandler = new IniFileHandler();
			File currentDirectoryForJFileChooser = iniFileHandler.getWorkDirectoryOfLastTime();
			
			String parentDirectoryOfSelectedFile = null;
			
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			
			if (currentDirectoryForJFileChooser != null) {
				jFileChooser.setCurrentDirectory(currentDirectoryForJFileChooser);
			}
			
			int state = jFileChooser.showOpenDialog(null);
			if (state == JFileChooser.APPROVE_OPTION) {
				filePath = jFileChooser.getSelectedFile().getAbsolutePath();
				parentDirectoryOfSelectedFile = jFileChooser.getSelectedFile().getParent();
			} else {
				return returnCode;
			}
			
			iniFileHandler.writeWorkDirectoryOfLastTime(parentDirectoryOfSelectedFile);
			
		// 引数にて対象ファイルの指定があった場合
		} else if (args.length == 1) {
			filePath = args[0];
		
		// 引数の指定に誤りがある場合
		} else {
			System.out.println("エラー：引数が指定されていません。");
			return returnCode;
		}

		File inputFile = new File(filePath);
		if (!inputFile.exists()) {
			System.out.println("エラー：指定されたファイル/フォルダが存在しません。");
			return returnCode;
		}
		try {
			System.out.println("input file info...");
			System.out.println("File#getAbsoluteFile (src)  : " + inputFile.getAbsoluteFile());
		//	System.out.println("File#getAbsolutePath  : " + inputFile.getAbsolutePath());
		//	System.out.println("File#getCanonicalFile : " + inputFile.getCanonicalFile());
		//	System.out.println("File#getCanonicalPath : " + inputFile.getCanonicalPath());
		//	System.out.println("File#getName          : " + inputFile.getName());
		//	System.out.println("File#getParent        : " + inputFile.getParent());
		//	System.out.println("File#getParentFile    : " + inputFile.getParentFile());
		//	System.out.println("File#getPath          : " + inputFile.getPath());
		} catch (Exception e) {
			e.printStackTrace();
			return returnCode;
		}
		EntryPoint converter = new EntryPoint();
		converter.convert(inputFile, 768, 1024);
		return returnCode;
	}
}
