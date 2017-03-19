package com.newsapp.maximka.newsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.newsapp.maximka.newsapp.NewsListActivity;
import com.newsapp.maximka.newsapp.R;
import com.newsapp.maximka.newsapp.gestures.CustomClick;
import com.newsapp.maximka.newsapp.models.NewsCategory;

import java.util.List;


public class NewsCategoryAdapter extends BaseAdapter{
    private Context ctx;
    private LayoutInflater lInflater;
    private List<NewsCategory> objects;

    public NewsCategoryAdapter(Context context, List<NewsCategory> categories) {
        ctx = context;
        objects = categories;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = lInflater.inflate(R.layout.news_category, parent, false);
        }
        NewsCategory category = (NewsCategory) getItem(position);

        TextView categoryName = (TextView) convertView.findViewById(R.id.categoryName);
        categoryName.setText(category.getName());
        categoryName.setOnTouchListener(new CustomClick(categoryClick));
        categoryName.setTag(position);

        CheckBox hideCheckbox = (CheckBox) convertView.findViewById(R.id.hideCategoryCheckbox);
        hideCheckbox.setTag(position);
        hideCheckbox.setOnCheckedChangeListener(null);
        hideCheckbox.setChecked(!category.isHidden());
        hideCheckbox.setOnCheckedChangeListener(myCheckChangeList);

        return convertView;
    }

    private OnCheckedChangeListener myCheckChangeList = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            int position = (Integer) buttonView.getTag();
            NewsCategory category = objects.get(position);
            category.setHidden(true);
            category.update();

            objects.remove(position);
            notifyDataSetChanged();
        }
    };

    private View.OnClickListener categoryClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();

            Intent intent = new Intent(ctx, NewsListActivity.class);
            intent.putExtra("category_id", objects.get(position).getId());
            ctx.startActivity(intent);
        }
    };
}
