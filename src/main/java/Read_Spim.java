/*
 * Plugin to load SPIM data directly as hyperstack via an index file
 * MW 2013
 */

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.io.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Scanner;

public class Read_Spim implements PlugIn {
	
	public final static String INDEX_NAME = "index.txt";
	public final static String DATA_NAME = "data.bin";
	public final static int N_CHANNEL = 1;
	public final static int COLOR_DEPTH = 16;

	public String getDir() {
		// Open a directory and get the path
		DirectoryChooser dirDialog = new DirectoryChooser("Select folder");
		String selectedPath = new String("");
		selectedPath = dirDialog.getDirectory();
		return selectedPath;
	}

	public int[] parseIndexFile(final String dirName, final String fileName)
			throws FileNotFoundException {

		Scanner scanner = new Scanner(new File(dirName, fileName));

		String[] tokens = new String[4];

		int[] newShape = new int[] { 0, 0, 0, 0 };

		// lines read from metadata file
		// returns the files shape as (width,height,slices,frames)

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

	
	public ImagePlus loadSpimFile(String fName, int[] stackDim) {
		// load the data file

		FileInfo fi = new FileInfo();

		fi.width = stackDim[0];
		fi.height = stackDim[1];
		fi.fileFormat = fi.RAW;
		fi.fileName = fName;
		fi.intelByteOrder = true;
		
		fi.fileType = FileInfo.GRAY16_UNSIGNED;
		fi.nImages = stackDim[2] * stackDim[3];

		ImagePlus img = new FileOpener(fi).open(false);
		
		System.out.printf("loading file of dimension %s\n",Arrays.toString(stackDim));
		return img;
	}
	
	@Override
	public void run(String arg) {
		
		// get the directiory
		
		String dirName = getDir();
		//String dirName = "/Users/mweigert/Desktop/SpimPlugin/data/";

		if (dirName == null)
			return;

		// get the dimensions of the stack
		int[] stackDim = new int[] { 0, 0, 0, 0 };

		try {
			stackDim = parseIndexFile(dirName, INDEX_NAME);
		} catch (FileNotFoundException e) {
			System.err.println("couldn't find index file!");
			return;
		}

		try {
			
			
			ImagePlus img = loadSpimFile(dirName+DATA_NAME, stackDim);
		
			// create a hyperstack from the image
			img.setDimensions(N_CHANNEL, stackDim[2], stackDim[3]);
			img.setOpenAsHyperStack(true);
			img.show();

		
		}
		catch(Exception e){
			IJ.log("couldnt open "+DATA_NAME);
			return;
		}
		
				
	}

	// debugging method
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins
		// menu
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
