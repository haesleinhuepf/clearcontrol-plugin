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
			
		int[] stackDim = new int[]{40,30,20,10};
		ImagePlus img = Read_Spim.loadSpimFile(fName, stackDim,1);
		
		System.out.println(img.getImageStackSize());
		
	
	}

	
	@Test
	public void testSomething() {
		
		SpimDirChooser chooser = new SpimDirChooser("choose spim folder");
		
		int mode = chooser.showRun();
		System.out.println(mode);
		
	
	}
	
}

