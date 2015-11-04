package com.ksk.sunshine;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by KOTA_Okada on 15/11/05.
 */
public class ListFragment extends android.support.v4.app.Fragment {

    ListView mListview;
    FragmentActivity activity;


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        String[] data = {"Today - Sunny","Tomorrow - Rainy","Wednesday - i don't know","Thursday - Sunny","Friday - -Let's drink!"};

        //ListView

        if(getActivity() == null) {
            System.out.print("getActivity is null");
            //getActivity()はnullじゃない
        }

        /*
        *
        * mListViewがnullなのがどうしてもわからない、getActivity().findViewByIdで出来ず。。
        *
        * */


        mListview = (ListView)container.findViewById(R.id.listview_forecast);
        //arraylistに変換（後で調べる）
        List<String> datalist = new ArrayList<String>(Arrays.asList(data));
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,R.id.list_item_forecast_textview,datalist);

        //mListViewがnullになってしまっている
        mListview.setAdapter(adapter);



        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //コンストラクタ
    public ListFragment(){

    }


}
