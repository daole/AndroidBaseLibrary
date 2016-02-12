package com.dreamdigitizers.androidbaselibrary.views.classes.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class FragmentBase extends Fragment {
    private static final String ERROR_MESSAGE__CONTEXT_NOT_IMPLEMENTS_INTERFACE = "Activity must implement IStateChecker.";

    protected IStateChecker mStateChecker;

    @Override
    public void onAttach(Context pContext) {
        super.onAttach(pContext);
        try {
            this.mStateChecker = (IStateChecker) pContext;
        } catch (ClassCastException e) {
            throw new ClassCastException(FragmentBase.ERROR_MESSAGE__CONTEXT_NOT_IMPLEMENTS_INTERFACE);
        }
    }

    @Override
    public void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        Intent intent = this.getActivity().getIntent();
        if (intent != null) {
            this.handleIntent(intent);
        }

        Bundle arguments = this.getArguments();
        if (arguments != null) {
            this.handleArguments(arguments);
        }

        if (pSavedInstanceState != null) {
            this.recoverInstanceState(pSavedInstanceState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {
        View view = this.loadView(pInflater, pContainer);
        this.retrieveScreenItems(view);
        this.mapInformationToScreenItems(view);
        this.setHasOptionsMenu(true);
        return view;
    }

    @Override
    final public void onCreateOptionsMenu(Menu pMenu, MenuInflater pInflater) {
        if (!this.mStateChecker.isBeingCovered(this)) {
            this.createOptionsMenu(pMenu, pInflater);
            int titleStringId = this.getTitle();
            if (titleStringId > 0) {
                ActionBar actionBar = this.getActionBar();
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(titleStringId);
            }
        }
    }

    public boolean onBackPressed() {
        return false;
    }

    protected ActionBar getActionBar() {
        return ((AppCompatActivity) this.getActivity()).getSupportActionBar();
    }

    protected void createOptionsMenu(Menu pMenu, MenuInflater pInflater) {
    }

    protected void handleIntent(Intent pIntent) {
    }

    protected void handleArguments(Bundle pArguments) {
    }

    protected void recoverInstanceState(Bundle pSavedInstanceState) {
    }

    protected abstract View loadView(LayoutInflater pInflater, ViewGroup pContainer);
    protected abstract void retrieveScreenItems(View pView);
    protected abstract void mapInformationToScreenItems(View pView);
    protected abstract int getTitle();

    public interface IStateChecker {
        boolean isRecreated();
        boolean isBeingCovered(FragmentBase pFragment);
    }
}
