package com.newsapp.maximka.newsapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.newsapp.maximka.newsapp.NewsApp;
import com.newsapp.maximka.newsapp.R;
import com.newsapp.maximka.newsapp.adapters.NewsCategoryAdapter;
import com.newsapp.maximka.newsapp.models.DaoSession;
import com.newsapp.maximka.newsapp.models.NewsCategory;
import com.newsapp.maximka.newsapp.models.NewsCategoryDao;

import org.greenrobot.greendao.async.AsyncOperation;
import org.greenrobot.greendao.async.AsyncOperationListener;
import org.greenrobot.greendao.async.AsyncSession;
import org.greenrobot.greendao.query.Query;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoriesFragment extends Fragment implements AsyncOperationListener {
    private Query<NewsCategory> query;
    private NewsCategoryDao dao;
    private AsyncSession session;
    @BindView(R.id.categoriesListView) ListView categoriesList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        DaoSession syncSession = ((NewsApp) getActivity().getApplication()).getSession();
        session = syncSession.startAsyncSession();
        session.setListenerMainThread(this);
        dao = syncSession.getNewsCategoryDao();
        query = dao.queryBuilder()
                .where(NewsCategoryDao.Properties.IsHidden.eq(false))
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        ButterKnife.bind(this, view);
        session.queryList(query);
        return view;
    }

    public void restoreAllCategories() {
        List<NewsCategory> categories = dao.queryBuilder()
                .where(NewsCategoryDao.Properties.IsHidden.eq(true))
                .list();
        for (NewsCategory category : categories) {
            category.setHidden(false);
        }
        dao.updateInTx(categories);

        categoriesList.setAdapter(new NewsCategoryAdapter(getActivity(), query.list()));
    }

    @Override
    public void onAsyncOperationCompleted(AsyncOperation operation) {
        List<NewsCategory> categories = (List<NewsCategory>)operation.getResult();
        categoriesList.setAdapter(new NewsCategoryAdapter(getActivity(), categories));
    }
}
