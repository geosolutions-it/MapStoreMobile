package yuku.ambilwarna;

import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

public class AmbilWarnaDialog {
        public interface OnAmbilWarnaListener {
                void onCancel(AmbilWarnaDialog dialog);
                void onOk(AmbilWarnaDialog dialog, int color);
        }

        final AlertDialog dialog;
        final OnAmbilWarnaListener listener;
        final View viewHue;
        final AmbilWarnaKotak viewSatVal;
        final ImageView viewCursor;
        final View viewOldColor;
        final View viewNewColor;
        final ImageView viewTarget;
        final ViewGroup viewContainer;
        final float[] currentColorHsv = new float[3];
        final View viewRGB;
        final Context context;
        final Integer Color_RGB;

        /**
         * create an AmbilWarnaDialog. call this only from OnCreateDialog() or from a background thread.
         * 
         * @param context
         *            current context
         * @param color
         *            current color
         * @param listener
         *            an OnAmbilWarnaListener, allowing you to get back error or
         */
        public AmbilWarnaDialog(final Context context, int color, OnAmbilWarnaListener listener) {
                this.listener = listener;
                this.context = context;
                Color.colorToHSV(color, currentColorHsv);
                Color_RGB = null;

                final View view = LayoutInflater.from(context).inflate(R.layout.ambilwarna_dialog, null);
                viewRGB = view.findViewById(R.id.ambilwarna_rgb);
                viewHue = view.findViewById(R.id.ambilwarna_viewHue);
                viewSatVal = (AmbilWarnaKotak) view.findViewById(R.id.ambilwarna_viewSatBri);
                viewCursor = (ImageView) view.findViewById(R.id.ambilwarna_cursor);
                viewOldColor = view.findViewById(R.id.ambilwarna_warnaLama);
                viewNewColor = view.findViewById(R.id.ambilwarna_warnaBaru);
                viewTarget = (ImageView) view.findViewById(R.id.ambilwarna_target);
                viewContainer = (ViewGroup) view.findViewById(R.id.ambilwarna_viewContainer);
                viewSatVal.setHue(getHue());
                viewOldColor.setBackgroundColor(color);
                viewNewColor.setBackgroundColor(color);

                viewHue.setOnTouchListener(new View.OnTouchListener() {
                        @Override public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_MOVE
                                                || event.getAction() == MotionEvent.ACTION_DOWN
                                                || event.getAction() == MotionEvent.ACTION_UP) {

                                        float y = event.getY();
                                        if (y < 0.f) y = 0.f;
                                        if (y > viewHue.getMeasuredHeight()) y = viewHue.getMeasuredHeight() - 0.001f; // to avoid looping from end to start.
                                        float hue = 360.f - 360.f / viewHue.getMeasuredHeight() * y;
                                        if (hue == 360.f) hue = 0.f;
                                        setHue(hue);

                                        // update view
                                        viewSatVal.setHue(getHue());
                                        moveCursor();
                                        viewNewColor.setBackgroundColor(getColor());

                                        return true;
                                }
                                return false;
                        }
                });
                viewSatVal.setOnTouchListener(new View.OnTouchListener() {
                        @Override public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_MOVE
                                                || event.getAction() == MotionEvent.ACTION_DOWN
                                                || event.getAction() == MotionEvent.ACTION_UP) {

                                        float x = event.getX(); // touch event are in dp units.
                                        float y = event.getY();

                                        if (x < 0.f) x = 0.f;
                                        if (x > viewSatVal.getMeasuredWidth()) x = viewSatVal.getMeasuredWidth();
                                        if (y < 0.f) y = 0.f;
                                        if (y > viewSatVal.getMeasuredHeight()) y = viewSatVal.getMeasuredHeight();

                                        setSat(1.f / viewSatVal.getMeasuredWidth() * x);
                                        setVal(1.f - (1.f / viewSatVal.getMeasuredHeight() * y));

                                        // update view
                                        moveTarget();
                                        viewNewColor.setBackgroundColor(getColor());

                                        return true;
                                }
                                return false;
                        }
                });

                dialog = new AlertDialog.Builder(context)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {
                                        if (AmbilWarnaDialog.this.listener != null) {
                                                AmbilWarnaDialog.this.listener.onOk(AmbilWarnaDialog.this, getColor());
                                        }
                                }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {
                                        if (AmbilWarnaDialog.this.listener != null) {
                                                AmbilWarnaDialog.this.listener.onCancel(AmbilWarnaDialog.this);
                                        }
                                }
                        })
                        .setOnCancelListener(new OnCancelListener() {
                                // if back button is used, call back our listener.
                                @Override public void onCancel(DialogInterface paramDialogInterface) {
                                        if (AmbilWarnaDialog.this.listener != null) {
                                                AmbilWarnaDialog.this.listener.onCancel(AmbilWarnaDialog.this);
                                        }

                                }
                        })
                        .create();
                // kill all padding from the dialog window
                dialog.setView(view, 0, 0, 0, 0);

                // move cursor & target on first draw
                ViewTreeObserver vto = view.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override public void onGlobalLayout() {
                                moveCursor();
                                moveTarget();
                                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                });
        }

        protected void moveCursor() {
                float y = viewHue.getMeasuredHeight() - (getHue() * viewHue.getMeasuredHeight() / 360.f);
                if (y == viewHue.getMeasuredHeight()) y = 0.f;
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewCursor.getLayoutParams();
                layoutParams.leftMargin = (int) (viewHue.getLeft() - Math.floor(viewCursor.getMeasuredWidth() / 2) - viewContainer.getPaddingLeft());
                ;
                layoutParams.topMargin = (int) (viewHue.getTop() + y - Math.floor(viewCursor.getMeasuredHeight() / 2) - viewContainer.getPaddingTop());
                ;
                viewCursor.setLayoutParams(layoutParams);
        }

        protected void moveTarget() {
                float x = getSat() * viewSatVal.getMeasuredWidth();
                float y = (1.f - getVal()) * viewSatVal.getMeasuredHeight();
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewTarget.getLayoutParams();
                layoutParams.leftMargin = (int) (viewSatVal.getLeft() + x - Math.floor(viewTarget.getMeasuredWidth() / 2) - viewContainer.getPaddingLeft());
                layoutParams.topMargin = (int) (viewSatVal.getTop() + y - Math.floor(viewTarget.getMeasuredHeight() / 2) - viewContainer.getPaddingTop());
                viewTarget.setLayoutParams(layoutParams);
        }

        private int getColor(){
                Integer rgb_code = getRGBcode();
                if(rgb_code !=null)        //Check if an rgb code has been inserted        
                        return rgb_code;
                return 
                        Color.HSVToColor(currentColorHsv);
        }

        private float getHue() {
                return currentColorHsv[0];
        }

        private float getSat() {
                return currentColorHsv[1];
        }

        private float getVal() {
                return currentColorHsv[2];
        }

        private void setHue(float hue) {
                currentColorHsv[0] = hue;
        }

        private void setSat(float sat) {
                currentColorHsv[1] = sat;
        }

        private void setVal(float val) {
                currentColorHsv[2] = val;
        }

        public void show() {
                dialog.show();
        }

        public AlertDialog getDialog() {
                return dialog;
        }
        
        public void fillVector(int position, String value){
                
        }
        
        /**
         * Method that return rgb code that has been inserted otherwise return null
         * @return Integer
         */
        public Integer getRGBcode() {
                String[] RGB_Code = new String[3];
                String compair = "-";
                
                //Get value of RGB if present                
                RGB_Code[0] = new String(getRGBvalue("R"));
                RGB_Code[1] = new String(getRGBvalue("G"));
                RGB_Code[2] = new String(getRGBvalue("B"));
            
                if(!compair.equals(RGB_Code[0]) && !compair.equals(RGB_Code[1]) && !compair.equals(RGB_Code[2]))
                        return  Color.rgb(Integer.parseInt(RGB_Code[0]),Integer.parseInt(RGB_Code[1]), Integer.parseInt(RGB_Code[2]));
                return null;                
        }
        
        /**
         * This method check for every box if a piece of an rgb code has been inserted and return it to calling,
         * otherwise, if no code has been inserted, return character "-".
         * @param which
         * @return String
         */
        public String getRGBvalue(String which){
                final EditText rgb_value;
                if(which.equals("R")){
                        rgb_value = (EditText)viewRGB.findViewById(R.id.value_rgb_R);
                        addListner(rgb_value);
        
                        if(rgb_value.length()!=0) //Check if the rgb edit text is not empty
                                return rgb_value.getText().toString();
                        else
                                return "-"; //Return a single character to indicate that the selected edit text is empty
                }
                
                else if(which.equals("G")){
                        rgb_value = (EditText)viewRGB.findViewById(R.id.value_rgb_G);
                        addListner(rgb_value);
                        
                        if(rgb_value.length()!=0)
                                return rgb_value.getText().toString();
                        else
                                return "-";                
                        }
                else if(which.equals("B")){
                        rgb_value = (EditText)viewRGB.findViewById(R.id.value_rgb_B);
                        addListner(rgb_value);

                        if(rgb_value.length()!=0)
                                return rgb_value.getText().toString();
                        else
                                return "-";                
                        }
                
                return null;
        }
        
        /**
         * Add a OnEditorActionListener to edit text to capture text insert by user.
         * @param rgbEditText edit text from which user inserted rgb code of color desired.
         */
        public void addListner(final EditText rgbEditText){
                rgbEditText.setOnEditorActionListener(new OnEditorActionListener(){

                        @Override
                        public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                                rgbEditText.setText(rgbEditText.getText().toString());                                
                                return false;
                        }                   
                });
        }
}