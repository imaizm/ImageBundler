package imaizm.imagebundler;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

import imaizm.imagebundler.ImageConverter.ContraAspectMode;

public class EntryPoint {

	/**
	 * デフォルトコンストラクタ
	 */
	public EntryPoint() {
	}

	public void convert(File inputFile, int width, int height)
		throws IOException {
		
		InputFileHandler inputFileHandler =
			new InputFileHandler(inputFile);
		
		this.convert(inputFile, inputFileHandler.getInputFiles(), width, height);
		
		// 入力ソースがディレクトリだった場合
		if (inputFile.isDirectory()) {
			
			String outputZipFileName =
				inputFile.getParent() +
				File.separator +
				inputFile.getName() +
				".zip";

			System.out.println("output zip file name : " + outputZipFileName);

			this.store(inputFileHandler.getInputFiles(), outputZipFileName);
		}
		
		inputFileHandler.close();
	}
	
	private void convert(File inputFile, File[] inputFiles, int width, int height) throws IOException {
		
		File workDirectory = (new WorkDirectoryHandler()).getWorkDirectory();
		
		ArrayList<File> outputFileList = new ArrayList<File>();
		
		// 処理中ダイアログ
		ProgressMonitor progressMonitor = new ProgressMonitor(null, "変換中 : " + inputFile.getName(), "ノート", 0, inputFiles.length);
		progressMonitor.setMillisToDecideToPopup(0);
		
		for (int i = 0; i < inputFiles.length; i++) {
			
			progressMonitor.setNote((i+1) + " of " + inputFiles.length);

			BufferedImage bufferedImage = ImageIO.read(inputFiles[i]);
			if (bufferedImage != null) {
				
				List<BufferedImage> convertedImageList = ImageConverter.convert(bufferedImage, width, height);
				
				int index = 0;
				for (BufferedImage convertedImage : convertedImageList) {
					index++;
					
					String outputFileName = 
							workDirectory.getAbsolutePath() +
							File.separator +
							FilenameUtils.getBaseName(
								inputFiles[i].getName()) +
							((convertedImageList.size() == 1) ? "" : "_" + Integer.toString(index)) +
							".jpg";
						File outputFile = writeJpegFile(convertedImage, outputFileName, 75);
						outputFileList.add(outputFile);
				}
				
			}
			
			progressMonitor.setProgress(i+1);
		}

		// 拡張子を除去
		String inputFileName = FilenameUtils.getBaseName(inputFile.getName());
		
		String outputZipFileName =
			inputFile.getParent() +
			File.separator +
			inputFileName +
			"_reduced.zip";

		System.out.println("output zip file name : " + outputZipFileName);

		this.store((File[])outputFileList.toArray(new File[0]), outputZipFileName);
		
		// 一時ファイルを削除
		for (Iterator<File> it=outputFileList.iterator(); it.hasNext();) {
			it.next().delete();
		}
		workDirectory.delete();
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

	private File writeJpegFile(
		BufferedImage inputBufferedImage,
		String outputFileName,
		int compressionQualityPercentage)
		throws IOException {
		
		// 変換元画像が透過情報を持っている場合、透過情報を白色に置き換える
		if (inputBufferedImage.getColorModel().getTransparency() != Transparency.OPAQUE) {
			inputBufferedImage = fillTransparentPixels(inputBufferedImage, Color.WHITE);
		}
		
		File outputFile = new File(outputFileName);
		float compressionQuality = (float) compressionQualityPercentage / 100F;
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
			File currentDirectoryForJFileChooser = iniFileHandler.getWorkDirectoryOfLastTime();
			
			String parentDirectoryOfSelectedFile = null;
			
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			jFileChooser.setMultiSelectionEnabled(true);
			
			if (currentDirectoryForJFileChooser != null) {
				jFileChooser.setCurrentDirectory(currentDirectoryForJFileChooser);
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
			File inputFile = new File(filePath);
			if (!inputFile.exists()) {
				System.out.println("エラー：指定されたファイル/フォルダが存在しません。");
				return returnCode;
			}
			targetFileList.add(inputFile);
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
			
			converter.convert(targetFile, 768, 1024);
			
			progressMonitor.setProgress(i+1);
		}
		returnCode = Constants.RETURN_CODE_NORMAL;
		return returnCode;
	}
}
