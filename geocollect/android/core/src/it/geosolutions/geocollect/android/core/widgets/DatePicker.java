package it.geosolutions.geocollect.android.core.widgets;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.R;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
/**
 * Date Picker that seems like the Google Calendar's one
 * 
 * **Note**: Min and Max date works only with versions of android sdk >10 
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
@SuppressLint("NewApi")
public class DatePicker extends EditText implements DatePickerDialog.OnDateSetListener {
	public interface OnDateSetListener {
		public void onDateSet(DatePicker view, int year, int month, int day);
	}
	
	protected int year;
	protected int month;
	protected int day;
	protected long maxDate = -1;
	protected long minDate = -1;
	protected OnDateSetListener onDateSetListener = null;
	protected java.text.DateFormat dateFormat;
	
	/**
	 * get the DateSetListener
	 * @return OnDateSetListener
	 */
	public OnDateSetListener getOnDateSetListener() {
		return onDateSetListener;
	}

	/**
	 * set the OnDateSetListener
	 * @param onDateSetListener
	 */
	public void setOnDateSetListener(OnDateSetListener onDateSetListener) {
		this.onDateSetListener = onDateSetListener;
	}
	
	/**
	 * Constructor for the date picker
	 * @param context
	 * @param attrs
	 */
	public DatePicker(Context context, AttributeSet attrs) {
		//spinner look
		super(context, attrs,android.R.attr.spinnerStyle);
		
		dateFormat = DateFormat.getMediumDateFormat(getContext());
		
		setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
		setFocusable(false);
		setToday();
	}
	
	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
		updateText();
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
		updateText();
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
		updateText();
	}
	
	public void setDate(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
		
		updateText();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if((event.getAction() == MotionEvent.ACTION_DOWN) && (event.getAction() != MotionEvent.ACTION_POINTER_DOWN))
			showDatePicker();
		
		return super.onTouchEvent(event);
	}
	
	public java.text.DateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(java.text.DateFormat dateFormat) {
		this.dateFormat = dateFormat;
		updateText();
	}
	
	public void setMaxDate(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day, 23, 59, 59);
		this.maxDate = c.getTimeInMillis();
	}

	public void setMinDate(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day, 0, 0, 0);
		this.minDate = c.getTimeInMillis();
	}
	
	public void setToday() {
		Calendar c = Calendar.getInstance();
		setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
	}

	protected void showDatePicker() {
		DatePickerDialog datePickerDialog;
		
		datePickerDialog = new DatePickerDialog(
				getContext(),
				this,
				getYear(),
				getMonth(),
				getDay());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (this.maxDate != -1) {
				datePickerDialog.getDatePicker().setMaxDate(maxDate);
			}
			if (this.minDate != -1) {
				datePickerDialog.getDatePicker().setMinDate(minDate);
			}
		} else {
		    Log.w("DatePicker", "API Level < 11 so not restricting date range...");
		}
		datePickerDialog.show();
	}

	@Override
	public void onDateSet(android.widget.DatePicker view, int year,
			int month, int day) {
		setDate(year, month, day);
		clearFocus();
		
		if(onDateSetListener != null)
			onDateSetListener.onDateSet(this, year, month, day);
	}
	
	public void updateText() {
		Calendar cal = new GregorianCalendar(getYear(), getMonth(), getDay());
		setText(dateFormat.format(cal.getTime()));
	}
	

}