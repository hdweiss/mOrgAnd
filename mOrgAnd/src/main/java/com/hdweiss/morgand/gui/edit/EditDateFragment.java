package com.hdweiss.morgand.gui.edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.data.dao.OrgNode;

import java.util.Calendar;

public class EditDateFragment extends BaseEditFragment {

    private DatePicker datePicker;
    private CheckBox timeStartCheckbox;
    private TimePicker timeStartPicker;
    private CheckBox timeEndCheckbox;
    private TimePicker timeEndPicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_date_fragment, container, false);

        datePicker = (DatePicker) view.findViewById(R.id.date);
        datePicker.setCalendarViewShown(false);
        datePicker.setSpinnersShown(true);

        timeStartPicker = (TimePicker) view.findViewById(R.id.time_start);
        timeStartPicker.setIs24HourView(true);
        timeStartCheckbox = (CheckBox) view.findViewById(R.id.time_start_check);

        timeEndPicker = (TimePicker) view.findViewById(R.id.time_end);
        timeEndPicker.setIs24HourView(true);
        timeEndCheckbox = (CheckBox) view.findViewById(R.id.time_end_check);

        timeStartCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    timeStartPicker.setVisibility(View.VISIBLE);
                    timeEndCheckbox.setEnabled(true);
                }
                else {
                    timeStartPicker.setVisibility(View.INVISIBLE);
                    timeEndCheckbox.setChecked(false);
                    timeEndCheckbox.setEnabled(false);
                }
            }
        });

        timeEndCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked)
                    timeEndPicker.setVisibility(View.VISIBLE);
                else
                    timeEndPicker.setVisibility(View.INVISIBLE);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


//        OrgNodeTimeDate timeDate = null; // TODO Complete
        controller.getNode().getTitle();
//        if (timeDate != null)
//            type = timeDate.type;
//        else
//            type = OrgNodeTimeDate.TYPE.Scheduled;

//        getDialog().setTitle(getResources().getString(R.string.action_set_time) + " - " + type.toString());

//        if (timeDate != null)
//            populateView(timeDate);
//        else
//            populateView();
    }

    private void populateView() {
        Calendar c = Calendar.getInstance();
        datePicker.updateDate(c.get(Calendar.YEAR) - 1, c.get(Calendar.MONTH) - 1, c.get(Calendar.DAY_OF_MONTH) - 1);

        timeStartCheckbox.setChecked(false);
        timeStartPicker.setVisibility(View.INVISIBLE);

        timeEndCheckbox.setChecked(false);
        timeEndCheckbox.setEnabled(false);
        timeEndPicker.setVisibility(View.INVISIBLE);
    }

//    private void populateView(OrgNodeTimeDate timeDate) {
//        if (timeDate.year >= 0 && timeDate.monthOfYear >= 0 && timeDate.dayOfMonth >= 0)
//            datePicker.updateDate(timeDate.year - 1, timeDate.monthOfYear - 1, timeDate.dayOfMonth - 1);
//
//        if (timeDate.startTimeOfDay >= 0 && timeDate.startMinute >= 0) {
//            timeStartPicker.setCurrentHour(timeDate.startTimeOfDay);
//            timeStartPicker.setCurrentMinute(timeDate.startMinute);
//            timeStartCheckbox.setChecked(true);
//        } else
//            timeStartCheckbox.setChecked(false);
//
//        if (timeDate.endTimeOfDay >= 0 && timeDate.endMinute >= 0) {
//            timeEndPicker.setCurrentHour(timeDate.endTimeOfDay);
//            timeEndPicker.setCurrentMinute(timeDate.endMinute);
//            timeEndCheckbox.setChecked(true);
//        } else
//            timeEndCheckbox.setChecked(false);
//    }
//
//
//    private OrgNodeTimeDate getTimeDate() {
//        OrgNodeTimeDate resultTimeDate = new OrgNodeTimeDate(type);
//
//        resultTimeDate.year = datePicker.getYear() + 1;
//        resultTimeDate.monthOfYear = datePicker.getMonth() + 1;
//        resultTimeDate.dayOfMonth = datePicker.getDayOfMonth() + 1;
//
//        if (timeStartCheckbox.isChecked()) {
//            resultTimeDate.startTimeOfDay = timeStartPicker.getCurrentHour();
//            resultTimeDate.startMinute = timeStartPicker.getCurrentMinute();
//
//            if (timeEndCheckbox.isChecked()) {
//                resultTimeDate.endTimeOfDay = timeEndPicker.getCurrentHour();
//                resultTimeDate.endMinute = timeEndPicker.getCurrentMinute();
//            }
//        }
//
//        return resultTimeDate;
//    }

    @Override
    public OrgNode getEditedNode() {
        return controller.getNode();
    }
}
