package imaizm.imagebundler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 * ZIPファイルの処理に関連するユーティリティメソッドを提供するクラスです。
 * <p>
 * 現在は、指定されたZIPファイルを解凍し、特定の条件（JPEGファイルのみ）に一致するファイルを
 * 指定されたディレクトリに展開する機能を提供します。
 * </p>
 * <p>
 * ZIPファイルのエンコーディングは "MS932" (Shift_JIS) を想定しています。
 * </p>
 */
public class ZipFileHandler {
	
	/**
	 * 指定されたZIPファイルを指定されたディレクトリに解凍（展開）します。
	 * <p>
	 * このメソッドは、ZIPファイル内のエントリを順次処理します。
	 * ディレクトリではないエントリのうち、ファイル名が ".jpg" または ".jpeg" で終わるもの（大文字・小文字を区別しない）のみを対象とします。
	 * 対象となったファイルは、元のファイル名を維持したまま、指定された {@code outputDirectoryPath} に展開されます。
	 * ZIPファイルの読み込み時のエンコーディングは "MS932" (Shift_JIS) を使用します。
	 * </p>
	 * <p>
	 * 注意：ZIPエントリ名にディレクトリ構造が含まれている場合（例: "folder/image.jpg"）、
	 * 出力ファイル名はそのベース名（"image.jpg"）のみが使用され、サブディレクトリは作成されません。
	 * </p>
	 *
	 * @param targetZipFilePath 解凍対象のZIPファイルのパス。
	 * @param outputDirectoryPath 解凍されたファイルを保存するディレクトリのパス。
	 *                            このディレクトリは事前に存在している必要があります。
	 * @return 解凍され、出力ディレクトリに保存されたファイルの {@link Path} のリスト。
	 *         対象となるファイル（JPG/JPEG）が存在しない場合は空のリストが返されます。
	 * @throws IOException ZIPファイルの読み込み、またはファイルの書き出し中にI/Oエラーが発生した場合。
	 */
	public static List<Path> inflate(
		Path targetZipFilePath,
		Path outputDirectoryPath)
		throws IOException {
		
		List<Path> outputFilePathList = new ArrayList<Path>();

		try (ZipFile zipFile = new ZipFile(targetZipFilePath.toFile(), "MS932")) {
			Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntries();
			while (zipEntries.hasMoreElements()) {
				
				ZipArchiveEntry zipEntry = zipEntries.nextElement();
				// ディレクトリはスキップ
				if (! zipEntry.isDirectory()) {
					String entryNameLower = zipEntry.getName().toLowerCase();
					// JPGまたはJPEGファイルのみを対象とする
					if (entryNameLower.endsWith(".jpg") ||
						entryNameLower.endsWith(".jpeg")) {
						
						// ZIPエントリ名からファイル名部分のみを抽出
						// (例: "path/to/file.jpg" -> "file.jpg")
						String outputFileName =
							zipEntry.getName().substring(
								zipEntry.getName().lastIndexOf("/") + 1);
						
						Path outputFilePath = outputDirectoryPath.resolve(outputFileName);
						outputFilePathList.add(outputFilePath);
						
						// ファイルを実際に解凍して書き出す
						try (OutputStream outputStream = Files.newOutputStream(outputFilePath)) {
							try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
								byte[] buffer = new byte[1024];
								int readSize;
								while ((readSize = inputStream.read(buffer)) != -1) {
									outputStream.write(buffer, 0, readSize);
								}
							}
						}
					}
				}
			}
		}
		
		return outputFilePathList;
	}

}
