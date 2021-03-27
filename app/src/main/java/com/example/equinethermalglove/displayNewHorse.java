package com.example.equinethermalglove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.SimpleTimeZone;

public class displayNewHorse extends AppCompatActivity {

    private static final int maxX = 5;
    private static final int maxY = 200;
    private static final int minY = 0;
    private static final String SET_LABEL = "Horse Temperature Data";
    private static final ArrayList<String> labels = new ArrayList<>();
    private EditText horse;
    private Spinner limb;

    BarChart barChart;
    ArrayList<Integer> dt = new ArrayList<>();
    String userID;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // TODO: add logic for returning to database menu and deleting horse
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_new_horse);

        final Button rtn = findViewById(R.id.return_btn);
        final Button save = findViewById(R.id.save_btn);
        horse = findViewById(R.id.horse_name);
        dt.add(12); dt.add(34); dt.add(21); dt.add(54); dt.add(2);
        limb = findViewById(R.id.horseLimb);
        userID = "test";
        String[] limbOptions = {"Front Left", "Front Right", "Back Left", "Back Right"};
        ArrayAdapter<String> sAdapt = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, limbOptions);
        limb.setAdapter(sAdapt);

        rtn.setOnClickListener(v -> {
            Intent intent = new Intent(displayNewHorse.this, MainActivity.class);
            startActivity(intent);
        });

        save.setOnClickListener(v -> {
            //TODO: add functionality to save data to firebase database
            String h = horse.getText().toString();
            String l;
            if (limb.getSelectedItem() == "Front Right") {
                l = "frontRight";
            } else if(limb.getSelectedItem() == "Front Left") {
                l = "frontLeft";
            } else if (limb.getSelectedItem() == "Back Left") {
                l = "backLeft";
            } else if (limb.getSelectedItem() == "Back Right") {
                l = "backRight";
            } else {
                l = "";
            }
            writeToDb(h, l);
        });
        // get the data from bluetooth scan for display
        //dt = getIntent().getSerializableExtra("data");
        barChart = findViewById(R.id.barchart);
        BarData data = createData();
        appearance();
        prepareData(data);
    }

    private BarData createData() {
        ArrayList<BarEntry> values = new ArrayList<>();
        int x, y;
        for (int i = 0; i < maxX; i++) {
            x = i;
            // get the data from bluetooth for display
            y = dt.get(i);
            values.add(new BarEntry(x, y));
        }

        BarDataSet set = new BarDataSet(values, SET_LABEL);;

        return new BarData(set);
    }

    private void prepareData(BarData data) {
        data.setValueTextSize(12f);
        barChart.setData(data);
        barChart.invalidate();
    }

    private void appearance() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawValueAboveBar(false);
        XAxis x = barChart.getXAxis();
        barChart.getXAxis().setGranularityEnabled(true);
        labels.add("Thumb"); labels.add("Index"); labels.add("Middle"); labels.add("Ring"); labels.add("Pinky");
        Object[] l = labels.toArray();
        x.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (String) l[(int) value];
            }
        });

        YAxis lAxis = barChart.getAxisLeft();
        YAxis rAxis = barChart.getAxisRight();

        lAxis.setGranularity(1f);
        lAxis.setAxisMinimum(0);

        rAxis.setGranularity(1f);
        rAxis.setAxisMinimum(0);
    }

    private void writeToDb(String horseName, String limb) {
        HashMap<String, Object> user = new HashMap<>();
        user.put("value", 1);
        HashMap<String, Object> data = new HashMap<>();
        data.put("temp", dt);

        db.collection(userID).document(horseName).set(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("data added", "Data added to database");
            } else {
                Log.d("data added", "data not added to database");
            }
        });

        Calendar curDate = Calendar.getInstance();
        String date = curDate.get(Calendar.DAY_OF_MONTH) + "-" + (curDate.get(Calendar.MONTH) + 1) + "-" + curDate.get(Calendar.YEAR);

        db.collection(userID).document(horseName).collection(limb).document(date).set(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("data added", "Data added to database");
            } else {
                Log.d("data added", "data not added to database");
            }
        });
    }
}