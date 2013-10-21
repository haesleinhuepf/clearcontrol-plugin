/*
 * Plugin to load SPIM data directly as hyperstack via an index file
 * it expects the following directory structure:
 * 
 * <root>
 *     <data>
 *     		data.bin
 *     		index.txt 
 *     
 * where <root> has to be chosen by the user 
 * MW 2013
 */

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.io.*;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

import spimdirchooser.SpimDirChooser;

public class Read_Spim implements PlugIn {

	public final static String INDEX_NAME = "index.txt";
	public final static String DATA_NAME = "data.bin";
	public final static String DATA_DIR_NAME = "data";
	public final static int N_CHANNEL = 1;
	public final static int COLOR_DEPTH = 16;

	private static String dirRootName = null;
	private int openMode;
	private int timeT1, timeT2;

	static public String joinPath(final String path1, final String path2) {

		File foo = new File(path1, path2);
		return foo.getAbsolutePath();
	}

	static public String getParentDir(final String path) {

		if (path==null)
			return null;
		else{
			File foo = new File(path);
			return foo.getAbsoluteFile().getParentFile().getAbsolutePath();
		}
	}

	public void getRootDir() {
		// Open a directory and get the path and time intervals

		
		SpimDirChooser chooser = new SpimDirChooser("choose spim folder",getParentDir(dirRootName));
		openMode = chooser.showRun();
		timeT1 = chooser.getT1();
		timeT2 = chooser.getT2();
		dirRootName = chooser.getSelectedDir();
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

	static public ImagePlus loadSpimFile(String fName, int[] stackDim,
			final long skip) {
		// load the data file

		FileInfo fi = new FileInfo();

		fi.width = stackDim[0];
		fi.height = stackDim[1];
		fi.fileFormat = FileInfo.RAW;
		fi.fileName = fName;
		fi.intelByteOrder = true;
		fi.longOffset = skip * fi.width * fi.height * 2;
		fi.fileType = FileInfo.GRAY16_UNSIGNED;
		fi.nImages = stackDim[2] * stackDim[3];

		ImagePlus img = new FileOpener(fi).open(false);

		return img;
	}

	@Override
	public void run(String arg) {

	
		// fetch the root directory and the time intervall to open
		getRootDir();

		if (openMode == SpimDirChooser.SELECT_CANCEL)
			return;

		else {
			// set up the paths...
			final String dataDirName = joinPath(dirRootName, DATA_DIR_NAME);
			final String indexFileName = joinPath(dataDirName, INDEX_NAME);
			final String dataFileName = joinPath(dataDirName, DATA_NAME);
			final String titleName = getParentDir(dataDirName);

			int[] stackDim = new int[4];

			// read the index.txt file
			try {
				stackDim = parseIndexFile(indexFileName);
			} catch (FileNotFoundException e) {
				IJ.log(String.format("could not open %s !", indexFileName));
				return;
			}

			long skip = 0;
			if (openMode == SpimDirChooser.SELECT_OPEN_INTERVAL) {
				skip = timeT1-1;
				int dimT = timeT2 - timeT1 + 1;
				stackDim[3] = Math.min(Math.max(1, dimT), stackDim[3]);
			}

			try {

				System.out.printf("loading file of dimension %s\n",
						Arrays.toString(stackDim));

				ImagePlus img = loadSpimFile(dataFileName, stackDim, skip);

				// create a hyperstack from the image and set the properties
				img.setDimensions(N_CHANNEL, stackDim[2], stackDim[3]);
				img.setOpenAsHyperStack(true);
				img.setTitle(titleName);
				img.show();

			} catch (Exception e) {
				IJ.log(String.format("could not open %s !", dataFileName));

				return;
			}

		}

	}

	// debugging method
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the menu
		Class<?> clazz = Read_Spim.class;
		String url = clazz.getResource(
				"/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring(5, url.length()
				- clazz.getName().length() - 6);
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		ImageJ app = new ImageJ();

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");

		// app.quit();
	}
}
