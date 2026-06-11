package com.example.task_23;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            return true;
        }
        else if (id == R.id.action_credits) {
            new AlertDialog.Builder(this)
                    .setTitle("קרדיטים ופרטי הגשה")
                    .setMessage("אפליקציית Expense Manager\n\n" +
                            "מגישה: שלי\n" +
                            "כיתה: יא'\n" +
                            "פרויקט גמר - 5 יח\"ל מדעי המחשב\n" +
                            "שנת הגשה: 2026")
                    .setPositiveButton("סגור", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}