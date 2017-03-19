package com.newsapp.maximka.newsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.newsapp.maximka.newsapp.NewsActivity;
import com.newsapp.maximka.newsapp.NewsApp;
import com.newsapp.maximka.newsapp.R;
import com.newsapp.maximka.newsapp.models.NewsFeed;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private static final int TARGET_WIDTH = 1200;
    private static final int TARGET_HEIGHT = 600;
    private LayoutInflater layoutInflater;
    private List<NewsFeed> objects;
    private Context ctx;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.news, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NewsFeed news = objects.get(position);

        holder.name.setText(news.getTitle());
        holder.description.setText(news.getDescription());
        holder.dateTime.setText(NewsApp.dateTimeFormatter.toString(news.getPublicationDateTime()));
        Picasso.with(ctx)
                .load(news.getImageUrl())
                .resize(TARGET_WIDTH, TARGET_HEIGHT)
                .centerCrop()
                .onlyScaleDown()
                .placeholder(R.drawable.news_placeholder)
                .tag(ctx)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.newsName) public TextView name;
        @BindView(R.id.newsDescription) public TextView description;
        @BindView(R.id.newsImage) public ImageView image;
        @BindView(R.id.newsDateTime) public TextView dateTime;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ctx, NewsActivity.class);
            intent.putExtra("news_id", getAdapterPosition());
            ctx.startActivity(intent);
        }
    }

    public NewsAdapter(Context context, List<NewsFeed> news) {
        ctx = context;
        objects = news;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}
