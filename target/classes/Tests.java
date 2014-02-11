import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.io.DirectoryChooser;

import org.junit.Test;

import spimdirchooser.SpimDirChooser;



public class Tests {

	@Test
	public void testFile() {
		
		String fName = "TestData/data/data.bin";
			
		int[] stackDim = new int[]{320,320,51,3};
		ImagePlus img = Read_Spim.loadSpimFile(fName, stackDim,1);
		
		System.out.println(img);
		System.out.println(img.getImageStackSize());
		
	
	}

	
	@Test
	public void test(){
		
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

	
		app.quit();
	}


	@Test
	public void testSomething() {
		
		SpimDirChooser chooser = new SpimDirChooser("choose spim folder");
		
		int mode = chooser.showRun();
		System.out.println(mode);
	
//		int[] stackDim = new int[]{320,320,51,3};
//		ImagePlus img = Read_Spim.loadSpimFile(fName, stackDim,1);
////		
//		System.out.println(img);
//		System.out.println(img.getImageStackSize());
//		
	
	}
	
}

