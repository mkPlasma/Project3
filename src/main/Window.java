package main;

import java.awt.Dimension;

import javax.swing.JFrame;

/**
 * Window.java
 * 
 * JFrame window that contains the drawing Panel and event listeners.
 * 
 * @author Daniel
 *
 */
public class Window extends JFrame{
	
	private static final long serialVersionUID = 1L;

	public Window(){
		super("Project 3");
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		// Disable repaint since it will be handled by the Panel class
		setIgnoreRepaint(true);
		
		// Set size
		getContentPane().setPreferredSize(new Dimension(800, 600));
		pack();
		
		// Center window
		setLocationRelativeTo(null);
		
		// Add input listener
		InputListener il = new InputListener();
		addKeyListener(il);
		addMouseListener(il);
		addMouseMotionListener(il);
		addMouseWheelListener(il);
		
		// Create double-buffer strategy (prevents flickering while drawing)
		createBufferStrategy(2);
		
		// Create and add main panel
		Panel panel = new Panel(getBufferStrategy());
		
		add(panel);
		setVisible(true);
		
		new Thread(panel).start();
	}
}
