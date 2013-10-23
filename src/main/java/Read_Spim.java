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
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.io.*;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

import spimdirchooser.SpimDirChooser;

public class Read_Spim implements PlugIn {

	public final static int N_CHANNEL = 1;
	public final static int COLOR_DEPTH = 16;
	private static String dirRootName = null;

	private static SpimInfo spimInfo;
	private int openMode;
	private int timeT1, timeT2;

	public void getRootDir() {
		// Open a directory and get the path and time intervals
//		SpimDirChooser chooser = new SpimDirChooser("choose spim folder",
//				SpimInfo.getParentDir(dirRootName));
		SpimDirChooser chooser = new SpimDirChooser("choose spim folder",
				null);

		openMode = chooser.showRun();
		timeT1 = chooser.getT1();
		timeT2 = chooser.getT2();
		dirRootName = chooser.getSelectedDir();
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
			// set up the object holding the info about the spim data folder,
			// dimensions etc...

			spimInfo = new SpimInfo();

			try {
				spimInfo.loadDir(dirRootName);
			} catch (Exception e) {
				System.err.println("wooooo");
				IJ.log(e.getMessage());
				return;
			}

			int[] stackDim = spimInfo.stackDim.clone();
			long skip = 0;
			if (openMode == SpimDirChooser.SELECT_OPEN_INTERVAL) {
				skip = timeT1 - 1;
				int dimT = timeT2 - timeT1 + 1;
				stackDim[3] = Math.min(Math.max(1, dimT), stackDim[3]);
			}

			System.out.printf("loading file of dimension %s\n",
					Arrays.toString(stackDim));

			try {

				ImagePlus img = loadSpimFile(spimInfo.dataFileName, stackDim, skip);

				// create a hyperstack from the image and set the properties
				img.setDimensions(N_CHANNEL, stackDim[2], stackDim[3]);
				
				// calibration 
				Calibration cal = new Calibration(img);
				cal.pixelWidth = spimInfo.pixelSize[0];
				cal.pixelHeight = spimInfo.pixelSize[1];
				cal.pixelDepth = spimInfo.pixelSize[2];
				cal.setUnit("um");

				
				
				img.setOpenAsHyperStack(true);
				img.setTitle(dirRootName);
				img.show();

			} catch (Exception e) {
				IJ.log(String.format("could not open %s !", spimInfo.dataFileName));
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
