package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * InputListener.java
 * 
 * Listens for user keyboard and mouse input, storing information that can be accessed through static class methods.
 * 
 * @author Daniel
 *
 */
public class InputListener implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener{
	
	// Keys are 1, 2, 3, +, -
	private static boolean[] keys;
	
	private static int mx, my;
	
	private static boolean mouseDown;
	
	private static int mouseScroll;
	
	public InputListener(){
		keys = new boolean[7];
	}
	
	public static boolean isKeyTyped(int i){
		boolean k = keys[i];
		keys[i] = false;
		return k;
	}
	
	public static int getMouseX(){
		return mx;
	}
	
	public static int getMouseY(){
		return my - 25;
	}
	
	public static boolean isMouseDown(){
		return mouseDown;
	}
	
	public static int getMouseScroll(){
		int ms = mouseScroll;
		mouseScroll = 0;
		return ms;
	}
	
	
	public void keyPressed(KeyEvent e){
		
	}

	public void keyReleased(KeyEvent e){
		
	}

	public void keyTyped(KeyEvent e){
		switch(e.getKeyChar()){
		
		case '1':
			keys[0] = true;
			return;
		
		case '2':
			keys[1] = true;
			return;
			
		case '3':
			keys[2] = true;
			return;
			
		case '+': case '=':
			keys[3] = true;
			return;
			
		case '-': case '_':
			keys[4] = true;
			return;
			
		case 'd': case 'D':
			keys[5] = true;
			return;
			
		case 'r': case 'R':
			keys[6] = true;
			return;
		}
	}
	
	public void mouseDragged(MouseEvent e){
		mx = e.getX();
		my = e.getY();
	}
	
	public void mouseMoved(MouseEvent e){
		mx = e.getX();
		my = e.getY();
	}
	
	public void mousePressed(MouseEvent e){
		mx = e.getX();
		my = e.getY();
		mouseDown = true;
	}
	
	public void mouseReleased(MouseEvent e){
		mx = e.getX();
		my = e.getY();
		mouseDown = false;
	}
	
	public void mouseClicked(MouseEvent e){
		
	}
	
	public void mouseEntered(MouseEvent e){
		
	}
	
	public void mouseExited(MouseEvent e){
		
	}

	public void mouseWheelMoved(MouseWheelEvent e){
		mouseScroll += e.getWheelRotation();
	}
}
