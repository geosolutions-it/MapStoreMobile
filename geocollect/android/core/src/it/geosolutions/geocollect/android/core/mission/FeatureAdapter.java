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


import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter for a row of layers from a <ArrayList> of <FeatureInfoQueryResult>
 * The implementation show legend and name.
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class FeatureAdapter extends ArrayAdapter<MissionFeature> {
    
    int resourceId = R.layout.mission_resource_row;
    
    private MissionTemplate template;
    
    private MissionFeatureFilter filter;
    
    private ArrayList<String> uploadableIDs;
    
    /**
     * The constructor gets the resource (id of the layout for the element)
     * 
     * @param context
     * @param resource
     */
    public FeatureAdapter(Context context, int resource) {
        super(context, resource);
        this.resourceId = resource;
    }
    
    /**
     * Create the adapter and populate it with a list of <FeatureInfoQueryResult>
     * 
     * @param context a context --> for testing this must not be a SherlockFragment
     * @param feature_info_layer_list_row
     * @param feature_layer_name
     */
    public FeatureAdapter(Context context, int resource, MissionTemplate template) {
        super(context, resource);
        this.resourceId = resource;
        this.template = template;
        
        updateUploadableIDs(context);
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    
        // assign the view we are converting to a local variable
        View v = convertView;
    
        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(resourceId, null);
        }
    
        /*
         * Recall that the variable position is sent in as an argument to this
         * method. The variable simply refers to the position of the current object
         * in the list. (The ArrayAdapter iterates through the list we sent it)
         * Therefore, i refers to the current Item object.
         */
        MissionFeature result = getItem(position);
    
        if (result != null) {
    
            // This is how you obtain a reference to the TextViews.
            // These TextViews are created in the XML files we defined.
            // TODO use ViewHolder
            // display name
            if(this.template !=null && this.template.nameField != null){
                TextView name = (TextView) v.findViewById(R.id.mission_resource_name);
                if (name != null && result.properties != null && result.properties.containsKey(this.template.nameField)) {
                    Object prop =result.properties.get(this.template.nameField);
                    if(prop!=null){
                        name.setText(prop.toString());
                    }else{
                        name.setText("");
                    }
                    
                }
            }
            if(this.template !=null && this.template.descriptionField != null){
                TextView desc = (TextView) v.findViewById(R.id.mission_resource_description);
                if (desc != null && result.properties != null && result.properties.containsKey(this.template.descriptionField)) {
                    Object prop =result.properties.get(this.template.descriptionField);
                    if(prop!=null){
                        desc.setText(prop.toString());
                    }else{
                        desc.setText("");
                    }
                    
                }
            }
            
            ImageView editingIcon = (ImageView) v.findViewById(R.id.mission_resource_edit_icon);
            if(editingIcon != null){
    
                if(result.editing){
                    editingIcon.setVisibility(View.VISIBLE);
                    //it was edited, uploadble is a subset of it, look if it is "done"
                    if(uploadableIDs != null && uploadableIDs.contains(MissionUtils.getFeatureGCID(result))){
                        //this one is uploadable, give it a hook
                        editingIcon.setImageResource(R.drawable.ic_navigation_accept_light);
                    }
                }else{
                    editingIcon.setVisibility(View.GONE);
                }
    
            }
            
            ImageView priorityIcon = (ImageView) v.findViewById(R.id.mission_resource_priority_icon);
            if ( priorityIcon != null && priorityIcon.getDrawable() != null ){
                
                // Get the icon and tweak the color
                Drawable d = priorityIcon.getDrawable();
                
                if ( result.displayColor != null ){
                    try{
                        d.mutate().setColorFilter(Color.parseColor(result.displayColor), PorterDuff.Mode.SRC_ATOP);
                    }catch(IllegalArgumentException iae){
                        Log.e("FeatureAdapter", "A feature has an incorrect color value" );
                    }
                }else{
                    d.mutate().clearColorFilter();
                }
    
            }
    
        }
    
        // the view must be returned to our activity
        return v;
    
    }
    
    public void setTemplate(MissionTemplate t){
        
        this.template = t;
        
        updateUploadableIDs(getContext());
    }
    
    private void updateUploadableIDs(Context context) {
        
        HashMap<String,ArrayList<String>> uploadables = PersistenceUtils.loadUploadables(context);
        
        if(uploadables.containsKey(this.template.schema_sop.localFormStore)){
            uploadableIDs = uploadables.get(this.template.schema_sop.localFormStore); 
        }
        
    }
    @Override
    public void clear() {
        super.clear();
        updateUploadableIDs(getContext());
    }
    
    
    @Override
    public Filter getFilter() {
        
        if(filter == null){
            filter = new MissionFeatureFilter();
        }
        return filter;
    }
    
    /**
     * Class to filter missions according to search/filter queries
     * @author Robert Oehler
     *
     */
    private class MissionFeatureFilter extends Filter{
        
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            
              ArrayList<MissionFeature> filtered = (ArrayList<MissionFeature>) results.values;
    //          Log.d(FeatureAdapter.class.getSimpleName(), "results size "+ filtered.size() + " after filtering : "+constraint);
              //apply results to the adapter if necessary
              if(filtered != null){              
                  clear();
                  if(Build.VERSION.SDK_INT > 10){
                      addAll(filtered);
                  }else{                  
                      for(MissionFeature f : filtered){
                          add(f);
                      }
                  }
    
                  notifyDataSetChanged();
              }
            
        }
        /**
         * performs the filtering
         * @param constraint to constraint to apply
         */
        @SuppressLint("DefaultLocale")
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
    
            FilterResults filterResults = new FilterResults();   
            ArrayList<MissionFeature> tempList=new ArrayList<MissionFeature>();
            
    
            int count = getCount();
            for(int i = 0; i < count; i++){
    
                MissionFeature item = getItem(i);
                if(constraint != null && constraint.length() > 0) {
                    //if constraint, filter
                    final String title = (String) item.properties.get(template.nameField);
                    final String desc =  (String) item.properties.get(template.descriptionField);
    
                    if(title != null && desc != null && constraint.length() > 0){
    
                        //do filtering here however you want missions to be filtered
                        //for now it adds the item if title or desc are within the search query --> constraint
                        if(title.toLowerCase().contains(constraint.toString().toLowerCase()) ||
                                desc.toLowerCase().contains(constraint.toString().toLowerCase())){
    
                            tempList.add(item);
                        }
                    }        
                }else{
                    //if constraint is null or empty ("") add all
                    tempList.add(item);
                }
    
            }
            //convert to FilterResults objects
            filterResults.values = tempList;
            filterResults.count = tempList.size();
            return filterResults;
        }
    };
}

