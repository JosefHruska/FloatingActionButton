package com.github.clans.fab.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.clans.fab.ExtendedFloatingActionMenu;
import com.github.clans.fab.FloatingActionMenu;
import com.github.fab.sample.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private ListView mListView;
    private FloatingActionMenu mFab;
    private ExtendedFloatingActionMenu mFabExtended;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById(R.id.list);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Locale[] availableLocales = Locale.getAvailableLocales();
        List<String> locales = new ArrayList<>();
        for (Locale locale : availableLocales) {
            locales.add(locale.getDisplayName());
        }

        mListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                android.R.id.text1, locales));
        mFab = (FloatingActionMenu) getActivity().findViewById(R.id.fab);
        mFabExtended = (ExtendedFloatingActionMenu) getActivity().findViewById(R.id.fabExtended);
        mFab.setIconAnimated(false);
        mFab.setClosedOnTouchOutside(true);
        mFab.setMenuButtonShowAnimation(AnimationUtils.loadAnimation(getContext(), (R.anim.fab_scale_up)));
        mFab.setMenuButtonHideAnimation(AnimationUtils.loadAnimation(getContext(), (R.anim.fab_scale_down)));
        mFab.setOnMenuButtonClickListener(new FloatingActionMenu.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mFab.isShown()) {
                    mFab.hideMenu(false);
                    mFabExtended.showMenu(false);
                    mFabExtended.open(false);
                } else {
                    mFabExtended.hideMenu(false);
                    mFab.showMenu(false);
                    mFab.open(false);

                }
            }
        });

        mFabExtended.setOnMenuButtonClickListener(new FloatingActionMenu.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFabExtended.isShown()) {
                    mFabExtended.hideMenu(false);
                    mFab.showMenu(false);
                    mFab.open(false);
                } else {
                    mFab.hideMenu(false);
                    mFabExtended.showMenu(false);
                    mFabExtended.open(false);
                }
            }
        });
    }
}
