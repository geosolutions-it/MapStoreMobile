/*
 * GeoSolutions GeoCollect - Digital field mapping on Android based devices
 * Copyright (C) 2013 - 2015  GeoSolutions (www.geo-solutions.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geocollect.android.core.mission;

import java.util.ArrayList;
import java.util.HashMap;

import it.geosolutions.geocollect.android.app.BuildConfig;
import it.geosolutions.geocollect.android.app.R;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter for MissionFeatures "created missions"
 *
 */
public class CreatedMissionAdapter extends ArrayAdapter<MissionFeature>{
    
    /**
     * TAG for logging
     */
    public static String TAG = CreatedMissionAdapter.class.getSimpleName();

    private int resourceId;
    
    private String mTableName;
    
    private MissionTemplate template;
    
    private ArrayList<String> uploadableIDs;
    
    public CreatedMissionAdapter(Context context, int resource, String tableName,  MissionTemplate template) {
        super(context, resource);
        
        this.resourceId = resource;
        
        this.mTableName = tableName;
        
        this.template = template;
        
        updateUploadableIDs(context);
    }
    
    @Override
    public void clear() {
        super.clear();
        updateUploadableIDs(getContext());
    }

    
    private void updateUploadableIDs(Context context) {
        
        HashMap<String,ArrayList<String>> uploadables = PersistenceUtils.loadUploadables(context);
        if(uploadables.containsKey(mTableName)){
            uploadableIDs = uploadables.get(mTableName); 
            if(BuildConfig.DEBUG){
                Log.i(TAG, "uploadables for "+mTableName+" :\n"+uploadableIDs.toString());
            }
        }
        
        
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        // assign the view we are converting to a local variable
        View v = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(resourceId, null);
        }

        /*
         * Recall that the variable position is sent in as an argument to this
         * method. The variable simply refers to the position of the current object
         * in the list. (The ArrayAdapter iterates through the list we sent it)
         * Therefore, i refers to the current Item object.
         */
        MissionFeature mission = getItem(position);

        if (mission != null) {

            // display name
            if(this.template !=null && this.template.nameField != null){
                TextView name = (TextView) v.findViewById(R.id.mission_resource_name);
                if (name != null && mission.properties != null && mission.properties.containsKey(this.template.nameField)) {
                    Object prop =mission.properties.get(this.template.nameField);
                    if(prop!=null){
                        name.setText(prop.toString());
                    }else{
                        name.setText("");
                    }
    
                }
            }

            //display description
            if(this.template !=null && this.template.descriptionField != null){
                TextView desc = (TextView) v.findViewById(R.id.mission_resource_description);
                if (desc != null && mission.properties != null && mission.properties.containsKey(this.template.descriptionField)) {
                    Object prop =mission.properties.get(this.template.descriptionField);
                    if(prop!=null){
                        desc.setText(prop.toString());
                    }else{
                        desc.setText("");
                    }
    
                }
            }
            
            ImageView priorityIcon = (ImageView) v.findViewById(R.id.mission_resource_priority_icon);
            if ( priorityIcon != null && priorityIcon.getDrawable() != null ){
                
                // Get the icon and tweak the color
                Drawable d = priorityIcon.getDrawable();
                
                if ( mission.displayColor != null ){
                    try{
                        d.mutate().setColorFilter(Color.parseColor(mission.displayColor), PorterDuff.Mode.SRC_ATOP);
                    }catch(IllegalArgumentException iae){
                        if(BuildConfig.DEBUG){
                            Log.e(TAG, "A feature has an incorrect color value" );
                        }
                    }
                }else{
                    d.mutate().clearColorFilter();
                }

            }
            
            //for created mission, this icon is only visible if a created missionfeature is "uploadable"
            ImageView editingIcon = (ImageView) v.findViewById(R.id.mission_resource_edit_icon);
            if(editingIcon != null){
                if(uploadableIDs != null && uploadableIDs.contains(mission.id)){
                    editingIcon.setVisibility(View.VISIBLE);
                    //this one is uploadable, give it a hook
                    editingIcon.setImageResource(R.drawable.ic_navigation_accept_light);
                }else{
                    editingIcon.setVisibility(View.GONE);
                }
            }
            

        }
        return v;

    }
    
}
