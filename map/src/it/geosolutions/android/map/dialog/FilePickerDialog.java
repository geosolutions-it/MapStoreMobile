package it.geosolutions.android.map.dialog;

import it.geosolutions.android.map.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 		   class which shows a dialog letting the user select a certain file
 * 
 * @author Robert Oehler 
 */
public class FilePickerDialog {
	
	protected static AlertDialog currentAlert;
	
	protected File mDirectory;
	protected ArrayList<File> mFiles;
	protected FilePickerListAdapter mAdapter;
	protected boolean mShowHiddenFiles = false;
	protected String[] acceptedFileExtensions;

	public FilePickerDialog(final Context context,final String message,final String directory,final String extension,final FilePickCallback filePickCallback) {

		LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View mapSelectionView = inflator.inflate(R.layout.filepick_selection, null);
		final ListView lv = (ListView) mapSelectionView.findViewById(R.id.waypoint_listview);

		// Initialize the extensions array to allow any file extensions
		acceptedFileExtensions = new String[] { extension };
		
		// Set initial directory
		mDirectory = new File(directory);

		// Initialize the ArrayList
		mFiles = new ArrayList<File>();

		// Set the ListAdapter
		mAdapter = new FilePickerListAdapter(context, mFiles);

		refreshFilesList();
		
		final AlertDialog.Builder builder = new AlertDialog.Builder((Activity)context);
		
		final TextView message_tv = (TextView) mapSelectionView.findViewById(R.id.message_tv);
		message_tv.setText(message);

		final View emptyView = inflator.inflate(R.layout.file_picker_empty, null);
		lv.setEmptyView(emptyView);
		lv.setAdapter(mAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				
				File newFile = (File)parent.getItemAtPosition(position);

				if(newFile.isFile()) {
					String fileName = newFile.toString();
					int dot = fileName.lastIndexOf('.');

					if(fileName.substring(dot + 1).equals(extension)){

						//picked
						filePickCallback.filePicked(newFile);						
						currentAlert.dismiss();
					}else{
						Toast.makeText(context,"Invalid file, please select an ."+extension+" file", Toast.LENGTH_SHORT).show();	
					}
				}
			}
		});
		
		final Button cancelButton = (Button) mapSelectionView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				currentAlert.dismiss();
			}
			
		});
		

		builder.setIcon(R.drawable.ic_launcher)
		.setTitle("MapStoreMobile")
		.setMessage(message)
		.setView(mapSelectionView);

		currentAlert = builder.create();
		currentAlert.show();

	}

	
	/**
	 * Updates the list view to the current directory
	 */
	protected void refreshFilesList() {
		// Clear the files ArrayList
		mFiles.clear();

		// Set the extension file filter
		ExtensionFilenameFilter filter = new ExtensionFilenameFilter(acceptedFileExtensions);

		// Get the files in the directory
		File[] files = mDirectory.listFiles(filter);
		if(files != null && files.length > 0) {
			for(File f : files) {
				if(f.isHidden() && !mShowHiddenFiles) {
					// Don't add the file
					continue;
				}

				// Add the file the ArrayAdapter
				mFiles.add(f);
			}

			Collections.sort(mFiles, new FileComparator());
		}
		mAdapter.notifyDataSetChanged();
	}
	/**
	 * sets the properties of the view of a file in the listview
	 */
	private class FilePickerListAdapter extends ArrayAdapter<File> {

		private List<File> mObjects;

		public FilePickerListAdapter(Context context, List<File> objects) {
			super(context, R.layout.file_picker_list_item, android.R.id.text1, objects);
			mObjects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View row = null;

			if(convertView == null) { 
				LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.file_picker_list_item, parent, false);
			} else {
				row = convertView;
			}

			File object = mObjects.get(position);

			//ImageView imageView = (ImageView)row.findViewById(R.id.file_picker_image);
			TextView textView = (TextView)row.findViewById(R.id.file_picker_text);
			// Set single line
			textView.setSingleLine(true);

			textView.setText(object.getName());


			return row;
		}

	}

	private class FileComparator implements Comparator<File> {
	    @Override
	    public int compare(File f1, File f2) {
	    	if(f1 == f2) {
	    		return 0;
	    	}
	    	if(f1.isDirectory() && f2.isFile()) {
	        	// Show directories above files
	        	return -1;
	        }
	    	if(f1.isFile() && f2.isDirectory()) {
	        	// Show files below directories
	        	return 1;
	        }
	    	// Sort the directories alphabetically
	        return f1.getName().compareToIgnoreCase(f2.getName());
	    }
	}

	private class ExtensionFilenameFilter implements FilenameFilter {
		private String[] mExtensions;

		public ExtensionFilenameFilter(String[] extensions) {
			super();
			mExtensions = extensions;
		}

		@Override
		public boolean accept(File dir, String filename) {

			if(mExtensions != null && mExtensions.length > 0) {
				for(int i = 0; i < mExtensions.length; i++) {
					if(filename.endsWith(mExtensions[i])) {
						// The filename ends with the extension
						return true;
					}
				}
				// The filename did not match any of the extensions
				return false;
			}
			// No extensions has been set. Accept all file extensions.
			return true;
		}
	}
	
	public interface FilePickCallback {
		
		public void filePicked(final File file);

	}

}
