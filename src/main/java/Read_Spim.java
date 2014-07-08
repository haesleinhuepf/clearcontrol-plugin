/*
 * Plugin to load SPIM data directly as hyperstack 
 * 
 * it expects the following directory structure:
 * 
 * <root>
 * 	   metadata.txt
 *     <data>
 *     		data.bin
 *     		index.txt 
 *     
 * where <root> has to be chosen by the user 
 * 
 * see the run() method of the plugin to see what ii is doing in which order
 * 
 * 
 * todo:
 * 
 * - open file as virtual stack (currently the data is opened as a Hyperstack, thus fetching all data into memory)
 * 
 * 
 * 
 * 
 * 2013 MW
 * mweigert@mpi-cbg.de 
 */

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.io.*;
import java.util.Arrays;

import ij.plugin.FileInfoVirtualStack;

import spimdirchooser.SpimDirChooser;

public class Read_Spim implements PlugIn {

	public final static int N_CHANNEL = 1;
	public final static int COLOR_DEPTH = 16;
	private static String dirRootName = ".";

	private static SpimInfo spimInfo;
	private int openMode;
	private int timeT1, timeT2;
	private static boolean virtual;
		

	public void getRootDir() {
		// Open a directory chooser dialog and gets the root path and time intervals to be imported
		
		System.out.println(SpimInfo.getWorkingDir("."));
		
		SpimDirChooser chooser = new SpimDirChooser("choose spim folder",
				SpimInfo.getParentDir(dirRootName));

		openMode = chooser.showRun();
		timeT1 = chooser.getT1();
		timeT2 = chooser.getT2();
		dirRootName = chooser.getSelectedDir();
		
		virtual = chooser.get_CB_virtual_value();
	}

	static public ImagePlus loadSpimFile(String fName, int[] stackDim,
			final long skip) {
		// load the actual data file (of type unsigned 16 bit)


		FileInfo fi = new FileInfo();
		
		
		fi.width = stackDim[0];
		fi.height = stackDim[1];
		fi.fileFormat = FileInfo.RAW;
		fi.fileName = fName;
		fi.intelByteOrder = true;
		
		fi.fileType = FileInfo.GRAY16_UNSIGNED;
		fi.nImages = stackDim[2] * stackDim[3];
		fi.longOffset = skip * stackDim[0] * stackDim[1] * stackDim[2] * 2;
		
		
		ImagePlus img;
		if(virtual){
			FileInfoVirtualStack vs = new FileInfoVirtualStack(fi,false);
			img = new ImagePlus("virtual stack test", vs);	
		}
		else
		{
			FileOpener fo = new FileOpener(fi);
			img = fo.open(false);
		}

		return img;
	}

	@Override
	public void run(String arg) {

		// fetch the root directory and the time interval to open
		getRootDir();

		if (openMode == SpimDirChooser.SELECT_CANCEL)
			return;

		// set up the object holding the info about the spim data folder,
		// dimensions etc...

		spimInfo = new SpimInfo();

		try {
			spimInfo.loadDir(dirRootName);
		} catch (Exception e) {
			System.err.println("could parse/load metadata");
			IJ.log(e.getMessage());
			return;
		}

		// crop the stackDim if time intervals are selected 
		// stackDim = [Nx, Ny, Nz, Nt]
		
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

			// load the image plus 
			ImagePlus img = loadSpimFile(spimInfo.dataFileName, stackDim, skip);

			// create a hyperStack from the image and set the properties
			img.setDimensions(N_CHANNEL, stackDim[2], stackDim[3]);

			// calibration
			Calibration cal = img.getCalibration();
			cal.pixelWidth = spimInfo.pixelSize[0];
			cal.pixelHeight = spimInfo.pixelSize[1];
			cal.pixelDepth = spimInfo.pixelSize[2];
			cal.setUnit("um");

			// show everything
			img.setOpenAsHyperStack(true);
			img.setTitle(dirRootName);
			img.show();

		} catch (Exception e) {
			IJ.log(String.format("could not open %s !", spimInfo.dataFileName));
			return;
		}
	}


	
	// debugging method, run from eclipse etc....
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

