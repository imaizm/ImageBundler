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

public class ZipFileHandler {
	
	public static List<Path> inflate(
		Path targetZipFilePath,
		Path outputDirectoryPath)
		throws IOException {
		
		List<Path> outputFilePathList = new ArrayList<Path>();

		try (ZipFile zipFile = new ZipFile(targetZipFilePath.toFile(), "MS932")) {
			Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntries();
			while (zipEntries.hasMoreElements()) {
				
				ZipArchiveEntry zipEntry = zipEntries.nextElement();
				if (! zipEntry.isDirectory()) {
					if (zipEntry.getName().toLowerCase().endsWith("jpg") ||
						zipEntry.getName().toLowerCase().endsWith("jpeg")) {
						
						String outputFileName =
							zipEntry.getName().substring(
								zipEntry.getName().lastIndexOf("/") + 1);
						
						Path outputFilePath = outputDirectoryPath.resolve(outputFileName);
						outputFilePathList.add(outputFilePath);
						try (OutputStream outputStream = Files.newOutputStream(outputFilePath)) {
							try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
								byte[] buffer = new byte[1024];
								int readSize;
								while ((readSize = inputStream.read(buffer)) != -1) {
									outputStream.write(buffer, 0, readSize);
								}
							} catch (IOException e) {
								throw e;
							}
						} catch (IOException e) {
							throw e;
						}
					}
				}
			}
		}
		
		return outputFilePathList;
	}

}
