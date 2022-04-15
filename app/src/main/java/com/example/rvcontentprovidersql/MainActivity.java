package com.example.rvcontentprovidersql;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.rvcontentprovidersql.contenprovider.RequestProvider;
import com.example.rvcontentprovidersql.tablemoc.TableItems;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public final int offset = 30;
    private int page = 0;

    private RecyclerView mRecyclerView;
    private boolean loadingMore = false;
    private Toast shortToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        CustomCursorRecyclerViewAdapter mAdapter = new CustomCursorRecyclerViewAdapter(this, null);

        mRecyclerView =(RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        int itemsCountLocal = getItemsCountLocal();
        if (itemsCountLocal == 0){
            fillTestElements();
        }

        shortToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int maxPosition = layoutManager.getItemCount();

                if (lastVisibleItemPosition == maxPosition -1){
                    if (loadingMore)
                        return;

                    loadingMore = true;
                    page++;
                    getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
                }
            }
        });

        getSupportLoaderManager().restartLoader(0,null,this);
    }

    private void fillTestElements() {
        int size =1000;
        ContentValues[] cvArray = new ContentValues[size];
        for (int i = 0; i < cvArray.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(TableItems.TEXT, ("text" + i));
            cvArray[i] = cv;
        }

        getContentResolver().bulkInsert(RequestProvider.urlForItems(0), cvArray);
    }

    private int getItemsCountLocal(){
        int itemsCount = 0;

        Cursor query = getContentResolver().query(RequestProvider.urlForItems(0), null, null, null, null);
        if (query != null) {
            itemsCount = query.getCount();
            query.close();
        }
        return itemsCount;
    }


    //Loader

    @Override
    public  Loader<Cursor> onCreateLoader(int id,Bundle args) {
        switch (id) {
            case 0:
                return new CursorLoader(this, RequestProvider.urlForItems(offset * page), null, null, null, null);
            default:
                throw new IllegalArgumentException("no id handled");
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case 0:
                Log.d(TAG, "OnLoadFinished : Loading More");
                shortToast.setText("Loading More" + page);
                shortToast.show();

                Cursor cursor = ((CustomCursorRecyclerViewAdapter)mRecyclerView.getAdapter()).getCursor();


                //fill all exisitng
                MatrixCursor mx = new MatrixCursor(TableItems.Columns);
                fillMx(cursor, mx);

                fillMx(data, mx);

                ((CustomCursorRecyclerViewAdapter)mRecyclerView.getAdapter()).swapCursor(mx);

                handlerToWait.postDelayed(new Runnable() {
                    @Override
                            public void run() {
                        loadingMore = false;}
                }, 2000);

                break;
            default:
                throw new IllegalArgumentException("no loader id handled");

            }
        }

        private Handler handlerToWait = new Handler();

    @SuppressLint("Range")
    private  void   fillMx(Cursor data, MatrixCursor mx){
        if (data == null)
            return;

        data.moveToPosition(-1);
        while (data.moveToNext()){
            mx.addRow(new Object[]{
                    data.getString(data.getColumnIndex(TableItems._ID)),
                    data.getString(data.getColumnIndex(TableItems.TEXT))
            });
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        //TODO : 2016-10-13
    }

    //

    private static final String TAG = "MainActivity";
}