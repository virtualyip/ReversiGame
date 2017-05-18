package hk.hku.cs.assignment1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by kinyipchan on 2/11/15.
 */
public class StartActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);

        initView();
    }

    public void initView(){

        Button btn_start = (Button) findViewById(R.id.btn_start);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //selectBoardSize();

                newGame(8);
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
                    newGame(6+which*2);
                }
            }
        });
        builder.show();
    }

    public void newGame(int boardSize){
        Intent intent = new Intent();
        intent.setClass(StartActivity.this, MainActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt("boardSize", boardSize);
        intent.putExtras(bundle);

        startActivity(intent);
    }
}
