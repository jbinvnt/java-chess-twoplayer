import java.util.*;

import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.scene.paint.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.animation.*;

public class MainBoard extends Application{
	
	public static final int BOARD_DIM = 720;
	public static final int NUM_SQUARE_SIDE = 8;
	public static final int LABEL_PADDING = 40;
	public static final int CAPTURE_PADDING = 200;
	public static final int FONT_SIZE = 90;
	public static final String FONT_NAME = "Cambria";
	public static final String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h"};
	public static final Color BLACK_COLOR = Color.rgb(24,48,24);
	public static final Color WHITE_COLOR = Color.rgb(240, 240, 240);
	
	private Scene scene;
	
	private static Group root;
	private static Group squaresGroup;
	private static Group piecesGroup;
	private static Group labelGroup;
	
	private static HashMap<String, Piece> whitePieces = new HashMap<String, Piece>();
	private static HashMap<String, Piece> blackPieces = new HashMap<String, Piece>();
	private static HashMap<String, Square> squares = new HashMap<String, Square>();
	private static FlowPane capturedWhitePieces = new FlowPane();
	private static FlowPane capturedBlackPieces = new FlowPane();
	private static boolean whiteTurn;
	private static Text winText;
	private static Font mainFont;
	private static boolean gameWon;
	
	public void start(Stage stage){
		//Text initialization
		winText = new Text();
		mainFont = new Font(FONT_NAME, FONT_SIZE);
		winText.setFont(mainFont);
		//board initialization
		whiteTurn = true;
		gameWon = false;
		root = new Group();
		squaresGroup = new Group();
		piecesGroup = new Group();
		labelGroup = new Group();
		root.getChildren().add(squaresGroup);
		root.getChildren().add(piecesGroup);
		root.getChildren().add(labelGroup);
		scene = new Scene(root, BOARD_DIM+LABEL_PADDING+CAPTURE_PADDING, BOARD_DIM+LABEL_PADDING, Color.rgb(255, 255, 255));
		createPattern(BLACK_COLOR, WHITE_COLOR);
		//capture area for white pieces
		capturedWhitePieces.setMaxWidth(CAPTURE_PADDING);
		capturedWhitePieces.setMaxHeight(BOARD_DIM/2);
		capturedWhitePieces.setTranslateX(BOARD_DIM+LABEL_PADDING);
		capturedWhitePieces.setTranslateY(0);
		root.getChildren().add(capturedWhitePieces);
		//capture area for black pieces
		capturedBlackPieces.setMaxWidth(CAPTURE_PADDING);
		capturedBlackPieces.setMaxHeight(BOARD_DIM/2);
		capturedBlackPieces.setTranslateX(BOARD_DIM+LABEL_PADDING);
		capturedBlackPieces.setTranslateY(BOARD_DIM/2);
		root.getChildren().add(capturedBlackPieces);
		//Stage Initialization
		stage.setTitle("Chess");
		stage.setScene(scene);
		stage.show();
	}
	public static void main(String[] args){
		launch(args);
	}
	public void createPattern(Color color1, Color color2){
		int counter = 0;
		int squareDim = BOARD_DIM/NUM_SQUARE_SIDE;
		for(int r = 0; r < NUM_SQUARE_SIDE; r++){
			for(int c = 0; c < NUM_SQUARE_SIDE; c++){
				counter++;
				Color color;
				if(counter%2 == 0){
					color = color1;
				}
				else{
					color = color2;
				}
				String position = letters[c] + Integer.toString((NUM_SQUARE_SIDE-r));
				if(c == 0){
					Text numberLabel = new Text();
					numberLabel.setText(Integer.toString(NUM_SQUARE_SIDE-r));
					numberLabel.setX(LABEL_PADDING/2);
					numberLabel.setY(squareDim*(r+0.5));
					labelGroup.getChildren().add(numberLabel);
				}
				if(r == NUM_SQUARE_SIDE-1){
					Text letterLabel = new Text();
					letterLabel.setText(letters[c]);
					letterLabel.setX(squareDim*(c+0.5)+LABEL_PADDING);
					letterLabel.setY(BOARD_DIM+LABEL_PADDING/2);
					labelGroup.getChildren().add(letterLabel);
				}
				Square sq = new Square(LABEL_PADDING + squareDim*c, squareDim*r, squareDim, position, color);
				squares.put(position, sq);
			}
			counter++;
		}
		squaresGroup.getChildren().addAll(squares.values());
		setPieces();
	}
	
	public void setPieces(){
		//start piece placement
			//white pieces
		spawnPiece("rook", "a1", true, true);
		spawnPiece("rook", "h1", false, true);
		spawnPiece("knight", "b1", true, true);
		spawnPiece("knight", "g1", false, true);
		spawnPiece("bishop", "c1", true, true);
		spawnPiece("bishop", "f1", false, true);
		spawnPiece("king", "d1", true);
		spawnPiece("queen", "e1", true);
			//black pieces
		spawnPiece("rook", "a8", true, false);
		spawnPiece("rook", "h8", false, false);
		spawnPiece("knight", "b8", true, false);
		spawnPiece("knight", "g8", false, false);
		spawnPiece("bishop", "c8", true, false);
		spawnPiece("bishop", "f8", false, false);
		spawnPiece("king", "d8", false);
		spawnPiece("queen", "e8", false);
		//pawns
		spawnPawns(2, true);
		spawnPawns(7, false);
		//end piece placement
	}
	public void spawnPiece(String name, String startPlace, boolean white){
		Piece pieceToAdd = new Piece(name, white);
		if(white){
			whitePieces.put(name, pieceToAdd);
		}
		else{
			blackPieces.put(name, pieceToAdd);
		}
		squares.get(startPlace).addPiece(pieceToAdd, false);
	}
	public void spawnPiece(String name, String startPlace, boolean king, boolean white){
		Piece pieceToAdd = new Piece(name, white);
		String placement;
		if(king)
			placement = "king";
		else
			placement = "queen";
		if(white){
			whitePieces.put(placement + name, pieceToAdd);
		}
		else{
			blackPieces.put(placement + name, pieceToAdd);
		}
		squares.get(startPlace).addPiece(pieceToAdd, false);
	}
	public void spawnPawns(int row, boolean white){ //pawns are named pawn1, pawn2... starting at an index of 1
		if(white){
			for(int x = 0; x < NUM_SQUARE_SIDE; x++){
				Piece pawn = new Piece("pawn", true);
				whitePieces.put("pawn" + Integer.toString(x+1), pawn);
				squares.get(letters[x] + Integer.toString(row)).addPiece(whitePieces.get("pawn" + Integer.toString(x+1)), false);
			}
		}
		else{
			for(int x = 0; x < NUM_SQUARE_SIDE; x++){
				Piece pawn = new Piece("pawn", false);
				blackPieces.put("pawn" + Integer.toString(x+1), pawn);
				squares.get(letters[x] + Integer.toString(row)).addPiece(blackPieces.get("pawn" + Integer.toString(x+1)), false);
			}
		}
	}
	public static HashMap<String, Square> getSquares(){
		return squares;
	}
	public static FlowPane getWhiteCapture(){
		return capturedWhitePieces;
	}
	public static FlowPane getBlackCapture(){
		return capturedBlackPieces;
	}
	public static Group getRoot(){
		return root;
	}
	public static Group getPiecesGroup(){
		return piecesGroup;
	}
	public static boolean isWhiteTurn(){
		return whiteTurn;
	}
	public static void changeTurn(){
		whiteTurn = !whiteTurn;
		if(gameWon == true){
			return; //don't flip the board if the game has already been won
		}
		flipY(root, true);
		for(Node piece : piecesGroup.getChildren()){
			flipY(piece, false);
		}
		for(Node piece : capturedWhitePieces.getChildren()){
			flipY(piece, false);
		}
		for(Node piece : capturedBlackPieces.getChildren()){
			flipY(piece, false);
		}
		for(Node label : labelGroup.getChildren()){
			flipY(label, false);
		}
	}
	public static void winGame(boolean whiteWon){
		gameWon = true;
		if(whiteWon){
			winText.setText("White won!");
		}
		else{
			winText.setText("Black won!");
		}
		winText.setScaleY(root.getScaleY());
		HBox box = new HBox();
		box.setLayoutX(BOARD_DIM/2-getTextWidth(winText.getText())/2+LABEL_PADDING);
		box.setLayoutY(BOARD_DIM/2-FONT_SIZE);
		box.getChildren().add(winText);
		box.setStyle("-fx-background-color: rgba(255, 255, 255, 0.75)"); //adds a translucent background around the win text
		root.getChildren().add(box);
	}
	public static void flipY(Node node, boolean animate){
		if(animate){
			final ScaleTransition scaleAnim = new ScaleTransition(Duration.millis(500));
			scaleAnim.setToY(node.getScaleY()*-1);
			scaleAnim.setNode(node);
			scaleAnim.play();
		}
		else{
			node.setScaleY(node.getScaleY()*-1);
		}
	}
	public static boolean gameWon(){
		return gameWon;
	}
	public static double getTextWidth(String message) {
		Text txt = new Text(message);
		txt.setFont(mainFont);
		return txt.getLayoutBounds().getWidth();
	}
}