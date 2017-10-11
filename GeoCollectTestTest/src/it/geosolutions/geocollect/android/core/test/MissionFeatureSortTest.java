package it.geosolutions.geocollect.android.core.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.utils.MissionFeatureSorter;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import android.test.InstrumentationTestCase;

public class MissionFeatureSortTest extends InstrumentationTestCase {

	/**
	 * tests the sorting of MissionFeatures by their editing and new flags
	 */
	public void testEditSort(){
		
		final String orderingField = "CODICE";
		final boolean byDistance = false;
		final boolean preferEdited = true;
		
		//1. test prefer edited
		
		final MissionFeatureSorter editSorter = new MissionFeatureSorter(orderingField, preferEdited, byDistance, false);
		final ArrayList<MissionFeature> features = new ArrayList<MissionFeature>();
		
		features.add(createMissionFeature(false, false, orderingField, "abc"));
		features.add(createMissionFeature(false, false, orderingField, "aabc"));
		features.add(createMissionFeature(true,  false, orderingField, "new"));
		features.add(createMissionFeature(false, false, orderingField, "def"));
		features.add(createMissionFeature(false, false, orderingField, "xyz"));
		features.add(createMissionFeature(false,  true, orderingField, "edit"));
		features.add(createMissionFeature(true,   true, orderingField, "both"));
		
		Collections.sort(features, editSorter);
		
		//assert "both", "new" and "editing" are up, afterwards the other items in alphabetical order 
		assertTrue(features.get(0).properties.get(orderingField).equals("both"));
		assertTrue(features.get(1).properties.get(orderingField).equals("new"));
		assertTrue(features.get(2).properties.get(orderingField).equals("edit"));
		assertTrue(features.get(3).properties.get(orderingField).equals("aabc"));
		assertTrue(features.get(4).properties.get(orderingField).equals("abc"));
		assertTrue(features.get(5).properties.get(orderingField).equals("def"));
		assertTrue(features.get(6).properties.get(orderingField).equals("xyz"));
		
		//2. test reversed
		
		final MissionFeatureSorter reversedSorter = new MissionFeatureSorter(orderingField, preferEdited, byDistance, true);
		Collections.sort(features, reversedSorter);
		//this is how reversion is actually done in SQLiteCascadeFeatureLoader
		Collections.reverse(features);
		
		//assert "both", "new" and "editing" are still up, only all others afterwards are reversed
		assertTrue(features.get(0).properties.get(orderingField).equals("both"));
		assertTrue(features.get(1).properties.get(orderingField).equals("new"));
		assertTrue(features.get(2).properties.get(orderingField).equals("edit"));
		assertTrue(features.get(3).properties.get(orderingField).equals("xyz"));
		assertTrue(features.get(4).properties.get(orderingField).equals("def"));
		assertTrue(features.get(5).properties.get(orderingField).equals("abc"));
		assertTrue(features.get(6).properties.get(orderingField).equals("aabc"));
		
		//3. do not prefer edited 
		
		final MissionFeatureSorter doNotPreferSorter = new MissionFeatureSorter(orderingField, false, byDistance, false);
		Collections.sort(features, doNotPreferSorter);
		
		//assert alphabetical order
		assertTrue(features.get(0).properties.get(orderingField).equals("aabc"));
		assertTrue(features.get(1).properties.get(orderingField).equals("abc"));
		assertTrue(features.get(2).properties.get(orderingField).equals("both"));
		assertTrue(features.get(3).properties.get(orderingField).equals("def"));
		assertTrue(features.get(4).properties.get(orderingField).equals("edit"));
		assertTrue(features.get(5).properties.get(orderingField).equals("new"));
		assertTrue(features.get(6).properties.get(orderingField).equals("xyz"));
		
	}
	
	private MissionFeature createMissionFeature(final boolean newFlag, final boolean editing, final String orderingField, final String name){
		
		MissionFeature mf = new MissionFeature();
		mf.properties = new HashMap<String, Object>();
		if(newFlag){			
			mf.typeName = "typeName" + MissionTemplate.NEW_NOTICE_SUFFIX;
		}else{
			mf.typeName = "typeName";
		}
		mf.properties.put(orderingField, name);
		mf.editing = editing;
		
		return mf;
	}
}
