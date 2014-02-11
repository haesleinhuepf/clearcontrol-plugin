import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

import org.junit.Test;

public class SpimInfo {

	static public String joinPath(final String path1, final String path2) {

		File foo = new File(path1, path2);
		return foo.getPath();
	}

	static public String getParentDir(final String path) {

		if (path == null)
			return null;
		else {
			File foo = new File(path);
			return foo.getAbsoluteFile().getParentFile().getAbsolutePath();
		}
	}

	static public String getWorkingDir(final String path) {

		if (path == null)
			return null;
		else {
			File foo = new File(path);
			return foo.getAbsoluteFile().getAbsolutePath();
		}
	}

	static public int[] parseIndexFile(final String fileName)
			throws FileNotFoundException {
		// the index file should be in the data directory and includes the
		// dimensions
		// of the 4D stacks
		// returns the dimensions as int[]{width,height,slices,frames}

		Scanner scanner = new Scanner(new File(fileName));

		String[] tokens = new String[4];

		int[] newShape = new int[] { 0, 0, 0, 0 };

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			try {
				tokens = line.split(",", 4);
				newShape[0] = Integer.valueOf(tokens[1].trim());
				newShape[1] = Integer.valueOf(tokens[2].trim());
				newShape[2] = Integer.valueOf(tokens[3].trim().split("\\D")[0]);
				newShape[3] += 1;
			} catch (Exception e) {
				// just ignore all line we couldnt parse...
			}

		}

		scanner.close();
		return newShape;
	}

	static public float[] parseMetadataFile(final String fileName)
			throws FileNotFoundException {
		// the metadata file should be in the data directory and includes the
		// pixel Sizes etc...
		final float[] defaultPixelSize = new float[] { .162f, .162f, .5f };

		if (!(new File(fileName)).exists()) {
			System.out.println(fileName
					+ " doesnt exists. Use default resolutions for pixelSize.");
			return defaultPixelSize;
		}

		Scanner scanner = new Scanner(new File(fileName));

		String[] tokens = new String[4];

		float startZ = -1.f, stopZ = -1.f;
		int numberOfPlanes = -1;
		
		try {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains("timelapse.StartZ")) {
					startZ = Float.parseFloat(line.split("\t")[2]);
					System.out.println(startZ);
				}
				if (line.contains("timelapse.StopZ")) {
					stopZ = Float.parseFloat(line.split("\t")[2]);
					System.out.println(stopZ);
				}
				if (line.contains("timelapse.NumberOfPlanes")) {
					numberOfPlanes = (int)Float.parseFloat(line.split("\t")[2]);
					System.out.println(numberOfPlanes);
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return defaultPixelSize;
			
		}
		return new float[] { .162f, .162f, (stopZ-startZ)/(numberOfPlanes-1) };
	}

	int[] stackDim;
	float[] pixelSize;
	public final static String INDEX_NAME = "index.txt";
	public final static String DATA_NAME = "data.bin";
	public final static String DATA_DIR_NAME = "data";
	public final static String META_NAME = "metadata.txt";

	public String dataDirName;
	public String indexFileName;
	public String dataFileName;
	public String metaFileName;

	public SpimInfo() {

		stackDim = new int[4];
		pixelSize = new float[3];
	}

	public void loadDir(final String dirRootName) throws Exception {

		dataDirName = SpimInfo.joinPath(dirRootName, DATA_DIR_NAME);
		metaFileName = SpimInfo.joinPath(dirRootName, META_NAME);
		indexFileName = SpimInfo.joinPath(dataDirName, INDEX_NAME);
		dataFileName = SpimInfo.joinPath(dataDirName, DATA_NAME);

		// check whether they all exists:

		if (!(new File(dataDirName)).exists())
			throw new Exception("Folder doesnt exists: " + dataDirName);

		if (!(new File(indexFileName)).exists())
			throw new Exception("File doesnt exists: " + indexFileName);

		if (!(new File(dataFileName)).exists())
			throw new Exception("File doesnt exists: " + dataFileName);

		// read the index.txt file
		try {
			stackDim = SpimInfo.parseIndexFile(indexFileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new Exception("couldnt parse index File: " + indexFileName);
		}

		pixelSize = parseMetadataFile(metaFileName);

	}

	@Test
	public void test_joinPath() {
		assertEquals(joinPath("You/", "/are.beautiful"), "You/are.beautiful");
		assertEquals(joinPath("You", "/are.beautiful"), "You/are.beautiful");
		assertEquals(joinPath("You/", "are.beautiful"), "You/are.beautiful");
		assertEquals(joinPath("You", "are.beautiful"), "You/are.beautiful");
	}

	@Test
	public void test_getParentDir() {
		assertEquals(getParentDir("."),
				"/Users/mweigert/workspace_IJ/Read_Spim");
		assertEquals(getParentDir("src/main"),
				"/Users/mweigert/workspace_IJ/Read_Spim/src");

	}

	@Test
	public void test_Index() throws Exception {

		int[] dim = new int[4];
		dim = parseIndexFile("index.txt");

		System.out.println("test_index: " + Arrays.toString(dim));
	}

	@Test
	public void test_Meta() throws Exception {

		float[] dim = new float[3];
		dim = parseMetadataFile("metadata.txt");

		System.out.println("test_Meta: " + Arrays.toString(dim));
	}

	@Test
	public void testLoadDir() {

		SpimInfo info = new SpimInfo();
		try {
			info.loadDir("/Users/mweigert/Desktop/Phd/Denoise/opencl/spimMeans/histone");
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		System.out.println(Arrays.toString(info.stackDim));
	}
}
