package com.gazi.demo2_mobileapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<MyViewHolder> {
    Context context;
    ArrayList<Reminders> remindersArrayList;
    private LayoutInflater inflater;

    public SimpleDateFormat dateFormat = new SimpleDateFormat("EE dd MMM yyyy", Locale.US);
    public SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-M-yyyy", Locale.US);
    Date date = null;
    String outputDateString = null;
    Reminders remind;

    public ReminderAdapter(Context context, ArrayList<Reminders> remindersArrayList) {
        this.context = context;
        this.remindersArrayList = remindersArrayList;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =inflater.from(context).inflate(R.layout.recycler_item,parent,false);
        return  new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        remind = remindersArrayList.get(position);
        holder.title.setText(remind.getTaskTitle());
        holder.desc.setText(remind.getTaskDescription());
        holder.rTime.setText(remind.getTime());
        holder.recId.setText(remind.getTaskId());

        try {
            date = inputDateFormat.parse(remind.getDate());
            outputDateString = dateFormat.format(date);

            String[] items1 = outputDateString.split(" ");
            String day = items1[0];
            String dd = items1[1];
            String month = items1[2];

            holder.recDateDay.setText(day);
            holder.recDate.setText(dd);
            holder.recDateMonth.setText(month);

        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.recOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {ShowBottomDialog(holder);}
        });
    }

    @Override
    public int getItemCount() {
        return remindersArrayList.size();
    }

    public void ShowBottomDialog(MyViewHolder holder){
        final Dialog dialog = new Dialog(inflater.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet);

        LinearLayout updateLayout = dialog.findViewById(R.id.layoutUpdate);
        LinearLayout deleteLayout = dialog.findViewById(R.id.layoutDelete);
        LinearLayout completeLayout = dialog.findViewById(R.id.layoutComplete);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        updateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("key",remindersArrayList.get(holder.getAdapterPosition()).getTaskId());
                hashMap.put("title",remindersArrayList.get(holder.getAdapterPosition()).getTaskTitle());
                hashMap.put("desc",remindersArrayList.get(holder.getAdapterPosition()).getTaskDescription());
                hashMap.put("date",remindersArrayList.get(holder.getAdapterPosition()).getDate());
                hashMap.put("time",remindersArrayList.get(holder.getAdapterPosition()).getTime());

                FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.frame_layout,new UpdateFragment(hashMap))
                        .commit();
            }
        });

        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                FirebaseDatabase.getInstance().getReference("Reminders").child(remindersArrayList.get(holder.getAdapterPosition()).getTaskId()).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(inflater.getContext(),"Hatırlatıcı başarıyla silindi.",Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(inflater.getContext(),"Hatırlatıcı silme işlemi başarısız.",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        completeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                FirebaseDatabase.getInstance().getReference("Reminders").child(remindersArrayList.get(holder.getAdapterPosition()).getTaskId()).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(inflater.getContext(),"Hatırlatıcı başarıyla tamamlandı.",Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(inflater.getContext(),"Hatırlatıcı tamamlanmadı zaman aşımı!!.",Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }


}
class MyViewHolder extends RecyclerView.ViewHolder{

    TextView title,desc,rTime,recDateDay,recDateMonth,recDate,recId;
    CalendarView calendarView;
    ImageView recOptions;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.recTitle);
        desc = itemView.findViewById(R.id.recDescription);
        rTime = itemView.findViewById(R.id.recTime);
        recDateDay = itemView.findViewById(R.id.day);
        recDateMonth = itemView.findViewById(R.id.month);
        recDate = itemView.findViewById(R.id.date);
        recOptions = itemView.findViewById(R.id.options);
        calendarView = itemView.findViewById(R.id.calendarView);
        recId = itemView.findViewById(R.id.recId);
    }
}