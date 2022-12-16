package com.example.streetline;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<ViewHolder> {

    Context context;
    List<RouteInfoUtility> info;

    public Adapter(Context context, List<RouteInfoUtility> info) {
        this.context = context;
        this.info = info;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.info,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.score.setText("Оценка: " + Integer.toString(info.get(position).getScore()));
        holder.login.setText("Пользователь: " +info.get(position).getLogin());
        holder.typeOfRoad.setText("Тип дороги: " +info.get(position).getTypeOfRoad());
        holder.comment.setText("Комментарий: " +info.get(position).getComment());
    }

    @Override
    public int getItemCount() {
        return info.size();
    }
}
