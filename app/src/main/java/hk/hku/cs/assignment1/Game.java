package hk.hku.cs.assignment1;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kinyipchan on 2/11/15.
 */
public class Game extends Thread {

    private Handler mHandler;
    private Message msg;

    //
    public Boolean gameOver = false;
    public Boolean showHints = false;
    public int turn = 0;
    public String message = "";

    //-----------
    // 0 : none
    // 1 : black
    // 2 : white
    //-----------
    public final int NULL = 0;
    public final int BLACK = 1;
    public final int WHITE = 2;

    //
    private int row = 0;
    private int col = 0;
    private int boardSize = 0;
    private Map<Integer, Map<Integer, Integer>> boardData;

    public Game(Handler mHandler){
        this.mHandler = mHandler;
    }

    public void run() {

        while(boardSize == 0){
            try {
                message = ("Please select board size");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        initBoardData();
        initBoard();

        Integer[] userCodes = {BLACK, WHITE};
        //init position
        int middlePosition = boardSize/2 - 1;
        setBoardData(middlePosition, middlePosition, BLACK);
        setBoardData(middlePosition, middlePosition + 1, WHITE);
        setBoardData(middlePosition + 1, middlePosition, WHITE);
        setBoardData(middlePosition + 1, middlePosition + 1, BLACK);

        while (!gameOver) {

            for (Integer userCode : userCodes) {

                turn = userCode;

                if (userCode == BLACK) {
                    message = ("Black turn");
                } else if (userCode == WHITE) {
                    message = ("White turn");
                }

                //refresh screen
                render();

                if(possibleMoveCount(userCode) > 0) {
                    while(true) {

                        resetMove();
                        while (row < 0 || col < 0) {
                            //waiting for the action
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (getBoardData(row,col) == NULL && isPossibleMove(userCode, row, col)) {
                            setMove(userCode, row, col);
                            break;
                        } else {
                            Log.d("test", "Wrong move");
                        }
                    }
                }else{
                    message = ("No possible move");
                }
            }

            //game check
            if (possibleMoveCount(BLACK) == 0 && possibleMoveCount(WHITE) == 0) {
                gameOver = true;
                break;
            }
        }

        //game over
        message = ("Game Over");

        //refresh screen
        render();
    }

    public void initBoardData(){
        if(boardSize == 0){
            boardSize = 8;
        }

        boardData = new HashMap<Integer, Map<Integer, Integer>>();
        for(int r=0; r<boardSize; r++){
            Map<Integer, Integer> colData = new HashMap<Integer, Integer>();
            for(int c=0; c<boardSize; c++){
                colData.put(c, NULL);
            }
            boardData.put(r, colData);
        }
    }

    public void initBoard(){
        msg = new Message();
        msg.what = 0;
        mHandler.sendMessage(msg);
    }

    public void render(){
        //refresh screen
        msg = new Message();
        msg.what = 1;
        mHandler.sendMessage(msg);
    }

    public void setBoardSize(int size){
        this.boardSize = size;
    }

    public int getBoardSize(){
        return this.boardSize;
    }

    public int getBoardData(int row, int col){
        return this.boardData.get(row).get(col);
    }

    public void setBoardData(int row, int col, int value){
        Map<Integer, Integer> colData = boardData.get(row);
        colData.put(col, value);
        boardData.put(row, colData);
    }

    public void setMove(int row, int col){
        this.row = row;
        this.col = col;
    }

    public void resetMove(){
        this.row = -1;
        this.col = -1;
    }

    public void setMove(int userCode, int r, int c){
        //set move on all direction
        for(int x=-1; x<=1; x++){
            for(int y=-1; y<=1; y++){
                if(x==0 && y==0){
                    this.setBoardData(r, c, userCode);
                }else{
                    if (isPossibleMoveDirection(userCode, r, c, x, y)) {
                        setMoveDirection(userCode, r, c, x, y);
                    }
                }
            }
        }
    }

    public void setMoveDirection(int userCode, int r, int c, int x, int y) {

        int len = 1;

        while(true) {
            int xl = r + x * len;
            int yl = c + y * len;

            if (xl >= 0 && yl >= 0 && xl < boardSize && yl < boardSize) {

                int cell = getBoardData(xl, yl);
                if (cell != NULL && cell != userCode) {
                    setBoardData(xl, yl, userCode);
                    len++;
                    continue;
                }
            }
            break;
        }
    }

    public int possibleMoveCount(int userCode){

        int possibleMove = 0;
        for(int r=0; r<boardSize; r++){
            for(int c=0; c<boardSize; c++){
                if(getBoardData(r, c) == NULL){
                    if(isPossibleMove(userCode, r, c)) {
                        possibleMove++;
                        //Log.d("test", "pos:"+r+","+c);
                    }
                }
            }
        }

        return possibleMove;
    }

    public Boolean isPossibleMove(int userCode, int r, int c){

        //check different direction
        for(int x=-1; x<=1; x++){
            for(int y=-1; y<=1; y++){
                if(!(x==0 && y==0)){
                    if (isPossibleMoveDirection(userCode, r, c, x, y)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public Boolean isPossibleMoveDirection(int userCode, int r, int c, int x, int y) {

        int len = 1;

        while(true) {
            int xl = r + x * len;
            int yl = c + y * len;

            if (xl >= 0 && yl >= 0 && xl < boardSize && yl < boardSize) {

                int cell = getBoardData(xl, yl);
                if (cell != NULL) {
                    if (cell != userCode) {
                        //extend the direction check
                        len++;
                        continue;
                    } else if (cell == userCode && len > 1) {
                        //
                        return true;
                    }
                }
            }

            break;
        }

        return false;
    }

}
