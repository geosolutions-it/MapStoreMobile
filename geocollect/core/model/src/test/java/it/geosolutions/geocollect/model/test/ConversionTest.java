package it.geosolutions.geocollect.model.test;



//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonSyntaxException;


/*
 * GeoSolutions - MapstoreMobile - GeoSpatial Framework on Android based devices
 * Copyright (C) 2014  GeoSolutions (www.geo-solutions.it)
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

/**
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 * Tests convertion for sample json files
 */
public class ConversionTest {
//	@Test
//	public void formConvertionTest(){
//		Gson gson = new GsonBuilder().create();
//		URL url = this.getClass().getResource("/forms/form.json");
//		
//		File f=null;
//		
//		try {
//			f = new File(url.toURI());
//		} catch (URISyntaxException e) {
//			fail();
//		}
//		
//		if (f == null){
//			fail();
//		}
//		try {
//			Form form = gson.fromJson(readFile(f),Form.class);
//			assertNotNull(form);
//			assertNotNull(form.pages);
//			assertTrue(form.pages.size()>0);
//			assertNotNull(form.pages.get(0));
//			assertNotNull(form.pages.get(0).fields);
//			assertNotNull(form.pages.get(0).fields.get(0));
//			
//		} catch (JsonSyntaxException e) {
//			//fail();
//			e.printStackTrace();
//		} catch (IOException e) {
//			//fail();
//			e.printStackTrace();
//		}
//	}
//	@Test
//	public void TemplateConvertionTest(){
//		Gson gson = new GsonBuilder().create();
//		URL url = this.getClass().getResource("/templates/template1.json");
//		
//		File f=null;
//		
//		try {
//			f = new File(url.toURI());
//		} catch (URISyntaxException e) {
//			fail();
//		}
//		
//		if (f == null){
//			fail();
//		}
//		try {
//			MissionTemplate template = gson.fromJson(readFile(f),MissionTemplate.class);
//			assertNotNull(template);
//			assertNotNull(template.config);
//			assertTrue(template.config.size()>0);
//			assertNotNull(template.config.get("myAllowedValues1"));
//			Object o = template.config.get("myAllowedValues1");
//			o.getClass();
//			
//		} catch (JsonSyntaxException e) {
//			e.printStackTrace();
//			fail();
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//			fail();
//			
//		}
//	}/**
//	 * Returns the content of a file
//	 * @param fileName
//	 * @return
//	 * @throws IOException
//	 */
//	private String readFile(File fileName) throws IOException {
//	    BufferedReader br = new BufferedReader(new FileReader(fileName));
//	    try {
//	        StringBuilder sb = new StringBuilder();
//	        String line = br.readLine();
//
//	        while (line != null) {
//	            sb.append(line);
//	            sb.append("\n");
//	            line = br.readLine();
//	        }
//	        return sb.toString();
//	    } finally {
//	        br.close();
//	    }
//	}
}
