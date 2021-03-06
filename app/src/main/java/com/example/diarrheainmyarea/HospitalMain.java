package com.example.diarrheainmyarea;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HospitalMain extends AppCompatActivity {

    private Spinner diseasespinner,localityspinner;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private Button msubmit;
    private EditText editDate,AgeText;
    private Calendar calendar;
    private int year, month, day;
    private RadioGroup radioGroup;
    private RadioButton radioButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_main);

        editDate  = findViewById(R.id.edtDate);
        diseasespinner=(Spinner)findViewById(R.id.diseases_spinner);
        localityspinner=(Spinner)findViewById(R.id.locality_spinner);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        msubmit = (Button)findViewById(R.id.submit);
        AgeText = findViewById(R.id.edtAge);
        radioGroup = findViewById(R.id.rdbGroup);

        msubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                submitdata();
            }
        });

        editDate.setOnClickListener(dateClickListener);

        diseasespinnerfill();
        localityspinnerfill();
    }

    private void submitdata() {
        progressBar.setVisibility(View.VISIBLE);
            DatabaseReference dbcase = FirebaseDatabase.getInstance().getReference("Case");

            final String date = editDate.getText().toString();
            final String disease = diseasespinner.getSelectedItem().toString();
            String location = localityspinner.getSelectedItem().toString();
            String age = AgeText.getText().toString();

            int selectedid = radioGroup.getCheckedRadioButtonId();
            radioButton=findViewById(selectedid);
            String gender = radioButton.getText().toString();

            final cases cases = new cases(date,disease,location,age,gender);
            dbcase.push().setValue(cases);
            progressBar.setVisibility(View.GONE);

        final DatabaseReference dbareacount = FirebaseDatabase.getInstance().getReference("Area/Surat").child(location).child("NumberOfTotalCases");
        dbareacount.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()) {
                int count = Integer.parseInt(String.valueOf(dataSnapshot.getValue()));
                count++;
                dbareacount.setValue(count);
            }
            else{
                dbareacount.setValue(1);
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        final DatabaseReference dbdate = FirebaseDatabase.getInstance().getReference("Date").child(date).child(disease);
        dbdate.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    int count= Integer.parseInt(String.valueOf(dataSnapshot.getValue()));
                    count++;
                    dbdate.setValue(count);
                }
                else{

                    dbdate.setValue(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference dbdatetotalcount = FirebaseDatabase.getInstance().getReference("Date").child(date);
        final DatabaseReference dbdatetotalcounta = FirebaseDatabase.getInstance().getReference("Area/Date").child(date);
        dbdatetotalcount.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int total=0;
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String totalcount = String.valueOf(ds.getValue());
                    total=Integer.parseInt(totalcount);
                    total++;
                   // dbdatetotalcount.child(date).setValue(count);

                }
                //total+=1;
                dbdatetotalcount.child("totalcount").setValue(total);
                dbdatetotalcounta.child("totalcount").setValue(total);
                //System.out.println(total);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


      final DatabaseReference abdiseasecountareawise = FirebaseDatabase.getInstance().getReference("Area/Surat").child(location).child(disease);
        final DatabaseReference abdiseasecountareawisea = FirebaseDatabase.getInstance().getReference("Area/Surat").child(location).child("Diseases").child(disease);
            abdiseasecountareawise.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        int count = Integer.parseInt(String.valueOf(dataSnapshot.getValue()));
                        count++;
                        abdiseasecountareawise.setValue(count);
                        abdiseasecountareawisea.setValue(count);

                    }
                    else{
                        abdiseasecountareawise.setValue(1);
                        abdiseasecountareawisea.setValue(1);
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        progressBar.setVisibility(View.GONE);
        Toast.makeText(getBaseContext(), "Data Submitted Successfully", Toast.LENGTH_SHORT).show();


    }

    private View.OnClickListener dateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);

            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            showDialog(999);
            showDate(year, month+1, day);
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2+1, arg3);
                }
            };

    private void showDate(int year, int month, int day) {
        editDate.setText(new StringBuilder().append(day).append("-")
                .append(month).append("-").append(year));
    }

    private void localityspinnerfill() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Area/Surat");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<String> titleList = new ArrayList<String>();
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    String name = dataSnapshot1.getKey();
                    titleList.add(name);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(HospitalMain.this, android.R.layout.simple_spinner_item, titleList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                localityspinner.setAdapter(arrayAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void diseasespinnerfill() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Diseases");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<String> titleList = new ArrayList<String>();
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){

                    String name = dataSnapshot1.getValue(String.class);
                    titleList.add(name);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(HospitalMain.this, android.R.layout.simple_spinner_item, titleList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                diseasespinner.setAdapter(arrayAdapter);
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
