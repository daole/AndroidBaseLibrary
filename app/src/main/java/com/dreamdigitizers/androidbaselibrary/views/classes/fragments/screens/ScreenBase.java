package com.dreamdigitizers.androidbaselibrary.views.classes.fragments.screens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.dreamdigitizers.androidbaselibrary.R;
import com.dreamdigitizers.androidbaselibrary.presenters.interfaces.IPresenter;
import com.dreamdigitizers.androidbaselibrary.utils.UtilsDialog;
import com.dreamdigitizers.androidbaselibrary.views.interfaces.IView;
import com.dreamdigitizers.androidbaselibrary.views.classes.fragments.FragmentBase;

public abstract class ScreenBase<P extends IPresenter> extends FragmentBase implements IView {
	private static final String ERROR_MESSAGE__CONTEXT_NOT_IMPLEMENTS_INTERFACE = "Activity must implement IOnScreenActionsListener.";

	protected boolean mIsRecoverable;
	protected P mPresenter;
	protected IOnScreenActionsListener mScreenActionsListener;

	public void onSaveInstanceState(Bundle pOutState) {
		super.onSaveInstanceState(pOutState);
		this.mIsRecoverable = true;
	}

	@Override
	public void onAttach(Context pContext) {
		super.onAttach(pContext);
		try {
			this.mScreenActionsListener = (IOnScreenActionsListener) pContext;
		} catch (ClassCastException e) {
			throw new ClassCastException(ScreenBase.ERROR_MESSAGE__CONTEXT_NOT_IMPLEMENTS_INTERFACE);
		}
	}

	@Override
	public void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		this.mPresenter = this.createPresenter();
	}

	@Override
	public void onResume() {
		super.onResume();
		this.mScreenActionsListener.onSetCurrentScreen(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		InputMethodManager imm = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.getView().getWindowToken(), 0);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.mScreenActionsListener = null;
	}

	@Override
	public Context getViewContext() {
		return this.getContext();
	}

	@Override
	public void showMessage(int pStringResourceId) {
		Toast.makeText(this.getActivity(), pStringResourceId, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void showConfirmation(int pStringResourceId, UtilsDialog.IOnDialogButtonClickListener pListener) {
		String title = this.getString(R.string.title__dialog);
		String positiveButtonText = this.getString(R.string.btn__ok);
		String negativeButtonText = this.getString(R.string.btn__no);
		String message = this.getString(pStringResourceId);
		UtilsDialog.displayDialog(this.getActivity(), title, message, true, positiveButtonText, negativeButtonText, pListener);
	}

	@Override
	public void showError(int pStringResourceId) {
		String title = this.getString(R.string.title__dialog_error);
		String buttonText = this.getString(R.string.btn__ok);
		String message = this.getString(pStringResourceId);
		UtilsDialog.displayErrorDialog(this.getActivity(), title, message, buttonText);
	}

	public boolean shouldPopBackStack() {
		return false;
	}
	
	protected void addChildToViewGroup(ViewGroup pParent, View pChild, int pPosition) {
		if (pPosition >= 0) {
			pParent.addView(pChild, pPosition);
		} else {
			pParent.addView(pChild);
		}
	}
	
	protected void replaceViewInViewGroup(ViewGroup pParent, View pChild, int pPosition) {
		if (pPosition < 0) {
			pPosition = 0;
		}
		
		int childCount = pParent.getChildCount();
		if (pPosition >= childCount) {
			pPosition = childCount - 1;
		}
		
		pParent.removeViewAt(pPosition);
		pParent.addView(pChild, pPosition);
	}

	protected int getCurrentOrientation() {
		return this.getResources().getConfiguration().orientation;
	}

	protected abstract P createPresenter();

	public interface IOnScreenActionsListener {
		void onSetCurrentScreen(ScreenBase pCurrentScreen);
		void onChangeScreen(ScreenBase pScreen);
		void onReturnActivityResult(int pResultCode, Intent pData);
		void onChangeLanguage(String pLanguage);
		void onBack();
	}
}
