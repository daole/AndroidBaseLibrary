package com.dreamdigitizers.androidbaselibrary.views.classes.fragments.screens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.dreamdigitizers.androidbaselibrary.R;
import com.dreamdigitizers.androidbaselibrary.presenters.interfaces.IPresenterBase;
import com.dreamdigitizers.androidbaselibrary.utilities.UtilsDialog;
import com.dreamdigitizers.androidbaselibrary.views.classes.activities.ActivityBase;
import com.dreamdigitizers.androidbaselibrary.views.classes.fragments.FragmentBase;
import com.dreamdigitizers.androidbaselibrary.views.interfaces.IViewBase;

public abstract class ScreenBase<P extends IPresenterBase> extends FragmentBase implements IViewBase {
	private static final String ERROR_MESSAGE__CONTEXT_NOT_IMPLEMENTS_INTERFACE = "Activity must implement IOnScreenActionsListener.";

	protected boolean mIsRecoverable;
	protected P mPresenter;
	protected IOnScreenActionsListener mScreenActionsListener;

	@Override
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
		if (this.shouldSetThisScreenAsCurrentScreen()) {
			this.mScreenActionsListener.onSetCurrentScreen(this);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		InputMethodManager inputMethodManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(this.getView().getWindowToken(), 0);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.mScreenActionsListener = null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(this.mPresenter != null) {
			this.mPresenter.dispose();
		}
	}

	@Override
	public Context getViewContext() {
		return this.getContext();
	}

	@Override
	public Object getViewSystemService(String pName) {
		return this.getContext().getSystemService(pName);
	}

	@Override
	public void showMessage(int pMessageResourceId) {
		this.showMessage(this.getString(pMessageResourceId));
	}

	@Override
	public void showMessage(String pMessage) {
		Toast.makeText(this.getActivity(), pMessage, Toast.LENGTH_LONG).show();
	}

	@Override
	public void showMessage(int pMessageResourceIdId, int pActionResourceId, View.OnClickListener pActionListener) {
		this.mScreenActionsListener.onShowSnackbar(Snackbar.LENGTH_LONG, pMessageResourceIdId, pActionResourceId, pActionListener);
	}

	@Override
	public void showMessage(String pMessage, String pAction, View.OnClickListener pActionListener) {
		this.mScreenActionsListener.onShowSnackbar(Snackbar.LENGTH_LONG, pMessage, pAction, pActionListener);
	}

	@Override
	public void showConfirmation(int pMessageResourceId, UtilsDialog.IOnDialogButtonClickListener pListener) {
		this.showConfirmation(this.getString(pMessageResourceId), pListener);
	}

	@Override
	public void showConfirmation(String pMessage, UtilsDialog.IOnDialogButtonClickListener pListener) {
		String title = this.getString(R.string.title__dialog);
		String positiveButtonText = this.getString(R.string.btn__ok);
		String negativeButtonText = this.getString(R.string.btn__no);
		UtilsDialog.showDialog(this.getActivity(), title, pMessage, true, positiveButtonText, negativeButtonText, pListener);
	}

	@Override
	public void showError(int pMessageResourceId) {
		this.showError(this.getString(pMessageResourceId));
	}

	@Override
	public void showError(String pMessage) {
		String title = this.getString(R.string.title__dialog_error);
		String buttonText = this.getString(R.string.btn__ok);
		UtilsDialog.showErrorDialog(this.getActivity(), title, pMessage, buttonText);
	}

	@Override
	public void showRetryableError(int pMessageResourceId, boolean pIsEndActivity, UtilsDialog.IRetryAction pRetryAction) {
		this.showRetryableError(this.getString(pMessageResourceId), pIsEndActivity, pRetryAction);
	}

	@Override
	public void showRetryableError(String pMessage, boolean pIsEndActivity, UtilsDialog.IRetryAction pRetryAction) {
		String title = this.getString(R.string.title__dialog_error);
		String positiveButtonText = this.getString(R.string.btn__ok);
		String negativeButtonText = this.getString(R.string.btn__no);
		UtilsDialog.showRetryableErrorDialog(
				this.getActivity(),
				title,
				pMessage,
				positiveButtonText,
				negativeButtonText,
				pIsEndActivity,
				pRetryAction);
	}

	@Override
	public void showNetworkProgress() {
		UtilsDialog.showNetworkProgressDialog(this.getActivity(), this.getString(R.string.title__dialog), this.getString(R.string.message__loading___));
	}

	@Override
	public void hideNetworkProgress() {
		UtilsDialog.hideNetworkProgressDialog();
	}

	public boolean shouldPopBackStack() {
		return false;
	}

	public void onShow() {
	}

	public void onHide() {
	}

	protected boolean shouldSetThisScreenAsCurrentScreen() {
		return true;
	}

	protected void changeActivityAndFinish(Class pTargetActivityClass) {
		Intent intent = new Intent(this.getContext(), pTargetActivityClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		((ActivityBase) this.getActivity()).changeActivity(intent, true);
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

	public abstract int getScreenId();
	protected abstract P createPresenter();

	public interface IOnScreenActionsListener {
		void onShowSnackbar(int pLength, int pMessageResourceIdId, int pActionResourceId, View.OnClickListener pActionListener);
		void onShowSnackbar(int pLength, String pMessage, String pAction, View.OnClickListener pActionListener);
		void onSetCurrentScreen(ScreenBase pCurrentScreen);
		void onChangeScreen(ScreenBase pScreen);
		void onReturnActivityResult(int pResultCode, Intent pData);
		void onChangeLanguage(String pLanguage);
		void onBack();
	}
}
