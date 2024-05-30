package org.techtown.moment.ui.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.techtown.moment.DiaryDayFragment;
import org.techtown.moment.R;
import org.techtown.moment.WriteDiaryActivity;

public class CalendarFragment extends Fragment {

    FloatingActionButton writeBtn;
    CalendarView calendarView;
    DiaryDayFragment diaryDayFragment;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View cal = inflater.inflate(R.layout.fragment_calendar, container, false);

        writeBtn=(FloatingActionButton) cal.findViewById(R.id.calWriteBtn);
        writeBtn.setOnClickListener(this::onClick);

        Bundle bundle=new Bundle();

        diaryDayFragment=new DiaryDayFragment();


        calendarView =cal.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Log.d("calendar",year+"년 "+(month+1)+"월 "+dayOfMonth+"일");
                bundle.putInt("year",year);
                bundle.putInt("month",month+1);
                bundle.putInt("day",dayOfMonth);

                //FragmentTransaction fT= getActivity().getSupportFragmentManager().beginTransaction();
                //fT.replace(R.id.diaryDayListView,diaryDayFragment).addToBackStack(null).commit();

                diaryDayFragment.setArguments(bundle);
            }
        });

        return cal;
    }


    public void onClick(View v){
        Intent intent =new Intent(getActivity(), WriteDiaryActivity.class);
        startActivity(intent);
    }
}