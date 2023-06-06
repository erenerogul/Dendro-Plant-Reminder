package com.gazi.demo2_mobileapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class UpdateFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    TextInputEditText titleEdTx,descEdTx,dateEdTx,timeEdTx;
    String taskKey = null;
    Button btnupdate;
    int hourOfDay,minuteOfDay;
    private HashMap<String,String> hashMap;

    public UpdateFragment(HashMap<String, String> hashMap) {
        this.hashMap = hashMap;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleEdTx = getView().findViewById(R.id.textInputEditTextUpdateTitle);
        descEdTx = getView().findViewById(R.id.textInputEditTextUpdateDescription);
        dateEdTx = getView().findViewById(R.id.textInputEditTextUpdateDate);
        timeEdTx = getView().findViewById(R.id.textInputEditTextUpdateTime);
        btnupdate = getView().findViewById(R.id.buttonUpdate);
        dateEdTx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        timeEdTx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
        taskKey = hashMap.get("key");
        titleEdTx.setText(hashMap.get("title"));
        descEdTx.setText((hashMap.get("desc")));
        dateEdTx.setText(hashMap.get("date"));
        timeEdTx.setText(hashMap.get("time"));
        btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> map = new HashMap<>();
                map.put("taskId",taskKey);
                map.put("taskTitle",titleEdTx.getText().toString());
                map.put("taskDescription",descEdTx.getText().toString());
                map.put("date",dateEdTx.getText().toString());
                map.put("time",timeEdTx.getText().toString());
                FirebaseDatabase.getInstance().getReference("Reminders")
                     .child(taskKey).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void unused) {
                             Toast.makeText(getContext(),"Hatırlatıcı başarıyla güncellendi!",Toast.LENGTH_SHORT).show();
                             titleEdTx.setText("");
                             descEdTx.setText("");
                             dateEdTx.setText("");
                             timeEdTx.setText("");
                             FragmentManager manager = ((AppCompatActivity)getContext()).getSupportFragmentManager();
                             manager.beginTransaction().replace(R.id.frame_layout,new ReminderFragment())
                                     .commit();

                         }
                         }).addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             Toast.makeText(getContext(),"Hatırlatıcı güncelleme başarısız!",Toast.LENGTH_SHORT).show();
                         }
                         });
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date = dayOfMonth+"-"+month+"-"+year;
        dateEdTx.setText(date);
    }

    private void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),this, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePickerDialog(){
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                hourOfDay = hour;
                minuteOfDay = minute;
                timeEdTx.setText(String.format(Locale.getDefault(),"%02d:%02d",hourOfDay,minuteOfDay));
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),onTimeSetListener,hourOfDay,minuteOfDay,true);
        timePickerDialog.setTitle("Zaman Seçiniz");
        timePickerDialog.show();
    }
}