package spimdirchooser;

import javax.swing.JFileChooser;

import org.junit.Test;

public class SpimDirChooserTests {

	@Test
	public void test() {
		
		SpimDirChooser dir = new SpimDirChooser("Hallo","/Users/mweigert/Desktop");

		int s = dir.showRun();
		
		
		System.out.println(s);
		
		System.out.printf("mode: %d\t selected Dir: %s \ttime t1: %d \ttime t2: %d",dir.getValue(),dir.getSelectedDir(),dir.getT1(),dir.getT2());
	}
}
