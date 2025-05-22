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

/**
 * 画像ファイルを指定された幅と高さに変換し、JPEG形式で圧縮してZIPファイルに格納するエントリーポイントクラスです。
 * <p>
 * 主な機能は以下の通りです。
 * <ul>
 *   <li>指定された画像ファイルまたはディレクトリ内の画像ファイルを処理します。</li>
 *   <li>画像をリサイズし、JPEG形式に変換します。</li>
 *   <li>変換後の画像を一時ディレクトリに保存します。</li>
 *   <li>一時ディレクトリ内の画像をZIPファイルにまとめて格納します。</li>
 *   <li>処理の進捗状況をプログレスモニターで表示します。</li>
 * </ul>
 * コマンドライン引数またはファイル選択ダイアログを通じて処理対象を指定できます。
 * </p>
 */
public class EntryPoint {

	/**
	 * {@code EntryPoint} オブジェクトを構築します。
	 * このコンストラクタは、インスタンスの初期化のみを行い、特定の処理は実行しません。
	 */
	public EntryPoint() {
	}

	/**
	 * 指定された入力ファイルまたはディレクトリ内の画像ファイルを処理し、指定された幅と高さに変換します。
	 * <p>
	 * 入力パスがファイルの場合、そのファイルを変換します。
	 * 入力パスがディレクトリの場合、ディレクトリ内のサポートされている画像ファイルを変換し、
	 * 変換後の画像を元のファイル名でZIPファイルに格納します。
	 * </p>
	 *
	 * @param inputFilePath 処理対象のファイルまたはディレクトリのパス。
	 * @param width 変換後の画像の幅（ピクセル単位）。
	 * @param height 変換後の画像の高さ（ピクセル単位）。
	 * @throws IOException ファイルの読み書き中にエラーが発生した場合。
	 */
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
	
	/**
	 * 指定された入力ファイルのリストを、指定された幅と高さに変換し、縮小版の画像をZIPファイルに格納します。
	 * <p>
	 * このメソッドは、主に {@link #convert(Path, int, int)} メソッドから内部的に呼び出されます。
	 * 変換処理の進捗はプログレスモニターで表示されます。
	 * 変換後の画像は一時ディレクトリに保存され、その後ZIPファイルにまとめられます。
	 * 処理完了後、一時ファイルおよび一時ディレクトリは削除されます。
	 * </p>
	 *
	 * @param inputFilePath 元の入力パス（ファイルまたはディレクトリ）。主にZIPファイル名の生成に使用されます。
	 * @param inputFilePathList 処理対象の画像ファイルのパスのリスト。
	 * @param width 変換後の画像の幅（ピクセル単位）。
	 * @param height 変換後の画像の高さ（ピクセル単位）。
	 * @throws IOException ファイルの読み書きまたはZIPファイル作成中にエラーが発生した場合。
	 */
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

			File inputFile = inputFilePathList.get(i).toFile();
			System.out.println("-> " + inputFile.getName());

			BufferedImage bufferedImage = ImageIO.read(inputFile);
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

	/**
	 * 指定された {@link BufferedImage} をJPEGファイルとして書き込みます。
	 * <p>
	 * 画像が透過情報を持つ場合、透過部分を指定された背景色（デフォルトは白）で塗りつぶしてからJPEGに変換します。
	 * JPEGの圧縮品質はパーセンテージで指定します。
	 * </p>
	 *
	 * @param inputBufferedImage 書き込む対象の画像データ。
	 * @param outputFileName 出力するJPEGファイルのパスとファイル名。
	 * @param compressionQualityPercentage JPEGの圧縮品質（0から100の範囲、100が最高品質）。
	 * @return 書き込まれたJPEGファイルの {@link Path} オブジェクト。
	 * @throws IOException ファイル書き込み中にエラーが発生した場合。
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
		float compressionQuality = compressionQualityPercentage / 100F;

		for (
			Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName("jpg");
			imageWriters.hasNext();
		) {
			ImageOutputStream imageOutputStream =
				ImageIO.createImageOutputStream(outputFilePath.toFile());
			ImageWriter imageWriter = imageWriters.next();
			imageWriter.setOutput(imageOutputStream);
			ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
			imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); // MODE_EXPLICIT = 2
			imageWriteParam.setCompressionQuality(compressionQuality);

			imageWriter.write(null, new IIOImage(inputBufferedImage, null, null), imageWriteParam);
			imageOutputStream.flush();
			imageOutputStream.close();
			imageWriter.dispose();
		}

		return outputFilePath;
	}

	/**
	 * 指定された {@link BufferedImage} の透過ピクセルを指定された色で塗りつぶします。
	 * <p>
	 * 新しい {@code BufferedImage} を {@code BufferedImage.TYPE_INT_RGB} 形式で作成し、
	 * 指定された {@code fillColor} で背景を塗りつぶした後、元の画像をその上に描画します。
	 * これにより、元の画像の透過部分は {@code fillColor} で置き換えられます。
	 * </p>
	 *
	 * @param inputBufferdImage 透過ピクセルを塗りつぶす対象の画像。
	 * @param fillColor 透過ピクセルを塗りつぶすために使用する色。
	 * @return 透過ピクセルが {@code fillColor} で塗りつぶされた新しい {@link BufferedImage} インスタンス。
	 */
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
	
	/**
	 * 指定されたファイルリストをZIPファイルに格納します。
	 * <p>
	 * 各ファイルはZIPファイル内のエントリとして、元のファイル名で格納されます。
	 * ZIP圧縮メソッドはSTORED（無圧縮）を使用し、エンコーディングはMS932（Shift_JIS）です。
	 * 各エントリには、最終更新日時、サイズ、CRC-32チェックサムが設定されます。
	 * </p>
	 *
	 * @param targetFilePathList ZIPファイルに格納するファイルのパスのリスト。
	 * @param outputFileName 出力するZIPファイルのパスとファイル名。
	 * @return 作成されたZIPファイルの {@link Path} オブジェクト。
	 * @throws IOException ファイルの読み書きまたはZIPファイル作成中にエラーが発生した場合。
	 */
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
	
	/**
	 * アプリケーションのメインエントリーポイントです。
	 * <p>
	 * コマンドライン引数を受け取り、{@link #execute(String[])} メソッドを呼び出して処理を実行します。
	 * 処理結果に応じて、適切なリターンコードでアプリケーションを終了します ({@link System#exit(int)})。
	 * 例外が発生した場合はスタックトレースを出力し、エラーリターンコードで終了します。
	 * </p>
	 *
	 * @param args コマンドライン引数。最初の引数として処理対象のファイルまたはディレクトリのパスを指定できます。
	 */
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

	/**
	 * コマンドライン引数に基づいて画像変換処理を実行します。
	 * <p>
	 * 引数で処理対象のファイルまたはディレクトリが指定されている場合、それを処理します。
	 * 引数がない場合は、ファイル選択ダイアログを表示し、ユーザーに処理対象を選択させます。
	 * 選択された各ファイルまたはディレクトリに対して、{@link #convert(Path, int, int)} メソッドを呼び出し、
	 * 画像を指定されたサイズ（幅768ピクセル、高さ1024ピクセル）に変換します。
	 * 処理の全体的な進捗はプログレスモニターで表示されます。
	 * </p>
	 * <p>
	 * ファイル選択ダイアログを使用した場合、最後に選択されたファイルの親ディレクトリが記憶され、
	 * 次回ダイアログを開く際の初期ディレクトリとして使用されます。
	 * </p>
	 *
	 * @param args コマンドライン引数。最初の引数として処理対象のファイルまたはディレクトリのパスを指定できます。
	 *             引数が指定されていない場合は、ファイル選択ダイアログが表示されます。
	 * @return 処理が正常に完了した場合は {@link Constants#RETURN_CODE_NORMAL} (0)、
	 *         エラーが発生した場合は {@link Constants#RETURN_CODE_ERROR} (1)。
	 * @throws IOException ファイルの読み書きまたは画像処理中にエラーが発生した場合。
	 */
	public static int execute(String args[]) throws IOException {
		// デフォルトリターンコード＝１
		int returnCode = Constants.RETURN_CODE_ERROR;
		
		//
		// 対象ファイルの取得
		//
		List<File> targetFileList = new LinkedList<File>();
		
		// 引数にて対象ファイルの指定がなかった場合
		if (args.length == 0) {
			//
			// ファイル/ディレクトリ選択ダイアログを表示
			//

			// iniファイルが有ればそこから前回の作業ディレクトリを取得
			IniFileHandler iniFileHandler = new IniFileHandler();
			Path currentDirectoryPathForJFileChooser = iniFileHandler.getWorkDirectoryPathOfLastTime();
			
			String parentDirectoryOfSelectedFile = null;

			// JFileChooserを初期化
			// ・ディレクトリも選択可に設定
			// ・複数選択可に設定
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			jFileChooser.setMultiSelectionEnabled(true);
			// ・前回の作業ディレクトリを初期ディレクトリに設定（あれば）
			if (currentDirectoryPathForJFileChooser != null) {
				jFileChooser.setCurrentDirectory(currentDirectoryPathForJFileChooser.toFile());
			}

			// JFileChooser起動
			int state = jFileChooser.showOpenDialog(null);
			if (state == JFileChooser.APPROVE_OPTION) {
				// 選択あれば

				// 選択されたファイル/ディレクトリをtargetFileListに格納
				for(File targetFile : jFileChooser.getSelectedFiles()) {
					targetFileList.add(targetFile);
					parentDirectoryOfSelectedFile = targetFile.getParent();
				}
			} else {
				// 選択がなかった場合→そのまま終了
				return returnCode;
			}

			// 今回の作業ディレクトリを「前回の作業ディレクトリ」として記録
			iniFileHandler.writeWorkDirectoryOfLastTime(parentDirectoryOfSelectedFile);
			
		// 引数にて対象ファイルの指定があった場合
		} else if (args.length == 1) {
			// 指定されたファイル/ディレクトリパスの存在チェック
			String filePath = args[0];
			Path inputFilePath = Paths.get(filePath);
			if (Files.notExists(inputFilePath)) {
				// 存在しなかった場合→エラー終了
				System.out.println("エラー：指定されたファイル/フォルダが存在しません。");
				return returnCode;
			}
			// 存在した場合、targetFileListに格納
			targetFileList.add(inputFilePath.toFile());
		// 引数の指定に誤りがある場合
		} else {
			System.out.println("エラー：引数が指定されていません。");
			return returnCode;
		}

		// 処理中ダイアログの準備
		ProgressMonitor progressMonitor = new ProgressMonitor(null, "全体進捗", "ノート", 0, targetFileList.size());
		progressMonitor.setMillisToDecideToPopup(0);

		EntryPoint converter = new EntryPoint();

		// targetFileListに含まれるファイル/ディレクトリを一つずつ処理
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
