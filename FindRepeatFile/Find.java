import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Find {
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		if (args.length != 1){
			System.out.println("I need directory path.");
			return;
		}
		List<String> repeatFiles = getRepeatFiles(findFile(args[0]));
		for (String path : repeatFiles) {
			System.out.println(path);
		}
	}

	private static List<String> findFile(String path) {
		File dir = new File(path);
		List<String> files = new ArrayList<String>();
		if (!(dir.exists() && dir.isDirectory())) return files;

		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				files.addAll(findFile(f.getPath()));
			} else {
				files.add(f.getPath());
			}
		}
		return files;
	}

	private static List<String> getRepeatFiles(List<String> oriFiles) throws IOException, NoSuchAlgorithmException {
		String fileHash;
		List<String> repeatFiles = new ArrayList<String>();
		Map<String, String> map = new HashMap<String, String>();
		for (String path : oriFiles) {
			fileHash = getFileHash(path);
			if (map.get(fileHash) == null) {
				map.put(fileHash, path);
			} else {
				repeatFiles.add(path);
			}
		}
		return repeatFiles;
	}

	private static String getFileHash(String path) throws NoSuchAlgorithmException, IOException {
		int bufSize = 4 * 1024 * 1024;
		MessageDigest digest = MessageDigest.getInstance("MD5");
		FileInputStream in = new FileInputStream(new File(path));
		DigestInputStream digestInputStream = new DigestInputStream(in, digest);

		byte[] buffer = new byte[bufSize];
		while (digestInputStream.read(buffer) > 0) ;
		return byteArrayToHex(digestInputStream.getMessageDigest().digest());
	}

	private static String byteArrayToHex(byte[] bytes) {
		char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
		char[] resultHex = new char[bytes.length * 2];

		int i = 0;
		for (byte b : bytes) {
			resultHex[i++] = hexDigits[b >>> 4 & 0xF];
			resultHex[i++] = hexDigits[b & 0xF];
		}
		return new String(resultHex);
	}
}

