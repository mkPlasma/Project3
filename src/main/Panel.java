package main;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

import javax.swing.JPanel;

/**
 * Panel.java
 * 
 * JPanel that controls update and draw timing.
 * 
 * @author Daniel
 *
 */
public class Panel extends JPanel implements Runnable{
	
	private static final long serialVersionUID = 1L;

	private final BufferStrategy bufferStrategy;
	
	private CollisionTest collisionTest;
	
	
	public Panel(BufferStrategy bufferStrategy){
		this.bufferStrategy = bufferStrategy;
	}
	
	
	public void run(){
		
		// Update rate
		final int UPDATES_PER_SECOND = 120;
		final long UPDATE_TIME_NANO = 1000000000 / UPDATES_PER_SECOND;
		
		// Initialize collision test
		collisionTest = new CollisionTest();
		collisionTest.init();
		
		long startTime = System.nanoTime();
		int lastSecond = 0;
		
		// Update/Draw loop
		while(true){
			long loopStartTime = System.nanoTime();
			
			// Update collision test and draw
			collisionTest.update();
			draw();
			
			
			// Wait for next frame
			try{
				long sleepTime = (UPDATE_TIME_NANO - (System.nanoTime() - loopStartTime)) / 1000000;
				sleepTime = sleepTime < 0 ? 0 : sleepTime;
				
				Thread.sleep(sleepTime);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			// Set frame time
			long frameTime = System.nanoTime() - loopStartTime;
			int currentSecond = (int)((System.nanoTime() - startTime) / 500000000);
			
			if(currentSecond > lastSecond){
				collisionTest.setFrameTime(frameTime);
				lastSecond = currentSecond;
			}
		}
	}
	
	private void draw(){
		
		// Draw using double buffer strategy
		do{
			do{
				Graphics g = bufferStrategy.getDrawGraphics();
				
				// Call draw for collision test
				collisionTest.draw((Graphics2D)g, getSize().width, getSize().height);
				
				g.dispose();

			}
			while(bufferStrategy.contentsRestored());
			
			bufferStrategy.show();
			
		}
		while(bufferStrategy.contentsLost());
	}
}
