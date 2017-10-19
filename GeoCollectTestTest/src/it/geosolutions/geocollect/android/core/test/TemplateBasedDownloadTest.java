package it.geosolutions.geocollect.android.core.test;

import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.google.gson.Gson;

public class TemplateBasedDownloadTest extends android.test.AndroidTestCase {
	
	/**
	 * tests if the template is correctly parsed containing background urls
	 * and if the state which is returned by checkTemplateForBackgroundData
	 * corresponds to the availability of basedir/bg.map, the default background file
	 */
	public void testTemplateParsing(){
				
		InputStream inputStream = getContext().getResources().openRawResource(R.raw.defaulttemplate);

		final Gson gson = new Gson();
		final BufferedReader reader =new BufferedReader(new InputStreamReader(inputStream));

		final MissionTemplate defaultTemplate = gson.fromJson(reader, MissionTemplate.class);
		
		HashMap<String,Integer> urls = MissionUtils.getContentUrlsAndFileAmountForTemplate(defaultTemplate);
		
		assertTrue(urls.size() > 0);
		
		final String mount  = MapFilesProvider.getEnvironmentDirPath(getContext());
		final String baseDir= MapFilesProvider.getBaseDir();
		
		final boolean bgExists = MissionUtils.checkTemplateForRasterBackgroundData(getContext(), defaultTemplate);
		
		final File bgFile = new File(mount + baseDir+ "/bg.map");
		
		assertTrue(bgFile.exists() && bgExists);

		
	}
}
