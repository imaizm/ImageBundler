package imaizm.imagebundler;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.ProgressMonitor;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;

public class EntryPoint {

	/**
	 * デフォルトコンストラクタ
	 */
	public EntryPoint() {
	}

	public void convert(Path inputFilePath, int width, int height)
		throws IOException {
		
		InputFileHandler inputFileHandler =
			new InputFileHandler(inputFilePath);
		
		this.convert(inputFilePath, inputFileHandler.getInputFilePathList(), width, height);
		
		// 入力ソースがディレクトリだった場合
		if (Files.isDirectory(inputFilePath)) {
			
			String outputZipFileName =
				inputFilePath.getParent().resolve(
					inputFilePath.getFileName().toString() + ".zip")
				.toAbsolutePath().toString();

			System.out.println("output zip file name : " + outputZipFileName);

			this.store(inputFileHandler.getInputFilePathList(), outputZipFileName);
		}
		
		inputFileHandler.close();
	}
	
	private void convert(Path inputFilePath, List<Path> inputFilePathList, int width, int height) throws IOException {
		
		Path workDirectoryPath = (new WorkDirectoryHandler()).getWorkDirectoryPath();
		
		ArrayList<Path> outputFilePathList = new ArrayList<Path>();
		
		// 処理中ダイアログ
		ProgressMonitor progressMonitor =
			new ProgressMonitor(
				null,
				"変換中 : " + inputFilePath.getFileName().toString(),
				"ノート",
				0,
				inputFilePathList.size());
		progressMonitor.setMillisToDecideToPopup(0);
		
		for (int i = 0; i < inputFilePathList.size(); i++) {
			
			progressMonitor.setNote((i+1) + " of " + inputFilePathList.size());

			BufferedImage bufferedImage = ImageIO.read(inputFilePathList.get(i).toFile());
			if (bufferedImage != null) {
				
				List<BufferedImage> convertedImageList = ImageConverter.convert(bufferedImage, width, height);
				
				int index = 0;
				for (BufferedImage convertedImage : convertedImageList) {
					index++;
					
					String outputFileName = 
							workDirectoryPath.toAbsolutePath().toString() +
							File.separator +
							FilenameUtils.getBaseName(
								inputFilePathList.get(i).getFileName().toString()) +
							((convertedImageList.size() == 1) ? "" : "_" + Integer.toString(index)) +
							".jpg";
						Path outputFilePath = writeJpegFile(convertedImage, outputFileName, 75);
						outputFilePathList.add(outputFilePath);
				}
				
			}
			
			progressMonitor.setProgress(i+1);
		}

		// 出力ファイル名のベース文字列の設定
		String outputFileNameBase;
		if (Files.isDirectory(inputFilePath)) {
			// 入力がディレクトリの場合、入力名をそのまま設定
			outputFileNameBase = inputFilePath.getFileName().toString();
		} else {
			// 入力がファイルの場合、入力名から拡張子を除去
			outputFileNameBase = FilenameUtils.getBaseName(inputFilePath.getFileName().toString());
		}
		
		String outputZipFileName =
			inputFilePath
				.getParent()
				.resolve(
					outputFileNameBase + "_reduced.zip")
				.toAbsolutePath().toString();

		System.out.println("output zip file name : " + outputZipFileName);

		this.store(outputFilePathList, outputZipFileName);
		
		// 一時ファイルを削除
		for (Path outputFilePath : outputFilePathList) {
			Files.delete(outputFilePath);
		}
		Files.delete(workDirectoryPath);
	}

/*
	private DecimalFormat createDecimalFormatObject(int length) {
		int digit = String.valueOf(length).length();
		StringBuffer formatStringBuffer = new StringBuffer();
		for (int i = 0; i < digit; i++)
			formatStringBuffer.append("0");

		DecimalFormat decimalFormat = new DecimalFormat(formatStringBuffer
				.toString());
		return decimalFormat;
	}
*/

	private Path writeJpegFile(
		BufferedImage inputBufferedImage,
		String outputFileName,
		int compressionQualityPercentage)
		throws IOException {
		
		// 変換元画像が透過情報を持っている場合、透過情報を白色に置き換える
		if (inputBufferedImage.getColorModel().getTransparency() != Transparency.OPAQUE) {
			inputBufferedImage = fillTransparentPixels(inputBufferedImage, Color.WHITE);
		}
		
		Path outputFilePath = Paths.get(outputFileName);
		float compressionQuality = (float) compressionQualityPercentage / 100F;
		ImageWriter imageWriter;
		ImageWriteParam imageWriteParam;
		
		for (
			Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName("jpg");
			imageWriters.hasNext();
			imageWriter.write(null, new IIOImage(inputBufferedImage, null, null), imageWriteParam)) {
			
			ImageOutputStream imageOutputStream =
				ImageIO.createImageOutputStream(outputFilePath.toFile());
			imageWriter = imageWriters.next();
			imageWriter.setOutput(imageOutputStream);
			imageWriteParam = imageWriter.getDefaultWriteParam();
			imageWriteParam.setCompressionMode(2);
			imageWriteParam.setCompressionQuality(compressionQuality);
		}

		return outputFilePath;
	}

	// 透過情報をfillColorに置き換えたBufferedImageを返却
	private BufferedImage fillTransparentPixels(
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
	
	private Path store(List<Path> targetFilePathList, String outputFileName)
		throws IOException {
		
		Path outputFilePath = Paths.get(outputFileName);
		try (ZipArchiveOutputStream zipOutputStream =
			new ZipArchiveOutputStream(
				new BufferedOutputStream(
					Files.newOutputStream(outputFilePath)))) {
			
			zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
			zipOutputStream.setEncoding("MS932");
			
			for (Path targetFilePath : targetFilePathList) {
				
				ZipArchiveEntry zipEntry = new ZipArchiveEntry(targetFilePath.getFileName().toString());
				zipEntry.setTime(Files.getLastModifiedTime(targetFilePath).toMillis());
				zipEntry.setSize(Files.size(targetFilePath));
				
				CRC32 crc = new CRC32();
				try (BufferedInputStream bufferedInputStream =
					new BufferedInputStream(
						Files.newInputStream(targetFilePath))) {
	
					byte buffer[] = new byte[4096];
					int readSize;
					int totalReadSize = 0;
					while ((readSize = bufferedInputStream.read(buffer)) != -1) {
						totalReadSize += readSize;
						crc.update(buffer, 0, readSize);
					}
					
					zipEntry.setCrc(crc.getValue());
					zipEntry.setSize(totalReadSize);
				} catch (IOException e) {
					throw e;
				}
				
				zipOutputStream.putArchiveEntry(zipEntry);
				
				try (BufferedInputStream bufferedInputStream =
					new BufferedInputStream(
						Files.newInputStream(targetFilePath))) {
				
					byte buffer[] = new byte[4096];
					int readSize;
					while ((readSize = bufferedInputStream.read(buffer)) != -1) {
						zipOutputStream.write(buffer, 0, readSize);
					}
				} catch (IOException e) {
					throw e;
				}
				
				zipOutputStream.closeArchiveEntry();
			}
		} catch (IOException e) {
			throw e;
		}
		return outputFilePath;
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
		List<File> targetFileList = new LinkedList<File>();
		
		// 引数にて対象ファイルの指定がなかった場合
		if (args.length == 0) {

			// iniファイルが有ればそこから前回の作業ディレクトリを取得
			IniFileHandler iniFileHandler = new IniFileHandler();
			Path currentDirectoryPathForJFileChooser = iniFileHandler.getWorkDirectoryPathOfLastTime();
			
			String parentDirectoryOfSelectedFile = null;
			
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			jFileChooser.setMultiSelectionEnabled(true);
			
			if (currentDirectoryPathForJFileChooser != null) {
				jFileChooser.setCurrentDirectory(currentDirectoryPathForJFileChooser.toFile());
			}
			
			int state = jFileChooser.showOpenDialog(null);
			if (state == JFileChooser.APPROVE_OPTION) {
				for(File targetFile : jFileChooser.getSelectedFiles()) {
					targetFileList.add(targetFile);
					parentDirectoryOfSelectedFile = targetFile.getParent();
				}
			} else {
				return returnCode;
			}
			
			iniFileHandler.writeWorkDirectoryOfLastTime(parentDirectoryOfSelectedFile);
			
		// 引数にて対象ファイルの指定があった場合
		} else if (args.length == 1) {
			String filePath = args[0];
			Path inputFilePath = Paths.get(filePath);
			if (Files.notExists(inputFilePath)) {
				System.out.println("エラー：指定されたファイル/フォルダが存在しません。");
				return returnCode;
			}
			targetFileList.add(inputFilePath.toFile());
		// 引数の指定に誤りがある場合
		} else {
			System.out.println("エラー：引数が指定されていません。");
			return returnCode;
		}

		// 処理中ダイアログ
		ProgressMonitor progressMonitor = new ProgressMonitor(null, "全体進捗", "ノート", 0, targetFileList.size());
		progressMonitor.setMillisToDecideToPopup(0);
		
		EntryPoint converter = new EntryPoint();
		for (int i=0; i<targetFileList.size(); i++) {
			
			progressMonitor.setNote((i+1) + " of " + targetFileList.size());
			
			File targetFile = targetFileList.get(i);
			
			System.out.println("input file info...");
			System.out.println("File#getAbsoluteFile (src)  : " + targetFile.getAbsoluteFile());
		//	System.out.println("File#getAbsolutePath  : " + targetFile.getAbsolutePath());
		//	System.out.println("File#getCanonicalFile : " + targetFile.getCanonicalFile());
		//	System.out.println("File#getCanonicalPath : " + targetFile.getCanonicalPath());
		//	System.out.println("File#getName          : " + targetFile.getName());
		//	System.out.println("File#getParent        : " + targetFile.getParent());
		//	System.out.println("File#getParentFile    : " + targetFile.getParentFile());
		//	System.out.println("File#getPath          : " + targetFile.getPath());
			
			converter.convert(targetFile.toPath(), 768, 1024);
			
			progressMonitor.setProgress(i+1);
		}
		returnCode = Constants.RETURN_CODE_NORMAL;
		return returnCode;
	}
}
