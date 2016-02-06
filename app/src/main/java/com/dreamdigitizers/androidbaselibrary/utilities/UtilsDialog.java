package com.dreamdigitizers.androidbaselibrary.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import com.dreamdigitizers.androidbaselibrary.R;

import java.util.Calendar;

public class UtilsDialog {
	private static final int MAX_INDETERMINATE = 100;

	private static DatePickerDialog datePickerDialog;
	private static TimePickerDialog timePickerDialog;
	private static ProgressDialog progressDialog;
	
	public static void showDialog(
			final Activity pActivity,
			final String pTitle,
			final String pMessage,
			final boolean pIsTwoButton,
			final String pPositiveButtonText,
			final String pNegativeButtonText,
			final IOnDialogButtonClickListener pListener) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(pActivity);
		alertDialogBuilder.setTitle(pTitle);
		alertDialogBuilder.setMessage(pMessage);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setPositiveButton(pPositiveButtonText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface pDialog, int pWhich) {
				if (pListener != null) {
					pListener.onPositiveButtonClick(
							pActivity,
							pTitle,
							pMessage,
							pIsTwoButton,
							pPositiveButtonText,
							pNegativeButtonText);
				}
			}
		});
		if (pIsTwoButton) {
			alertDialogBuilder.setNegativeButton(pNegativeButtonText, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface pDialog, int pWhich) {
				if (pListener != null) {
					pListener.onNegativeButtonClick(
							pActivity,
							pTitle,
							pMessage,
							pIsTwoButton,
							pPositiveButtonText,
							pNegativeButtonText);
				}
				}
			});
		}
		final AlertDialog alertDialog = alertDialogBuilder.create();
		pActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				alertDialog.show();
			}
		});
	}
	
	public static void showErrorDialog(
			final Activity pActivity,
			final String pTitle,
			final String pMessage,
			final String pButtonText) {
		IOnDialogButtonClickListener dialogButtonClickListener = new IOnDialogButtonClickListener() {
			@Override
			public void onPositiveButtonClick(
					Activity pActivity,
					String pTitle,
					String pMessage,
					boolean pIsTwoButton,
					String pPositiveButtonText,
					String pNegativeButtonText) {
			}

			@Override
			public void onNegativeButtonClick(
					Activity pActivity,
					String pTitle,
					String pMessage,
					boolean pIsTwoButton,
					String pPositiveButtonText,
					String pNegativeButtonText) {
			}
		};
		UtilsDialog.showDialog(
				pActivity,
				pTitle,
				pMessage,
				false,
				pButtonText,
				null,
				dialogButtonClickListener);
	}

	public static void showRetryableErrorDialog(
			final Activity pActivity,
			final String pTitle,
			final String pMessage,
			final String pPositiveButtonText,
			final String pNegativeButtonText,
			final IRetryAction pRetryAction) {
		UtilsDialog.showRetryableErrorDialog(
				pActivity,
				pTitle,
				pMessage,
				pPositiveButtonText,
				pNegativeButtonText,
				false,
				pRetryAction);
	}

	public static void showRetryableErrorDialog(
			final Activity pActivity,
			final String pTitle,
			final String pMessage,
			final String pPositiveButtonText,
			final String pNegativeButtonText,
			final boolean pIsEndActivity,
			final IRetryAction pRetryAction) {
		IOnDialogButtonClickListener dialogButtonClickListener = new IOnDialogButtonClickListener() {
			@Override
			public void onPositiveButtonClick(
					Activity pActivity,
					String pTitle,
					String pMessage,
					boolean pIsTwoButton,
					String pPositiveButtonText,
					String pNegativeButtonText) {
				if (pIsEndActivity) {
					pActivity.finish();
				} else if(pRetryAction != null) {
					pRetryAction.retry();
				}
			}

			@Override
			public void onNegativeButtonClick(
					Activity pActivity,
					String pTitle, String pMessage,
					boolean pIsTwoButton,
					String pPositiveButtonText,
					String pNegativeButtonText) {

			}
		};
		UtilsDialog.showDialog(
				pActivity,
				pTitle,
				pMessage,
				true,
				pPositiveButtonText,
				pNegativeButtonText,
				dialogButtonClickListener);
	}

	public static void showDatePickerDialog(
			final Activity pActivity,
			final String pCancelButtonText,
			final IOnDatePickerDialogEventListener pListener) {
		if (UtilsDialog.datePickerDialog == null) {
			Calendar calendar = Calendar.getInstance();
			UtilsDialog.datePickerDialog = new DatePickerDialog(pActivity,
					new DatePickerDialog.OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker pView, int pYear, int pMonthOfYear, int pDayOfMonth) {
							if (pListener != null) {
								pListener.onDateSet(pYear, pMonthOfYear, pDayOfMonth, pActivity, pCancelButtonText);
							}
							UtilsDialog.hideDatePickerDialog();
						}
					},
					calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DATE));
			UtilsDialog.datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, pCancelButtonText, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface pDialog, int pWhich) {
					if (pListener != null) {
						pListener.onCancel(pActivity, pCancelButtonText);
					}
					UtilsDialog.hideDatePickerDialog();
				}
			});
			UtilsDialog.datePickerDialog.show();
		}
	}

	public static void hideDatePickerDialog() {
		if (UtilsDialog.datePickerDialog != null) {
			UtilsDialog.datePickerDialog.dismiss();
			UtilsDialog.datePickerDialog = null;
		}
	}

	public static void showTimePickerDialog(
			final Activity pActivity,
			final String pCancelButtonText,
			final boolean pIs24HourView,
			final IOnTimePickerDialogEventListener pListener) {
		if (UtilsDialog.timePickerDialog == null) {
			Calendar calendar = Calendar.getInstance();
			UtilsDialog.timePickerDialog = new TimePickerDialog(pActivity,
					new TimePickerDialog.OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker pView, int pHourOfDay, int pMinute) {
							if (pListener != null) {
								pListener.onTimeSet(pHourOfDay, pMinute, pActivity, pCancelButtonText, pIs24HourView);
							}
							UtilsDialog.hideTimePickerDialog();
						}
					},
					calendar.get(Calendar.HOUR_OF_DAY),
					calendar.get(Calendar.MINUTE),
					pIs24HourView);
			UtilsDialog.timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, pCancelButtonText, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface pDialog, int pWhich) {
					if (pListener != null) {
						pListener.onCancel(pActivity, pCancelButtonText, pIs24HourView);
					}
					UtilsDialog.hideTimePickerDialog();
				}
			});
			UtilsDialog.timePickerDialog.show();
		}
	}

	public static void hideTimePickerDialog() {
		if (UtilsDialog.timePickerDialog != null) {
			UtilsDialog.timePickerDialog.dismiss();
			UtilsDialog.timePickerDialog = null;
		}
	}
	
	public static void showProgressDialog(
			final Activity pActivity,
			final int pStyle,
			final String pTitle,
			final String pMessage,
			final String pCancelButtonText,
			final boolean pIsCancelable,
			final boolean pIsCanceledOnTouchOutside,
			final boolean pIndeterminate,
			final IOnProgressDialogCancelButtonClickListener pListener) {
		if (UtilsDialog.progressDialog == null) {
			UtilsDialog.progressDialog = new ProgressDialog(pActivity);
			UtilsDialog.progressDialog.setProgressStyle(pStyle);
			UtilsDialog.progressDialog.setTitle(pTitle);
			UtilsDialog.progressDialog.setMessage(pMessage);
			UtilsDialog.progressDialog.setCancelable(pIsCancelable);
			UtilsDialog.progressDialog.setCanceledOnTouchOutside(pIsCanceledOnTouchOutside);
			UtilsDialog.progressDialog.setIndeterminate(pIndeterminate);
			if (pIsCancelable && pListener != null) {
				UtilsDialog.progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, pCancelButtonText, new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface pDialog, int pWhich) {
						if (pListener != null) {
							pListener.onCancelButtonClick(
									pActivity,
									pStyle,
									pTitle,
									pMessage,
									pCancelButtonText,
									pIsCancelable,
									pIsCanceledOnTouchOutside,
									pIndeterminate);
						}
						UtilsDialog.hideProgressDialog();
				    }
				});
			}
			if (!pIndeterminate) {
				UtilsDialog.progressDialog.setMax(UtilsDialog.MAX_INDETERMINATE);
			}
			UtilsDialog.progressDialog.show();
			UtilsDialog.progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			UtilsDialog.progressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			UtilsDialog.progressDialog.setContentView(R.layout.dialog___progress);

			ProgressBar progressBar = (ProgressBar) UtilsDialog.progressDialog.findViewById(R.id.pgbProgress);
			progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(pActivity, R.color.color_accent), PorterDuff.Mode.SRC_IN);
			Drawable progressDrawable = progressBar.getProgressDrawable();
			if (progressDrawable != null) {
				progressDrawable.setColorFilter(ContextCompat.getColor(pActivity, R.color.color_accent), PorterDuff.Mode.SRC_IN);
			}
		}
	}
	
	public static void hideProgressDialog() {
		if (UtilsDialog.progressDialog != null) {
			UtilsDialog.progressDialog.dismiss();
			UtilsDialog.progressDialog = null;
		}
	}

	public static void showNetworkProgressDialog(final Activity pActivity, final String pTitle, final String pMessage) {
		UtilsDialog.showProgressDialog(
				pActivity,
				ProgressDialog.STYLE_SPINNER,
				pTitle,
				pMessage,
				null,
				false,
				false,
				true,
				null);
	}

	public static void hideNetworkProgressDialog() {
		UtilsDialog.hideProgressDialog();
	}

	public interface IOnDialogButtonClickListener {
		void onPositiveButtonClick(
				final Activity pActivity,
				final String pTitle,
				final String pMessage,
				final boolean pIsTwoButton,
				final String pPositiveButtonText,
				final String pNegativeButtonText);

		void onNegativeButtonClick(
				final Activity pActivity,
				final String pTitle,
				final String pMessage,
				final boolean pIsTwoButton,
				final String pPositiveButtonText,
				final String pNegativeButtonText);
	}

	public interface IRetryAction {
		void retry();
	}

	public interface IOnDatePickerDialogEventListener {
		void onDateSet(final int pYear, final int pMonthOfYear, final int pDayOfMonth, final Activity pActivity, final String pCancelButtonText);
		void onCancel(final Activity pActivity, final String pCancelButtonText);
	}

	public interface IOnTimePickerDialogEventListener {
		void onTimeSet(final int pHourOfDay, final int pMinute, final Activity pActivity, final String pCancelButtonText, final boolean pIs24HourView);
		void onCancel(final Activity pActivity, final String pCancelButtonText, final boolean pIs24HourView);
	}

	public interface IOnProgressDialogCancelButtonClickListener {
		void onCancelButtonClick(
				final Activity pActivity,
				final int pStyle,
				final String pTitle,
				final String pMessage,
				final String pCancelButtonText,
				final boolean pIsCancelable,
				final boolean pIsCanceledOnTouchOutside,
				final boolean pIndeterminate);
	}
}
