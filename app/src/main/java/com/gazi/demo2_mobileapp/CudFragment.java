package com.gazi.demo2_mobileapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CudFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    TextInputEditText dateEditText, timeEditText, titleEditText, descriptionEditText;
    int hourOfDay, minuteOfDay;
    Button buttonRemind;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    Calendar calendar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cud, container, false);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), this, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date = dayOfMonth + "-" + month + "-" + year;
        dateEditText.setText(date);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dateEditText = getView().findViewById(R.id.textInputEditTextDate);
        titleEditText = getView().findViewById(R.id.textInputEditTextTitle);
        descriptionEditText = getView().findViewById(R.id.textInputEditTextDescription);
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        timeEditText = getView().findViewById(R.id.textInputEditTextTime);
        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
        buttonRemind = getView().findViewById(R.id.button);
        buttonRemind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData();
            }
        });

    }


    private void showTimePickerDialog() {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                hourOfDay = hour;
                minuteOfDay = minute;
                timeEditText.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfDay));
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), onTimeSetListener, hourOfDay, minuteOfDay, true);
        timePickerDialog.setTitle("Zaman Seçiniz");
        timePickerDialog.show();
    }

    public void uploadData() {
        String title = titleEditText.getText().toString();
        String desc = descriptionEditText.getText().toString();
        String datte = dateEditText.getText().toString();
        String time = timeEditText.getText().toString();
        String id = FirebaseDatabase.getInstance().getReference("Reminders").push().getKey();
        Reminders reminders = new Reminders(datte, time, id, title, desc);
        FirebaseDatabase.getInstance().getReference("Reminders").child(id).setValue(reminders)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(getActivity(), "Hatırlatıcı başarıyla oluşturuldu!", Toast.LENGTH_LONG).show();
                            showNotification(datte,time);
                            titleEditText.setText("");
                            descriptionEditText.setText("");
                            dateEditText.setText("");
                            timeEditText.setText("");
                            FragmentManager manager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
                            manager.beginTransaction().replace(R.id.frame_layout, new ReminderFragment())
                                    .commit();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Hatırlatıcı oluşturulamadı!!!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showNotification(String date,String time) {
        String CHANNEL_ID = "reminder";
        String CHANNEL_NAME = "Dendro Reminder";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableVibration(true);
        NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        channel.canShowBadge();
        int NOTIFICATION_ID = 52;
        Notification notification = new Notification.Builder(getContext(), CHANNEL_ID)
                .setContentTitle("Bahçenize yeni bir bitki eklendi!")
                .setContentText(date+" tarihinde ve "+time+" saatinde yeni bir hatırlatıcınız var!")
                .setSmallIcon(R.drawable.baseline_compost_24)
                .setAutoCancel(true)
                .build();
        NotificationManager managerr = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        managerr.notify(NOTIFICATION_ID, notification);
    }
}