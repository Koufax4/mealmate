package com.example.mealmate.ui.mealplan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmate.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeekDayAdapter extends RecyclerView.Adapter<WeekDayAdapter.ViewHolder> {

    private List<Date> weekDates = new ArrayList<>();
    private int selectedPosition = -1; // Start with no selection
    private final Context context;

    public WeekDayAdapter(Context context) {
        this.context = context;
    }

    public void setWeekDates(List<Date> dates, Date selectedDate) {
        this.weekDates = dates;
        this.selectedPosition = findPositionForDate(selectedDate);
        notifyDataSetChanged();
    }

    private int findPositionForDate(Date date) {
        if (date == null) return 0;
        Calendar today = Calendar.getInstance();
        Calendar itemDate = Calendar.getInstance();

        for (int i = 0; i < weekDates.size(); i++) {
            itemDate.setTime(weekDates.get(i));
            if (today.get(Calendar.DAY_OF_YEAR) == itemDate.get(Calendar.DAY_OF_YEAR) &&
                    today.get(Calendar.YEAR) == itemDate.get(Calendar.YEAR)) {
                return i;
            }
        }
        // If today is not in the list, default to Monday
        return 0;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_week_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Date date = weekDates.get(position);
        holder.bind(date, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return weekDates.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textDayOfWeek;
        private final TextView textDayOfMonth;
        private final View selectionBackground;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDayOfWeek = itemView.findViewById(R.id.text_day_of_week);
            textDayOfMonth = itemView.findViewById(R.id.text_day_of_month);
            selectionBackground = itemView.findViewById(R.id.selection_background);
        }

        void bind(Date date, boolean isSelected) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.getDefault());

            textDayOfWeek.setText(dayFormat.format(date));
            textDayOfMonth.setText(dateFormat.format(date));

            if (isSelected) {
                selectionBackground.setVisibility(View.VISIBLE);
                textDayOfMonth.setTextColor(ContextCompat.getColor(context, R.color.white));
                textDayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.purple_700));
            } else {
                selectionBackground.setVisibility(View.GONE);
                textDayOfMonth.setTextColor(ContextCompat.getColor(context, R.color.black));
                textDayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.gray_600));
            }
        }
    }
}