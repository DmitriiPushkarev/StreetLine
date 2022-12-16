package com.example.streetline;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {

    TextView score, login, comment, typeOfRoad;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        score = itemView.findViewById(R.id.rating);
        login = itemView.findViewById(R.id.login);
        comment = itemView.findViewById(R.id.comment);
        typeOfRoad = itemView.findViewById(R.id.type_of_road);
    }
}
