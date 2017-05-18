package hk.hku.cs.assignment1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;


public class MainActivity extends Activity {

    private static Handler mHandler;

    private Button btn_newGame;
    private ToggleButton btn_hints;
    private TextView mBlackCtn;
    private TextView mWhiteCtn;
    private TextView mMessage;
    private TableLayout tbl_Board;

    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        Bundle bundle =this.getIntent().getExtras();
        Integer boardSize = bundle.getInt("boardSize");
        if(boardSize > 0)
            newGame(boardSize);
    }

    public void initView(){
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 0){
                    initBoard();
                }else if(msg.what == 1){
                    render();
                }
            }
        };

        btn_newGame = (Button) findViewById(R.id.btn_start);
        btn_hints = (ToggleButton) findViewById(R.id.btn_hint);
        //btn_hints.setChecked(false);

        mBlackCtn = (TextView) findViewById(R.id.black_cnt);
        mWhiteCtn = (TextView) findViewById(R.id.white_cnt);
        mMessage = (TextView) findViewById(R.id.system_message);

        btn_hints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (game != null) {
                    game.showHints = btn_hints.isChecked();
                    render();
                }
            }
        });

        btn_newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectBoardSize();
            }
        });

    }

    public void selectBoardSize(){
        CharSequence size[] = new CharSequence[] {"6 x 6", "8 x 8 (Default)", "10 x 10", "12 x 12"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Board Size");
        builder.setItems(size, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which >= 0) {
                    newGame(6 + which * 2);
                }
            }
        });
        builder.show();
    }

    public void newGame(int boardSize){
        game = new Game(mHandler);
        game.showHints = btn_hints.isChecked();
        game.setBoardSize(boardSize);
        game.start();
    }

    public void initBoard(){
        if(game != null){
            tbl_Board = (TableLayout) findViewById(R.id.borad);
            tbl_Board.removeAllViews();

            int boardSize = game.getBoardSize();

            class boardCellListener implements View.OnClickListener {
                private int r;
                private int c;

                public boardCellListener(int r, int c) {
                    this.r = r;
                    this.c = c;
                }

                @Override
                public void onClick(View v) {
                    game.setMove(r, c);
                }
            }

            for(int r=0; r<boardSize; r++) {
                TableRow tbrow = new TableRow(this);
                for(int c=0; c<boardSize; c++) {
                    ImageView cell = new ImageView(this);
                    cell.setOnClickListener(new boardCellListener(r, c));
                    tbrow.addView(cell);
                }
                tbl_Board.addView(tbrow);
            }
        }
    }

    public void render(){
        mHandler.post(new Runnable() {
            public void run() {
                int boardSize = game.getBoardSize();
                Integer blackCount = 0;
                Integer whiteCount = 0;

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int screen_width = size.x;
                //int screen_height = size.y;

                int cell_width = screen_width / boardSize;
                int cell_height = cell_width;

                for (int r = 0; r < boardSize; r++) {
                    TableRow row = (TableRow) tbl_Board.getChildAt(r);
                    row.setBackgroundColor(Color.rgb(0, 0, 0));
                    for (int c = 0; c < boardSize; c++) {

                        ImageView cell = (ImageView) row.getChildAt(c);
                        //cell.setPadding(10, 10, 10, 10);
                        cell.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        if ((r + c) % 2 == 0) {
                            cell.setBackgroundColor(Color.rgb(220, 220, 120));
                        } else {
                            cell.setBackgroundColor(Color.rgb(140, 140, 70));
                        }


                        int cellData = game.getBoardData(r, c);
                        if (cellData == game.BLACK) {
                            blackCount++;
                            //col.setText("[" + r + "," + c + "]" + game.BLACK);
                            cell.setImageResource(R.drawable.black_chess);
                        } else if (cellData == game.WHITE) {
                            whiteCount++;
                            //col.setText("[" + r + "," + c + "]" + game.WHITE);
                            cell.setImageResource(R.drawable.white_chess);
                        } else if (cellData == game.NULL && game.showHints && game.isPossibleMove(game.turn, r, c)) {
                            //col.setText("[" + r + "," + c + "]X");
                            if (game.turn == game.BLACK) {
                                cell.setImageResource(R.drawable.black_chess_t);
                            } else if (game.turn == game.WHITE) {
                                cell.setImageResource(R.drawable.white_chess_t);
                            }
                        } else {
                            //col.setText("[" + r + "," + c + "]0");
                            cell.setImageResource(R.drawable.transparent);
                        }
                        imageResize(cell, cell_width, cell_height);

                        // Now change ImageView's dimensions to match the scaled image
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cell.getLayoutParams();
                        params.width = cell_width;
                        params.height = cell_height;
                        cell.setLayoutParams(params);
                    }
                }
                mBlackCtn.setText(blackCount.toString());
                mWhiteCtn.setText(whiteCount.toString());
                mMessage.setText(game.message);
                if (game.gameOver) {
                    if (blackCount > whiteCount) {
                        mMessage.setText("Black Win. Please NEW GAME button to start new game");
                    } else if (blackCount < whiteCount) {
                        mMessage.setText("White Win. Please NEW GAME button to start new game");
                    } else {
                        //draw
                        mMessage.setText("Game draw. Please NEW GAME button to start new game");
                    }
                }
            }
        });
    }


    public void imageResize(ImageView imageView, int swidth, int sheight){
        Drawable drawing = imageView.getDrawable();
        if (drawing == null) {
            return; // Checking for null & return, as suggested in comments
        }
        Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float xScale = ((float) swidth) / width;
        float yScale = ((float) sheight) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
        imageView.setImageDrawable(result);
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/

}
