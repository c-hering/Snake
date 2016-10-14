import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JOptionPane;

public class snakeCanvas extends Canvas implements Runnable, KeyListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int BOX_HEIGHT = 15;
	private final int BOX_WIDTH =15;
	private final int GRID_WIDTH = 30;
	private final int GRID_HEIGHT = 30;
	
	private LinkedList<Point> snake;
	private Point fruit;
	private int direction = Direction.NO_DIRECTION;
	
	private Thread runThread;
	private int score = 0;
	private String highScore = "";
	
	private Image menuImage = null;
	private boolean isInMenu = true;
	
	public void paint(Graphics g){
		if (runThread == null){
			this.setPreferredSize(new Dimension(640, 480));
			this.addKeyListener(this);
			runThread = new Thread(this);
			runThread.start();
		}
		if (isInMenu){
			DrawMenu(g);
		}else{
			if (snake == null){
				snake = new LinkedList<Point>();
				GenerateDefaultSnake();
				PlaceFruit();
				}
			if (highScore.equals("")){
				highScore = this.GetHighScore();
			}
			DrawFruit(g);
			DrawGrid(g);
			DrawSnake(g);
			DrawScore(g);

		}
	}
	
	public void DrawMenu(Graphics g){
		
		if (this.menuImage == null){
			try{
				URL imagePath = snakeCanvas.class.getResource("menu.png");
				menuImage = Toolkit.getDefaultToolkit().getImage(imagePath);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		g.drawImage(menuImage, 0, 0, 640, 480, this);
	}
	
	public void update(Graphics g){
		
		Graphics offScreenGraphics;
		BufferedImage offScreen = null;
		Dimension d = this.getSize();
		
		offScreen = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
		offScreenGraphics = offScreen.getGraphics();
		offScreenGraphics.setColor(this.getBackground());
		offScreenGraphics.fillRect(0, 0, d.width, d.height);
		offScreenGraphics.setColor(this.getForeground());
		paint(offScreenGraphics);
		
		g.drawImage(offScreen, 0, 0, this);
		
	}
	
	public void GenerateDefaultSnake(){
		score = 0;
		snake.clear();
		snake.add(new Point(3,3));
		snake.add(new Point(3,2));
		snake.add(new Point(3,1));
		direction = Direction.NO_DIRECTION;
	}
	
	public void Move(){
		
		Point head = snake.peekFirst();
		Point newPoint = head;
		
		switch (direction){
		case Direction.NORTH:
			newPoint = new Point(head.x, head.y - 1);
			break;
		case Direction.SOUTH:
			newPoint = new Point(head.x, head.y + 1);
			break;
		case Direction.EAST:
			newPoint = new Point(head.x + 1, head.y);
			break;
		case Direction.WEST:
			newPoint = new Point(head.x - 1, head.y);
			break;
		}
		
		snake.remove(snake.peekLast());
		
		if (newPoint.equals(fruit)){
			snake.addFirst(newPoint);
			score += 10;
			PlaceFruit();
		}
		else if (newPoint.x < 0 || newPoint.x > GRID_WIDTH - 1){
			CheckScore();
			GenerateDefaultSnake();
			PlaceFruit();
			return;
		}
		else if (newPoint.y < 0 || newPoint.y > GRID_HEIGHT - 1){
			CheckScore();
			GenerateDefaultSnake();
			PlaceFruit();
			return;
		}
		else if (snake.contains(newPoint)){
			GenerateDefaultSnake();
			return;
		}
		else if (snake.size() == (GRID_WIDTH * GRID_HEIGHT)){
			
		}
		
		
		snake.push(newPoint);
		
	}
	
	public void DrawScore(Graphics g){
		
		g.drawString("Score: " + score, BOX_WIDTH * GRID_WIDTH + 5, 20);
		g.drawString("High Score: " + highScore, BOX_WIDTH * GRID_WIDTH + 5 , 30);
		
	}
	
	public void CheckScore(){
		if(highScore.equals(""))
			return;
		if (score > Integer.parseInt((highScore.split(":")[1]))){
			String name = JOptionPane.showInputDialog("NEW HIGH SCORE, NAME: ");
			highScore = name + ":" + score;
			
			File scoreFile = new File("highscore.dat");
			if (!scoreFile.exists()){
				try {
					scoreFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			FileWriter writeFile = null;
			BufferedWriter writer = null;
			try{
				writeFile = new FileWriter(scoreFile);
				writer = new BufferedWriter(writeFile);
				writer.write(this.highScore);
			}catch(Exception e){
				
			}
			finally{
				if (writer != null){
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void DrawGrid(Graphics g){
		
		g.drawRect(0, 0, GRID_WIDTH * BOX_WIDTH, GRID_HEIGHT * BOX_HEIGHT);
		//for (int x = BOX_WIDTH; x < GRID_WIDTH * BOX_WIDTH; x += BOX_WIDTH){
		//	g.drawLine(x, 0, x, BOX_HEIGHT * GRID_HEIGHT);
		//}
		//for (int y = BOX_HEIGHT; y < GRID_HEIGHT * BOX_HEIGHT; y += BOX_HEIGHT){
		//	g.drawLine(0, y, GRID_WIDTH * BOX_WIDTH, y);	
		//}
		
	}
	
	public void DrawSnake(Graphics g){
		
		g.setColor(Color.BLUE);
		for (Point p : snake){
			g.fillRect(p.x * BOX_WIDTH, p.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
		}
		g.setColor(Color.BLACK);
		
	}
	
	public void DrawFruit(Graphics g){
		
		g.setColor(Color.RED);
		g.fillRect(fruit.x * BOX_WIDTH, fruit.y * BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
		g.setColor(Color.BLACK);
	}
	
	public void PlaceFruit(){
		
		Random rand = new Random();
		int randomX = rand.nextInt(GRID_WIDTH);
		int randomY = rand.nextInt(GRID_HEIGHT);
		Point randomPoint = new Point(randomX, randomY);
		
		while(snake.contains(randomPoint)){
			randomX = rand.nextInt(GRID_WIDTH);
			randomY = rand.nextInt(GRID_HEIGHT);
			randomPoint = new Point(randomX, randomY);
		}
		fruit = randomPoint;
		
	}


	public void run() {
		
		while(true){
			repaint();
			if(!isInMenu)
				Move();
			try{
				Thread.currentThread();
				Thread.sleep(60);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}

	public void keyPressed(KeyEvent e) {
		
		switch (e.getKeyCode()){
		case KeyEvent.VK_UP:
			if (direction != Direction.SOUTH)
				direction = Direction.NORTH;
			break;
		case KeyEvent.VK_DOWN:
			if (direction != Direction.NORTH)
				direction = Direction.SOUTH;
			break;
		case KeyEvent.VK_RIGHT:
			if (direction != Direction.WEST)
				direction = Direction.EAST;
			break;
		case KeyEvent.VK_LEFT:
			if (direction != Direction.EAST)
				direction = Direction.WEST;
			break;
		case KeyEvent.VK_ENTER:
			if (isInMenu)
				isInMenu = false;
			repaint();
			break;
		}
		
	}
	
	private String GetHighScore(){
		
		FileReader readFile = null;
		BufferedReader reader = null;
		
		try{
			readFile = new FileReader("highscore.dat");
			reader = new BufferedReader(readFile);
			return reader.readLine();
		}catch (Exception e){
			return "Nobody:0";		
		}
		finally
		{
			try{
				if (reader != null)
					reader.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
	
	}

	public void keyReleased(KeyEvent e) {

		
		
	}

	public void keyTyped(KeyEvent e) {

		
		
	}
	
}
