package imaizm.imagebundler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

public class ZipFileHandler {
	public static File[] inflate(File targetZipFile, File outputDirectory) throws IOException {
		List<File> outputFileList = new ArrayList<File>();
		
		ZipFile zipFile = new ZipFile(targetZipFile, "MS932");
		Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntries();
		while (zipEntries.hasMoreElements()) {
			
			ZipArchiveEntry zipEntry = zipEntries.nextElement();
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
		zipFile.close();
		
		return outputFileList.toArray(new File[0]);
	}

}
