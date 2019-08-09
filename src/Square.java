import javafx.scene.shape.*;
import javafx.event.EventHandler;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import javafx.util.Duration;
import javafx.animation.*;
import javafx.event.ActionEvent;


public class Square extends Rectangle{
	
	private ImageView viewer;
	private Piece currentPiece = null;
	private String position;
	private Color originalColor;
	
	public static final long ANIM_TIME = 1000;
	
	private static final Color SELECT_COLOR = Color.rgb(255, 255, 0);
	private static final Color ERROR_COLOR = Color.rgb(255, 0, 0);
	private static final int BORDER_WIDTH = 20;
	
	private static Square originalSpot = null;
	
	public Square(double x, double y, double dim, String newPosition, Color fill){
		this.setX(x);
		this.setY(y);
		this.setWidth(dim);
		this.setHeight(dim);
		this.setFill(fill);
		originalColor = fill;
		this.position = newPosition;
		final Square currentSquare = this; //allows the eventhandler to recognize the current square
		this.setOnMousePressed(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				if(MainBoard.gameWon()){
					return;
				}
				if(originalSpot == null){ //if this is the first spot getting selected
					originalSpot = currentSquare;
					scanSquares(true);
					currentSquare.selectSquare(true);
				}
				else{
					scanSquares(false);
					originalSpot.movePiece(currentSquare);
					originalSpot.selectSquare(false);
					originalSpot = null;
				}
			}
		});
	}
	public void addPiece(Piece newPiece, boolean flipBoard){
		currentPiece = newPiece;
		TranslateTransition anim = setImageView(newPiece);
		if(flipBoard){
			anim.setOnFinished(new EventHandler<ActionEvent>(){
				@Override
				public void handle(ActionEvent e){
					MainBoard.changeTurn();
				}
			});
		}
		anim.play();
	}
	public void removePiece(){
		if(this.isOccupied()){
			viewer = null;
			currentPiece = null;
		}
	}
	public void capturePiece(){
		if(this.isOccupied()){
			if(getCurrentPiece().getType() == "king"){
				MainBoard.winGame(!getCurrentPiece().isWhite());
			}
			MainBoard.getPiecesGroup().getChildren().remove(getCurrentPiece().getImageView());
			ImageView capturedPiece = new ImageView(getCurrentPiece().getImage());
			if(getCurrentPiece().isWhite()){
				MainBoard.flipY(capturedPiece, false); //flips the white piece to keep it upright
				MainBoard.getWhiteCapture().getChildren().add(capturedPiece); //moves current piece to the capture area
			}
			else{
				MainBoard.getBlackCapture().getChildren().add(capturedPiece);
			}
		}
		this.removePiece();
	}
	public Piece getCurrentPiece(){
		return currentPiece;
	}
	public boolean isOccupied(){
		return currentPiece != null;
	}
	public TranslateTransition setImageView(Piece piece){
		viewer = piece.getImageView();
		TranslateTransition anim = new TranslateTransition(Duration.millis(ANIM_TIME), viewer);
		anim.setToX(this.getX()+(this.getWidth()/2)-(Piece.PIECE_DIM/2));
		anim.setToY(this.getY()+(this.getHeight()/2)-(Piece.PIECE_DIM/2));
		return anim;
	}
	public String getPosition(){
		return position;
	}
	public void selectSquare(boolean select){
		if(select){
			this.setStroke(SELECT_COLOR);
			this.setStrokeType(StrokeType.INSIDE);
			this.setStrokeWidth(BORDER_WIDTH);
		}
		else{
			this.setStroke(this.originalColor);
		}
	}
	public void setError(boolean select){
		if(select){
			this.setStroke(ERROR_COLOR);
			this.setStrokeType(StrokeType.INSIDE);
			this.setStrokeWidth(BORDER_WIDTH);
		}
		else{
			this.setStroke(this.originalColor);
		}
	}
	public void movePiece(Square target){
		if(checkMove(target)){ //only move the piece if the move is legal
			target.capturePiece(); //captures the piece if it's already there
			this.getCurrentPiece().setFirstMove();
			target.addPiece(this.getCurrentPiece(), true);
			this.removePiece();
		}
	}
	public void scanSquares(boolean select){
		for(Square sq : MainBoard.getSquares().values()){
			if(select)
				sq.selectSquare(checkMove(sq));
			else
				sq.selectSquare(false);
		}
	}
	public boolean checkMove(Square target){
		String newPosition = target.getPosition();
		if(this.isOccupied() && newPosition != this.position && this.currentPiece.isWhite() == MainBoard.isWhiteTurn()){
			if(target.isOccupied()){
				if(target.getCurrentPiece().isWhite() == this.getCurrentPiece().isWhite()){
					return false; //reject move if the target is one of the player's own pieces
				}
			}
		}
		else{
			return false; //don't do anything if the square doesn't have a piece on it or if it isn't the player's turn or if the target is the current square or if the target is the current player's piece
		}
		char firstChar = this.position.charAt(0);
		char secondChar = newPosition.charAt(0);
		int firstInt = Integer.parseInt(this.position.substring(1,2));
		int secondInt = Integer.parseInt(newPosition.substring(1,2));
		String pieceType = currentPiece.getType();
		if(pieceType != "knight"){
			return checkPattern(pieceType, firstChar, secondChar, firstInt, secondInt) && checkObstacles(firstChar, secondChar, firstInt, secondInt); //checks whether the piece is allowed to make the move
		}
		else{
			return checkPattern(pieceType, firstChar, secondChar, firstInt, secondInt);
		}
	}
	public boolean checkObstacles(char firstChar, char secondChar, int firstInt, int secondInt){ //draws a line and uses that to check whether the path of a move is blocked
		Square original = MainBoard.getSquares().get(firstChar+Integer.toString(firstInt));
		Square target = MainBoard.getSquares().get(secondChar+Integer.toString(secondInt));
		Line path = new Line(original.getCenterX(), original.getCenterY(), target.getCenterX(), target.getCenterY());
		for(Square obst : MainBoard.getSquares().values()){
			if(path.contains(obst.getCenterX(), obst.getCenterY())){
				if(obst != original && obst != target){
					if(obst.isOccupied()){
						return false; //reject move if the path is blocked by one of the player's own pieces
					}
				}
			}
		}
		return true;
	}
	public boolean checkPattern(String pieceType, char firstChar, char secondChar, int firstInt, int secondInt){
		Piece pieceToMove = MainBoard.getSquares().get(firstChar+Integer.toString(firstInt)).getCurrentPiece();
		Square target = MainBoard.getSquares().get(secondChar+Integer.toString(secondInt));
		boolean verdict = true;
		switch(pieceType){
		case "bishop":
			verdict = Math.abs(firstChar - secondChar) == Math.abs(firstInt - secondInt);
			break;
		case "rook":
			verdict = firstChar == secondChar || firstInt == secondInt;
			break;
		case "queen":
			verdict = checkPattern("bishop", firstChar, secondChar, firstInt, secondInt) || checkPattern("rook", firstChar, secondChar, firstInt, secondInt);
			break;
		case "knight":
			verdict = (Math.abs(firstChar - secondChar) == 2 && Math.abs(firstInt - secondInt) == 1) || (Math.abs(firstChar - secondChar) == 1 && Math.abs(firstInt - secondInt) == 2);
			break;
		case "king":
			verdict = (Math.abs(firstChar - secondChar) <= 1 && Math.abs(firstInt - secondInt) <= 1);
			break;
		case "pawn":
			int pawnDirection;
			if(pieceToMove.isWhite()){
				pawnDirection = 1;
			}
			else{
				pawnDirection = -1;
			}
			if((secondInt - firstInt) == pawnDirection && Math.abs(secondChar - firstChar) == 1){ //if pawn is moving forward and one square to the left or right (capturing)
				verdict = target.isOccupied() && target.getCurrentPiece().isWhite() != pieceToMove.isWhite(); //the target has an enemy piece
			}
			else{
				verdict = (secondInt - firstInt) == pawnDirection && Math.abs(secondChar - firstChar) == 0 && !target.isOccupied(); //pawn can only move forward if there isn't a piece there
			}
			if(!pieceToMove.hasMoved()){
				verdict = verdict || ((secondInt - firstInt) == pawnDirection*2 && Math.abs(secondChar - firstChar) == 0 && !target.isOccupied()); //if it is on its first move, pawn can move forward two squares
			}
		}
		return verdict;
	}
	public double getCenterX(){
		double centerWidth = (MainBoard.BOARD_DIM/MainBoard.NUM_SQUARE_SIDE)/2; //the half-width of a square on the board
		return getX() + centerWidth;
	}
	public double getCenterY(){
		double centerWidth = (MainBoard.BOARD_DIM/MainBoard.NUM_SQUARE_SIDE)/2; //the half-width of a square on the board
		return getY() + centerWidth;
	}
}