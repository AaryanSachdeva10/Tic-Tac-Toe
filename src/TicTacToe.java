import java.awt.EventQueue;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.ButtonGroup;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;
import javax.swing.Timer;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class TicTacToe {
    private JFrame frame;
    private JLabel xscoretext, oscoretext;
    private int count, xscore, oscore;
    private boolean xturn = true;
    private JButton btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, hint;
    private Random rand = new Random();
    private static final Color TURQUOISE = new Color(20, 189, 172); // bg color
    private static final Color GRAY = new Color(84, 84, 84); // x color
    private static final Color LIGHT_GRAY = new Color(242, 235, 211); // o color
    private int delay = 650; // the delay for the computer to make its move to make it more human like
    private JButton cornerBtn;
    private boolean gotHere;
    private char difficulty = 'M'; // difficulty is pre-defined as medium
    private ArrayList<JButton> emptyBtns;
    private JButton[] cornerBtns, edgeBtns;
    private static Map<String, Clip> soundClips = new HashMap<>();
    
    Map<JButton, JButton> cornerOpposites = new HashMap<>();

    private JButton[] buttons; // Declare the array of buttons
    private JButton[][] winCombos;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
            	preloadSounds();
                TicTacToe window = new TicTacToe();
                window.frame.setVisible(true);
                window.frame.setTitle("Tic-Tac-Toe");
                window.frame.setResizable(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * @throws IOException 
     * @wbp.parser.entryPoint
     */
    
    public TicTacToe() throws IOException {
        initialize();
    }

    private void initialize() throws IOException {
    	FlatDarkLaf.setup(); // setup FlatLaf library dark mode
    	
    	// calculations to make the window summon in the center of the screen by chatGPT
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (screenSize.width - 505) / 2;
        int centerY = (screenSize.height - 585) / 2;
        
        Image icon = Toolkit.getDefaultToolkit().getImage("res/tic-tac-toe-icon.png");

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(centerX, centerY, 505, 585);
        frame.getContentPane().setBackground(TURQUOISE);
        frame.setIconImage(icon);
        frame.getContentPane().setLayout(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu optionMenu = new JMenu("Options");
        JMenuItem resetItem = new JMenuItem("Reset");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        JMenu difficultyMenu = new JMenu("Difficulty");
        JRadioButtonMenuItem easyItem = new JRadioButtonMenuItem("Easy");
        JRadioButtonMenuItem mediumItem = new JRadioButtonMenuItem("Medium");
        JRadioButtonMenuItem impossibleItem = new JRadioButtonMenuItem("Impossible");

        // set each menu item to reset with param of difficulty
        easyItem.addActionListener(e -> reset('E')); // easy
        mediumItem.addActionListener(e -> reset('M')); // medium
        impossibleItem.addActionListener(e -> reset('I')); // impossible
        
        resetItem.addActionListener(e -> reset(difficulty)); // reset to whatever the difficulty was
        exitItem.addActionListener(e -> System.exit(0)); // exit once clicked
        
        // make button group for radio buttons so user cannot select more than 1 at a time
        ButtonGroup difficultyGroup = new ButtonGroup();
        difficultyGroup.add(easyItem);
        difficultyGroup.add(mediumItem);
        difficultyGroup.add(impossibleItem);

        optionMenu.add(resetItem);
        optionMenu.addSeparator(); // add line between reset and exit items
        optionMenu.add(exitItem);
        
        difficultyMenu.add(easyItem);
        difficultyMenu.add(mediumItem);
        difficultyMenu.add(impossibleItem);

        menuBar.add(optionMenu);
        menuBar.add(difficultyMenu);
        frame.setJMenuBar(menuBar);
        
        mediumItem.setSelected(true); // make sure to set whatever difficulty is to be pre-selected!

        String imagePath = "res/tic-tac-toe.png"; // board image
        ImageIcon imageIcon = loadImage(imagePath, 500, 500);
        
        /*if (imageIcon == null) {
            JOptionPane.showMessageDialog(frame, "Image file could not be loaded. Check the path and try again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }*/

        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setBounds(0, 0, 485, 500); // Adjust size and position as needed
        frame.getContentPane().add(imageLabel, 0); // Add with lower Z-order

        // Create buttons with createButton method
        btn1 = createButton(btn1, 10, 10, 150, 150);
        btn2 = createButton(btn2, 170, 10, 150, 150);
        btn3 = createButton(btn3, 330, 10, 150, 150);

        btn4 = createButton(btn4, 10, 170, 150, 150);
        btn5 = createButton(btn5, 170, 170, 150, 150);
        btn6 = createButton(btn6, 330, 170, 150, 150);

        btn7 = createButton(btn7, 10, 330, 150, 150);
        btn8 = createButton(btn8, 170, 330, 150, 150);
        btn9 = createButton(btn9, 330, 330, 150, 150);

        buttons = new JButton[]{btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9}; // Initialize buttons array

        // Put opposite corners in hashmap
        cornerOpposites.put(btn1, btn9);
        cornerOpposites.put(btn9, btn1);
        cornerOpposites.put(btn3, btn7);
        cornerOpposites.put(btn7, btn3);
        
        // Set each button to have an action listener when clicked of btnClick(button)
        for (JButton btn : buttons)   btn.addActionListener(e -> btnClick(btn));

        // Create the hint button with the resized icon
        hint = new JButton("Hint");
        hint.addActionListener(e -> btnClick(getBestMove('I'))); // Get the best move by passing impossible mode
        hint.setFont(new Font("Segoe UI Light", Font.PLAIN, 30));
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        hint.setBounds(170, 500, 150, 44);
        hint.setBackground(GRAY);
        hint.setForeground(Color.WHITE);
        hint.setFocusable(false);
        frame.getContentPane().add(hint);
        
        xscoretext = new JLabel("X: " + xscore);
        xscoretext.setHorizontalAlignment(SwingConstants.CENTER);
        xscoretext.setFont(new Font("Segoe UI Light", Font.PLAIN, 40));
        xscoretext.setBounds(0, 495, 160, 44);
        xscoretext.setForeground(Color.BLACK);
        frame.getContentPane().add(xscoretext);

        oscoretext = new JLabel("O: " + oscore);
        oscoretext.setHorizontalAlignment(SwingConstants.CENTER);
        oscoretext.setFont(new Font("Segoe UI Light", Font.PLAIN, 40));
        oscoretext.setBounds(330, 495, 160, 44);
        oscoretext.setForeground(Color.BLACK);
        frame.getContentPane().add(oscoretext);
        
        frame.setVisible(true);
        
        winCombos = new JButton[][]{ // 2D array to store all win combinations
            {btn1, btn2, btn3},
            {btn4, btn5, btn6},
            {btn7, btn8, btn9},
            {btn1, btn4, btn7},
            {btn2, btn5, btn8},
            {btn3, btn6, btn9},
            {btn1, btn5, btn9},
            {btn3, btn5, btn7}
        };

        edgeBtns = new JButton[]{btn2, btn4, btn6, btn8}; // array to hold edge buttons
        cornerBtns = new JButton[]{btn1, btn3, btn7, btn9}; // array to hold corner buttons
        cornerBtn = cornerBtns[rand.nextInt(cornerBtns.length)]; // generate a random corner for the computer to play at the start of the game
    }
    
    private ImageIcon loadImage(String path, int width, int height) { // load image method generated by chatGPT
        try {
            File file = new File(path);
            if (!file.exists()) {
                throw new IOException("File not found");
            }
            BufferedImage originalImage = ImageIO.read(file);
            BufferedImage resizedImage = resizeImage(originalImage, width, height);
            return new ImageIcon(resizedImage);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) { // resize image generated by chatGPT
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resizedImage;
    }

    // createButton method is more efficient and saves many lines
    private JButton createButton(JButton button, int x, int y, int width, int height) {
        Font font = new Font("Tahoma", Font.PLAIN, 99);
        button = new JButton("");
        button.setBounds(x, y, width, height);
        button.setFocusable(false); // no highlight border when selected with cursor
        button.setFont(font);
        button.setBackground(null); // no background color
        button.setContentAreaFilled(false); // no highlight when cursor hovers over button
        button.setBorder(null); // no border
        frame.getContentPane().add(button);
        
        return button;
    }

    public void btnClick(JButton btn) { // button click method
        if (btn.getText().isEmpty()) { // only do this if the button's text is blank which means the box is not taken yet
            btn.setText((xturn) ? "X" : "O"); // if its x's turn set text to X and vice versa
            btn.setForeground((xturn) ? GRAY : LIGHT_GRAY);
            playSound((xturn) ? "res/xsfx.wav" : "res/osfx.wav"); // if its x's turn play x sound effect and vice versa
            xturn = !xturn; // set x turn to opposite of what it was
            count++; // increment count
            checkifwon(); // check if won
        }
    }

    private void checkifwon() { // check if won method
        for (JButton[] combo : winCombos) { // for each combination in all win combinations
            String first = combo[0].getText(); // first button's text
            String second = combo[1].getText(); // second button's text
            String third = combo[2].getText(); // third button's text

            if (!first.isEmpty() && first.equals(second) && second.equals(third)) { // check if all buttons text's are the same and it's not empty
                won(first + " has won!", "Victory!", first.charAt(0), combo[0], combo[1], combo[2]); // if all buttons text's are same and not empty then win has occured
            }
        }

        if (count == 9) { // if all boxes have been filled (9 moves have occured in total)
        	playSound("res/applause.wav"); // play tie sound effect
            won("It's a tie! Nobody wins!", "Draw", 'N', null, null, null); // won method with parameters for draw
        } else if (!xturn) { // if at this point it's not the user's turn then
        	hint.setEnabled(false); // set hint button to disabled
        	doComputerMove(); // let the computer do its move
        }
    }

    private JButton getBestMove(char difficulty) {
    	emptyBtns = new ArrayList<>();
    	
    	for(JButton btn : buttons) if(btn.getText().isEmpty()) emptyBtns.add(btn);
    	
    	switch(difficulty) {
    	case 'E':
    		return emptyBtns.get(rand.nextInt(emptyBtns.size())); // easy difficulty is random so theoretically it can be as hard as impossible
    	
    	case 'M': // medium difficulty is just finding immediate moves
            for (JButton btn : emptyBtns) {
                btn.setText("O");
                if (isWinningMove("O")) {
                    btn.setText("");
                    return btn;
                } else {
                    btn.setText("");
                }
            }

            // Check for blocking move against X
            for (JButton btn : emptyBtns) {
                btn.setText("X");
                if (isWinningMove("X")) {
                    btn.setText("");
                    return btn;
                } else {
                    btn.setText("");
                }
            }
            break;
    		
    	case 'I': // impossible difficulty
    		if(!xturn) { // Computer's turn
            	// Check for winning move for O
	            for (JButton btn : emptyBtns) {
	                btn.setText("O");
	                if (isWinningMove("O")) {
	                    btn.setText("");
	                    return btn;
	                } else {
	                    btn.setText("");
	                }
	            }
	
	            // Check for blocking move against X
	            for (JButton btn : emptyBtns) {
	                btn.setText("X");
	                if (isWinningMove("X")) {
	                    btn.setText("");
	                    return btn;
	                } else {
	                    btn.setText("");
	                }
	            }
    		}
    		else { // User's turn
	            // Check for winning move for X
	            for (JButton btn : emptyBtns) {
	                btn.setText("X");
	                if (isWinningMove("X")) {
	                    btn.setText("");
	                    return btn;
	                } else {
	                    btn.setText("");
	                }
	            }
	            
            	// Check for winning move for O
	            for (JButton btn : emptyBtns) {
	                btn.setText("O");
	                if (isWinningMove("O")) {
	                    btn.setText("");
	                    return btn;
	                } else {
	                    btn.setText("");
	                }
	            }
    		}

    		// If it's the first turn and the AI's turn then place it in a random corner 
    		// I put randomness to make it more fun as it would be boring if the computer kept placing its marker on the same corner
            if(count == 0) { 
            	cornerBtn = cornerBtns[rand.nextInt(cornerBtns.length)];
            	return cornerBtn;
            }
            
            /* If it's the computer's turn on an even count, that implies that the computer started
            the game. This means that at this else if statement the board has an 'O' (computer's marker)
            in a random corner and the user has placed their marker somewhere
            In tic-tac-toe, the best starting move is a corner as the opponent will only have 1
            move to not lose by force which is the center.
            */
            else if(count == 2){
            	gotHere = true; // Broadcast that the game has gotten to this point
            	
            	/* If the center is not empty that means the opponent has selected the center
            	which is the best move. The best move for the computer now is to place its marker 
            	on the opposite corner of the corner that it placed randomly on the first turn*/
            	if(!btn5.getText().isEmpty()){
            		return cornerOpposites.get(cornerBtn);
            	}
            	
            	else if((cornerBtn != btn1) && (!btn7.getText().isEmpty() || !btn3.getText().isEmpty()) && (!btn1.getText().isEmpty() || !btn2.getText().isEmpty() || !btn4.getText().isEmpty())) {
            		return btn9;
            	}
            	else if((cornerBtn != btn9) && (!btn7.getText().isEmpty() || !btn3.getText().isEmpty()) && (!btn6.getText().isEmpty() || !btn8.getText().isEmpty() || !btn9.getText().isEmpty())) {
            		return btn1;
            	}
            	else if((cornerBtn != btn3) && (!btn1.getText().isEmpty() || !btn9.getText().isEmpty()) && (!btn2.getText().isEmpty() || !btn3.getText().isEmpty() || !btn6.getText().isEmpty())) {
            		return btn7;
            	}
            	else if((cornerBtn != btn7) && (!btn1.getText().isEmpty() || !btn9.getText().isEmpty()) && (!btn4.getText().isEmpty() || !btn7.getText().isEmpty() || !btn8.getText().isEmpty())) {
            		return btn3;
            	}
            }
            
            else if((count == 4) && gotHere) {
            	/* exit the if statement as now if this else if statement is reached as it will now
				   go immediately to the enhanced for loop to check for any available corner which
				   at this current state there is only 1 corner left which is the winning corner
            	 */
            }
            
            // If the center is empty then place the marker
            else if (btn5.getText().isEmpty()) {
                return btn5;
            }
            
            /* If it's x's turn and the game state is 
            | |X    X| |
            |O|  or  |O|   then any edge box is safe, the corners will lose by the opponent going in the other corner and have 2 ways to win
           X| |      | |X
            */
            else if ((count == 3) && ((btn1.getText().equals("X") && btn5.getText().equals("O") && btn9.getText().equals("X")) || (btn3.getText().equals("X") && btn5.getText().equals("O") && btn7.getText().equals("X")))) {
                return edgeBtns[rand.nextInt(edgeBtns.length)];
            }
            
            /* If it's x's turn and the game state is 
            | |O    O| |
            |X|  or  |X|   then any edge box is safe, the corners will lose by the opponent going in the other corner and have 2 ways to win
           O| |      | |O
            */
            else if((count == 3) && ((btn1.getText().equals("O") && btn5.getText().equals("X") && btn9.getText().equals("O")) || (btn3.getText().equals("O") && btn5.getText().equals("X") && btn7.getText().equals("O")))) {
            	return edgeBtns[rand.nextInt(edgeBtns.length)];
            }
            
            // return the first available corner button in the "cornerBtns" array
            for (JButton btn : cornerBtns) if (btn.getText().isEmpty()) return btn;
    	}
    	/* If no statement matches then return a random button in the available buttons array
    	   "emptyBtns". This is here as the method requires a return statement if somehow
    	   no statment matches and this is also here for the medium difficulty that if there is no
    	   immediate win or block then the program just returns a random selection*/
		return emptyBtns.get(rand.nextInt(emptyBtns.size()));
    }
    
    private void doComputerMove() {
        setCursorLoading(true); // make the cursor a circular blue loading bar
        // set swing timer to wait a certain amount of time "delay"
        Timer timer = new Timer(delay, e -> {
            btnClick(getBestMove(difficulty));
            setCursorLoading(false);
            hint.setEnabled(true);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private boolean isWinningMove(String player) { // check if a move will immediately win
        for (JButton[] combo : winCombos) { 
            String first = combo[0].getText();
            String second = combo[1].getText();
            String third = combo[2].getText();

            if (first.equals(player) && second.equals(player) && third.equals(player)) {
                return true;
            }
        }
        return false;
    }

    private void won(String message, String title, char player, JButton firstBtn, JButton secondBtn, JButton thirdBtn) {
    	hint.setEnabled(false);
    	switch (player) {
            case 'X':
                xscore++; // increment x's score
                xscoretext.setText("X: " + xscore); // update x's score label
                xturn = !xturn; // toggle "xturn" variable
                playSound("res/win.wav"); // play win sound effect
                break;
            case 'O':
                oscore++; // increment o's score
                oscoretext.setText("O: " + oscore); // update o's score label
                xturn = !xturn; // toggle "xturn" variable
                playSound("res/win.wav"); // play win sound effect
                break;
        }

        for (JButton btn : buttons) { // set every button to disabled
            btn.setEnabled(false);
        }

        if (player != 'N') { // if somebody won then set the winning boxes to enabled
            firstBtn.setEnabled(true);
            secondBtn.setEnabled(true);
            thirdBtn.setEnabled(true);
        }

        int response = JOptionPane.showConfirmDialog(null, message + " Reset?", title, JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) { // reset message box
            reset(difficulty);
        }
    }

    private void setCursorLoading(boolean isLoading) {
        Cursor cursor = isLoading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor();
        frame.setCursor(cursor);
    }

    private void reset(char difficulty) { // reset method
        count = 0; // reset counter to 0
        this.difficulty = difficulty;
        playSound("res/reset.wav");
        for (JButton btn : buttons) {
            btn.setText("");
            btn.setEnabled(true);
        }
        hint.setEnabled(true);
        gotHere = false;
        
        if(!xturn) doComputerMove();
    }
    
    private static void preloadSounds() { // generated by chatGPT
        loadSound("res/xsfx.wav");
        loadSound("res/osfx.wav");
        loadSound("res/win.wav");
        loadSound("res/applause.wav");
        loadSound("res/reset.wav");
    }

    private static void loadSound(String soundFilePath) { // load sound method generated by chatGPT
        try {
            File soundFile = new File(soundFilePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            soundClips.put(soundFilePath, clip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playSound(String soundFilePath) { // generated by chatGPT
        Clip clip = soundClips.get(soundFilePath);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();  // Stop the clip if it's already playing
            }
            clip.setFramePosition(0);  // Rewind to the beginning
            clip.start();
        } else {
            System.err.println("Sound file not found: " + soundFilePath);
        }
    }
}