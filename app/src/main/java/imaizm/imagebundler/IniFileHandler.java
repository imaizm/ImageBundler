package imaizm.imagebundler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * アプリケーションの設定をINIファイルとして読み書きするクラスです。
 * <p>
 * 主に、前回使用された作業ディレクトリのパスをINIファイルに保存し、
 * 次回起動時にそのパスを読み込むために使用されます。
 * INIファイルは、アプリケーションの実行ディレクトリに {@code ImageBundler.ini} という名前で作成されます。
 * </p>
 */
public class IniFileHandler {
	
	/** INIファイルのパス。 */
	private Path iniFilePath;
	/** 前回使用された作業ディレクトリのパス。INIファイルから読み込まれます。 */
	private Path workDirectoryPathOfLastTime;
	
	/**
	 * INIファイルから読み込まれた前回使用時の作業ディレクトリのパスを取得します。
	 *
	 * @return 前回使用時の作業ディレクトリのパス。INIファイルが存在しない、または読み込めない場合はnullになる可能性があります。
	 */
	public Path getWorkDirectoryPathOfLastTime() {
		return this.workDirectoryPathOfLastTime;
	}
	
	/**
	 * {@code IniFileHandler} オブジェクトを構築し、INIファイルの読み込みまたは新規作成を行います。
	 * <p>
	 * 現在の作業ディレクトリ（通常はアプリケーションの実行ディレクトリ）に、
	 * {@link Constants#APPLICATION_NAME} に ".ini" を付加した名前のINIファイルが存在するか確認します。
	 * <ul>
	 *   <li>INIファイルが存在する場合：
	 *     ファイルから最初の行を読み込み、その行が示すパスを {@link #getCurrentDirectoryPath(String)} メソッドで検証し、
	 *     結果を {@link #workDirectoryPathOfLastTime} フィールドに設定します。
	 *   </li>
	 *   <li>INIファイルが存在しない場合：
	 *     新しいINIファイルを指定されたパスで作成します。この時点ではファイルは空です。
	 *   </li>
	 * </ul>
	 * </p>
	 *
	 * @throws IOException INIファイルの読み込みまたは作成中にI/Oエラーが発生した場合。
	 */
	public IniFileHandler() throws IOException {
		//
		// ファイル選択ダイアログの起点を定める
		//
		
		// ユーザディレクトリ直下からiniファイルを探す
		String currentDirectoryPath = System.getProperty("user.dir");
		this.iniFilePath = Paths.get(currentDirectoryPath, Constants.APPLICATION_NAME + ".ini");
		
		// iniファイルが存在した場合
		if (Files.exists(this.iniFilePath)) {
			try (BufferedReader bufferedReader = Files.newBufferedReader(this.iniFilePath)) {
				String line = bufferedReader.readLine();
				if (line != null) {
					this.workDirectoryPathOfLastTime = this.getCurrentDirectoryPath(line);
				}
			} catch (IOException e) {
				throw e;
			}
		
		// ImageConverterForPda.iniファイルが存在しない場合
		} else {
			// ユーザディレクトリにImageConverterForPda.iniファイルを作成する
			Files.createFile(this.iniFilePath);
		}
		
	}
	
	/**
	 * 指定されたパス文字列を検証し、そのパスまたはその親ディレクトリのいずれかが実際に存在する場合に、
	 * 存在する最も深い階層の {@link Path} オブジェクトを返します。
	 * <p>
	 * まず、指定された {@code pathString} を {@link Path} オブジェクトに変換します。
	 * 変換されたパスが存在する場合、そのパスをそのまま返します。
	 * 存在しない場合、パスの親ディレクトリを順に遡っていき、最初に見つかった存在するディレクトリのパスを返します。
	 * ルートディレクトリまで遡っても有効なパスが見つからない場合（通常はありえませんが）、null を返す可能性があります。
	 * </p>
	 *
	 * @param pathString 検証するディレクトリのパスを表す文字列。
	 * @return 指定されたパスまたはその親ディレクトリのうち、実際に存在する最も深い階層の {@link Path} オブジェクト。
	 *         有効なパスが見つからない場合は null またはルートに近い存在するパス。
	 */
	private Path getCurrentDirectoryPath(String pathString) {

		Path pathForReturn = Paths.get(pathString);
		if (Files.notExists(pathForReturn)) {
			while (
				pathForReturn != null &&
				Files.notExists(pathForReturn)) {
				pathForReturn = pathForReturn.getParent();
			}				
		}
		
		return pathForReturn;
	}
	
	/**
	 * 指定されたファイルパスをINIファイルに書き込みます。
	 * <p>
	 * このメソッドは、{@link #iniFilePath} で示されるINIファイルに、
	 * 指定された {@code filePath} 文字列を上書きで書き込みます。
	 * これにより、次回アプリケーション起動時にこのパスが読み込まれるようになります。
	 * </p>
	 *
	 * @param filePath INIファイルに書き込む作業ディレクトリのパス文字列。
	 * @throws IOException INIファイルへの書き込み中にI/Oエラーが発生した場合。
	 */
	public void writeWorkDirectoryOfLastTime(String filePath) throws IOException {
		
		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.iniFilePath)) {
			bufferedWriter.write(filePath);
		} catch (IOException e) {
			throw e;
		}
		
	}
}
