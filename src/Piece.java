import java.io.*;

import javafx.scene.image.*;

public class Piece {
	public static final int PIECE_DIM = 64;
	private String type;
	private Image img;
	private boolean isWhite;
	private boolean hasMoved;
	private ImageView viewer;
	
	public Piece(String newType, boolean white){
		hasMoved = false;
		isWhite = white;
		type = newType;
		File imageFile;
		if(white){
			imageFile = new File("w" + newType + ".png");
		}
		else{
			imageFile = new File("b" + newType + ".png");
		}
		img = new Image(imageFile.toURI().toString(), PIECE_DIM, PIECE_DIM, false, false);
		viewer = new ImageView(img);
		viewer.setMouseTransparent(true); //allows the user to click on squares that are underneath the imageview
		MainBoard.getPiecesGroup().getChildren().add(viewer);
	}
	public Image getImage(){
		return img;
	}
	public boolean isWhite(){
		return isWhite;
	}
	public String getType(){
		return type;
	}
	public ImageView getImageView(){
		return viewer;
	}
	public void setFirstMove(){
		hasMoved = true;
	}
	public boolean hasMoved(){
		return hasMoved;
	}
}
