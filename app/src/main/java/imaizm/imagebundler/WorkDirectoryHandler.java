package imaizm.imagebundler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

/**
 * アプリケーションの一時的な作業ディレクトリを作成および管理するクラスです。
 * <p>
 * このクラスの主な目的は、OSの一時ディレクトリ内に、アプリケーション固有の
 * 一時作業ディレクトリを安全に作成することです。作業ディレクトリ名は、
 * 現在の日時とランダムな5桁の数値から生成され、重複のリスクを低減します。
 * </p>
 * <p>
 * 作成された作業ディレクトリのパスは、{@link #getWorkDirectoryPath()} メソッドを通じて取得できます。
 * このクラスの利用者は、アプリケーション終了時などに、取得したパスを使用して
 * 作業ディレクトリを適切にクリーンアップする責任があります。
 * </p>
 */
public class WorkDirectoryHandler {

	/** 
	 * OSの一時ディレクトリのパスを格納します。
	 * このパスは {@code java.io.tmpdir} システムプロパティから取得されます。
	 */
	private String tempDirectoryPath;
	/** 
	 * 作成される一時作業ディレクトリの名前を格納します。
	 * この名前は、現在の日時 (yyyyMMddHHmmss形式) と5桁のランダムな数値から構成されます。
	 */
	private String workDirectoryName;
	/** 
	 * 作成された一時作業ディレクトリへの {@link Path} オブジェクトです。
	 * このパスは、{@code tempDirectoryPath} と {@code workDirectoryName} を結合して生成されます。
	 */
	private Path workDirectoryPath;

	/**
	 * {@code WorkDirectoryHandler} オブジェクトを構築し、新しい一時作業ディレクトリを作成します。
	 * <p>
	 * 処理の流れは以下の通りです。
	 * <ol>
	 *   <li>OSの一時ディレクトリのパスを {@code java.io.tmpdir} システムプロパティから取得し、
	 *       {@link #tempDirectoryPath} に格納します。</li>
	 *   <li>作成する一時作業ディレクトリの名前を生成します。この名前は、
	 *       現在の日時（"yyyyMMddHHmmss"形式）と、0から99999までの5桁のランダムな整数を
	 *       連結したものになります。生成された名前は {@link #workDirectoryName} に格納されます。</li>
	 *   <li>上記で取得したOS一時ディレクトリのパスと生成した作業ディレクトリ名を結合し、
	 *       新しいディレクトリを実際にファイルシステム上に作成します。
	 *       作成されたディレクトリの {@link Path} オブジェクトは {@link #workDirectoryPath} に格納されます。</li>
	 * </ol>
	 * </p>
	 *
	 * @throws IOException ディレクトリの作成中にI/Oエラーが発生した場合。
	 */
	public WorkDirectoryHandler() throws IOException {
		
		// 作業ディレクトリ：実行時OSのTEMPディレクトリを取得
		this.tempDirectoryPath = System.getProperty("java.io.tmpdir");
		// 仮ディレクトリ：日時＋乱数５桁
		this.workDirectoryName = 
			(DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())) +
			(new DecimalFormat("#####")).format((int) (Math.random() * 100000D));
		
		// 仮ディレクトリの作成
		this.workDirectoryPath = Files.createDirectory(Paths.get(tempDirectoryPath, workDirectoryName));
	}
	
	/**
	 * 作成された一時作業ディレクトリの {@link Path} オブジェクトを取得します。
	 * <p>
	 * このメソッドは、コンストラクタで作成された一時作業ディレクトリの完全なパスを返します。
	 * このパスは、アプリケーションが一時ファイル（例：ZIPファイルの解凍先など）を
	 * 保存するために使用できます。
	 * </p>
	 *
	 * @return 作成された一時作業ディレクトリのパス。
	 */
	public Path getWorkDirectoryPath() {
		return this.workDirectoryPath;
	}
}
