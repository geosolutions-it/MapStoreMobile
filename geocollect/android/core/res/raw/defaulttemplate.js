{
	"id":"punti_abbandono",
	"title": "Punti Abbandono",
	"schema_seg":{		
   		"type":"WFS",		
   		"_URL":"http://demo.geo-solutions.it/share/comunege/geocollect/punti_abbandono.geojson",	
   		"URL":"http://geocollect.geo-solutions.it/geoserver/it.geosolutions/wfs?srsName=EPSG:4326&",	
   		"typeName":"it.geosolutions:punti_abbandono",	
   		"localSourceStore":"punti_abbandono",
		"fields":{
			"CODICE":"string",
			"DATA_RILEV":"date",
			"MACROAREA":"string",
			"MICROAREA":"string",
			"CIRCOSCRIZ":"string",
			"MORFOLOGIA":"string",
			"INCLINAZIO":"string",
			"MORFOLOGI1":"string",
			"COPERTURA_":"string",
			"COPERTURA1":"string",
			"USO_AGRICO":"integer",
			"USO_PARCHE":"integer",
			"USO_COMMER":"integer",
			"USO_STRADA":"integer",
			"USO_ABBAND":"string",
			"PRESUNZION":"string",
			"AREA_PRIVA":"string",
			"AREA_PUBBL":"string",
			"ALTRE_CARA":"integer",
			"DISTANZA_U":"integer",
			"DIMENSIONI":"string",
			"RIFIUTI_NO":"string",
			"RIFIUTI_PE":"string",
			"QUANTITA_R":"integer",
			"STATO_FISI":"string",
			"ODORE":"string",
			"MODALITA_S":"string",
			"PERCOLATO":"string",
			"VEGETAZION":"string",
			"STABILITA":"integer",
			"INSEDIAMEN":"string",
			"INSEDIAME1":"string",
			"DISTANZA_C":"string",
			"INSEDIAME2":"string",
			"INSEDIAME3":"string",
			"DISTANZA_P":"string",
			"BOSCATE":"string",
			"BOSCATE_AB":"string",
			"AGRICOLO":"integer",
			"AGRICOLO_A":"string",
			"TORRENTI_R":"string",
			"NOME_TORRE":"string",
			"RISCHIO_ES":"string",
			"RIFIUTI_IN":"string",
			"PROBABILE_":"string",
			"IMPATTO_ES":"string",
			"POZZI_FALD":"string",
			"CRITICITA":"string",
			"IMPATTO_CO":"integer",
			"NOTE":"string",
			"PULIZIA":"string",
			"DISSUASION":"string",
			"VALORE_GRA":"integer",
			"FATTIBILIT":"string",
			"VALORE_FAT":"integer",
			"LATITUDINE":"integer",
			"LONGITUDIN":"integer",
			"GCID":"integer",
			"ID1":"integer",
			"GRAVITA":"string",
			"RISCHIO":"string",
			"VALORE_RIS":"integer",
			"SOCIO_PAES":"string",
			"VALORE_SOC":"integer",
			"GMROTATION":"real"
		},
   	 "orderingField":"CODICE"	
    },
	"schema_sop":{
		"localFormStore":"punti_abbandono_sop",
   	 	"fields":{
   	 		"DATA_SCHEDA":"date",
   	 		"DATA_AGG":"date",
   	 		"MY_ORIG_ID":"string",
   	 		"NOME_RILEVATORE":"string",
   	 		"COGNOME_RILEVATORE":"string",
   	 		"ENTE_RILEVATORE":"string",
   	 		"TIPOLOGIA_SEGNALAZIONE":"string",
   	 		"PROVENIENZA_SEGNALAZIONE":"string",
   	 		"CODICE_DISCARICA":"string",
   	 		"TIPOLOGIA_RIFIUTO":"string",
   	 		"COMUNE":"string",
   	 		"LOCALITA":"string",
   	 		"INDIRIZZO":"string",
   	 		"CIVICO":"string",
   	 		"PRESA_IN_CARICO":"string",
   	 		"EMAIL":"string",
   	 		"RIMOZIONE":"string",
   	 		"SEQUESTRO":"string",
   	 		"RESPONSABILE_ABBANDONO":"string",
   	 		"QUANTITA_PRESUNTA":"string"
   	 	},
		"orderingField":"CODICE"
	},
	"nameField":"CODICE",
	"descriptionField":"NOTE",
	"priorityField":"GRAVITA",
	"priorityValuesColors":{
		"elevata":"#FF0000",
		"media":"#FF9933",
		"scarsa":"#FDF600",
		"area pulita":"#00FF00"
	},
	"preview":{
		"title":"Punto Abbandono",
		"fields":[{
					
					"type":"text",
					"xtype":"separatorWithIcon",
					"value":"${origin.GRAVITA}",
					"label":"${origin.CODICE}"
				},{
					
					"type":"text",
					"xtype":"label",
					"value":"${origin.DATA_RILEV}",
					"label":"Data Rilevazione"
				},{
					
					"type":"text",
					"xtype":"label",
					"value":"${origin.MICROAREA}",
					"label":"Micro Area"
				},{
					
					"type":"text",
					"xtype":"label",
					"value":"${origin.CIRCOSCRIZ}",
					"label":"Circoscrizione"
				},{
					
					"type":"text",
					"xtype":"label",
					"value":"${origin.GRAVITA}",
					"label":"Gravità"
				},{
					
					"type":"text",
					"xtype":"label",
					"value":"${origin.RIFIUTI_NO}",
					"label":"Descrizione"
				},{
					"type":"geoPoint",
					"value":"${origin.the_geom}",
					"xtype":"mapViewPoint",
					"fieldId": "GEOMETRY",
					"attributes":{
						"editable":false,
						"disablePan":true,
						"disableZoom":true,
						"description":"${origin.RIFIUTI_NO}",
						"height":640,
						"displayOriginalValue":true
					}
				}]
	},
	"sop_form": {
		"id": 1,
		"name": "FormSopralluogo",
		"pages":[
			{
				"title": "Scheda",
				"iconCls":"not_supported_yet",
				"attributes":{
					"message":"Scorri a sinistra per continuare"
				},
				"fields":[{
					"fieldId": "DATA_SCHEDA",
					"type":"text",
					"xtype":"datefield",
					"label":"Data Scheda"
				},{
					"fieldId": "DATA_AGG",
					"type":"text",
					"xtype":"datefield",
					"label":"Data aggiornamento scheda"
				},{
				    "fieldId": "MY_ORIG_ID",
				    "type":"text",
				    "xtype":"label",
				    "value":"${origin.GCID}",
				    "label":"Original Mission ID"
				   }]
			},{
				"title": "Rilevatore",
				"iconCls":"not_supported_yet",
				"attributes":{
					"message":"Scorri a sinistra per continuare"
				},
				"fields":[{
					"fieldId": "NOME_RILEVATORE",
					"type":"person",
					"xtype":"textfield",
					"value":"${local.user_name}",
					"label":"Nome Rilevatore",
					"mandatory":"true"
				},{
					"fieldId": "COGNOME_RILEVATORE",
					"type":"person",
					"xtype":"textfield",
					"value":"${local.user_surname}",
					"label":"Cognome Rilevatore",
					"mandatory":"true"
				},{
					"fieldId": "ENTE_RILEVATORE",
					"type":"person",
					"xtype":"textfield",
					"value":"${local.user_organization}",
					"label":"Ente di appartenenza Rilevatore"
				}]
			},{
				"title": "Segnalazione",
				"attributes":{
					"message":"Scorri a sinistra per continuare"
				},
				"fields":[{
					"fieldId": "TIPOLOGIA_SEGNALAZIONE",
					"type":"text",
					"xtype":"spinner",
					"label":"Tipologia Segnalazione e/o Iniziativa",
					"options":["Soggetto Pubblico","Soggetto Privato"]
				},{
					"fieldId": "PROVENIENZA_SEGNALAZIONE",
					"type":"text",
					"xtype":"spinner",
					"label":"Provenienza Segnalazione",
					"options":["Pubblico","Privato"]
				}]
			},{
				"title": "Discarica",
				"fields":[{
					"fieldId": "CODICE_DISCARICA",
					"type": "text",
					"xtype":"label",
					"label": "Codice Discarica",
					"value":"${origin.CODICE}"
				},{
					"fieldId": "TIPOLOGIA_RIFIUTO",
					"type":"text",
					"xtype":"spinner",
					"label":"Tipologia Rifiuto",
					"options":["Pericoloso","Non Pericoloso", "Altro"]
				},{
					"fieldId": "COMUNE",
					"type":"text",
					"xtype":"spinner",
					"label":"Comune",
					"options":[
						"Genova","Sant'Olcese",
						"Campomorone",
						"Serra Riccò",
						"Mignanego",
						"Casella",
						"Mele",
						"Busalla",
						"Savignone",
						"Bogliasco",
						"Fraconalto",
						"Montoggio",
						"Pieve Ligure",
						"Davagna",
						"Bargagli",
						"Ronco Scrivia",
						"Crocefieschi",
						"Masone",
						"Voltaggio",
						"Sori",
						"Valbrevenna",
						"Vobbia",
						"Arenzano",
						"Lumarzo"]
				},{
					"fieldId": "LOCALITA",
					"type":"text",
					"xtype":"text",
					"label":"Località",
					"options":[
							"Acquasanta",
							"Aggio",
							"Begato",
							"Borgano",
							"Burba",
							"Camaldoli",
							"Camposilvano",
							"Cantalupo",
							"Carpenara",
							"Cartagenova",
							"Carupola",
							"Ceresola",
							"Creto",
							"Fiorino",
							"Pino Soprano",
							"Quarto dei Mille",
							"Ronco",
							"Sestri Ponente",
							"Vesim"]
				},{
					"fieldId": "INDIRIZZO",
					"type":"text",
					"xtype":"text",
					"label":"Indirizzo"
					
				},{
					"fieldId": "CIVICO",
					"type":"text",
					"xtype":"textfield",
					"label":"Numero Civico"
					
				}]
			},{
				"title": "Dati Rilevamento",
				"iconCls":"not_supported_yet",
				"fields":[{
					"fieldId": "PRESA_IN_CARICO",
					"type":"text",
					"xtype":"spinner",
					"label":"Presa in Carico",
					"options":["Rilevatore ","Ufficio afferente"]
				},{
					"fieldId": "EMAIL",
					"type":"text",
					"xtype":"checkbox",
					"label":"Email"
					
				},{
					"fieldId": "RIMOZIONE",
					"type":"text",
					"xtype":"spinner",
					"label":"Rimozione",
					"options":["Si ","No","Parziale"]
				},{
					"fieldId": "SEQUESTRO",
					"type":"text",
					"xtype":"spinner",
					"label":"Sequestro",
					"options":["Si ","No"]
				},{
					"fieldId": "RESPONSABILE_ABBANDONO",
					"type":"text",
					"xtype":"spinner",
					"label":"Responsabile Abbandono",
					"options":["noto ","ignoto","in fase di accertamento"]
				},{
					"fieldId": "QUANTITA_PRESUNTA",
					"type":"decimal",
					"xtype":"textfield",
					"label":"Quantità Presunta (mc)"
				}]
			},{
				"title": "Posizione",
				"attributes":{
					"message":"Premi a lungo sul marker per muoverlo."
				},
				"iconCls":"not_supported_yet",
				"fields":[{
					"fieldId": "GEOMETRY",
					"type":"geoPoint",
					"value":"${origin.the_geom}",
					"xtype":"mapViewPoint",
					"attributes":{
						"editable":true,
						"description":"${origin.PROVNAME}"
					}
				}],
				"actions":[
				{
					"id":3,
					"text":"Localizzare",
					"name":"Localize",
					"type":"localize",
					"iconCls":"ic_localize"
				},{
					"id":4,
					"text":"Centrare",
					"name":"Center",
					"type":"center",
					"iconCls":"ic_center"
				}]
			},{
				"title": "Rilievi Fotografici",
				"iconCls":"not_supported_yet",
				"fields":[{
					"xtype":"photo",
					"label":"photo_has_no_label"
				}],
				"actions":[{
					"id":1,
					"text":"Scatta Foto",
					"name":"photos",
					"type":"photo",
					"iconCls":"ic_camera"
				}]
			},{
				"title": "Invio",
				"iconCls":"not_supported_yet",
				"fields":[{
					
					"type":"text",
					"xtype":"separator",
					"label":"Riepilogo"
				},{
					"fieldId": "DATA_AGG",
					"type":"text",
					"xtype":"label",
					"label":"Data aggiornamento scheda"
				},{
					"fieldId": "TIPOLOGIA_RIFIUTO",
					"type":"text",
					"xtype":"label",
					"label":"Tipologia Rifiuto"
				},{
					"fieldId": "QUANTITA_PRESUNTA",
					"type":"decimal",
					"xtype":"label",
					"label":"Quantità Presunta (mc)"
				}],
				   
				"actions":[{
					"id":2,
					"text":"Invia",
					"type":"send",
					"name":"send",
					"iconCls":"ic_send",
					"attributes":{
						"url":"http://geocollect.geo-solutions.it/opensdi2-manager/mvc/geocollect/action/storeSopSequence",
						"mediaurl":"http://geocollect.geo-solutions.it/opensdi2-manager/mvc/geocollect/data",
						"confirmMessage":"Inviare?"
					}
				}]
			}],
			
		"submitURL": "http://sample.com"
	},
	"seg_form": {
		"id": 2,
		"name": "FormSegnalazione",
		"pages":[
			{
				"title": "Scheda",
				"iconCls":"not_supported_yet",
				"attributes":{
					"message":"Scorri a sinistra per continuare"
				},
				"fields":[{
					"fieldId": "DATA_RILEV",
					"type":"text",
					"xtype":"datefield",
					"label":"Data Rilevazione"
				},{
					"fieldId": "CODICE",
					"type":"text",
					"xtype":"text",
					"label":"Codice",
					"mandatory":"true"
				},{
				    "fieldId": "MACROAREA",
				    "type":"text",
				    "xtype":"text",
				    "label":"Macroarea"
				},{
				    "fieldId": "MICROAREA",
				    "type":"text",
				    "xtype":"text",
				    "label":"Microarea"
				}]
			},{
				"title": "Circoscrizione",
				"iconCls":"not_supported_yet",
				"attributes":{
					"message":"Scorri a sinistra per continuare"
				},
				"fields":[{
					"fieldId": "CIRCOSCRIZ",
					"type":"text",
					"xtype":"text",
					"label":"Circoscrizione"
				},{
					"fieldId": "MORFOLOGIA",
					"type":"text",
					"xtype":"spinner",
					"label":"Morfologia",
					"options":["area pianegginate","scarpata","zona valliva"]
				},{
				"fieldId": "MORFOLOGI1",
					"type":"text",
					"xtype":"spinner",
					"label":"Morfologia1",
					"options":["area pianegginate","scarpata","zona valliva"]
				},{
					"fieldId": "INCLINAZIO",
					"type":"text",
					"xtype":"text",
					"value":"${local.user_organization}",
					"label":"Ente di appartenenza Rilevatore"
				}]
			},{
				"title": "Copertura",
				"attributes":{
					"message":"Scorri a sinistra per continuare"
				},
				"fields":[{
					"fieldId": "COPERTURA_",
					"type":"text",
					"xtype":"checkbox",
					"label":"Copertura_"
				},{
					"fieldId": "COPERTURA1",
					"type":"text",
					"xtype":"checkbox",
					"label":"Copertura1"
				},{
					"fieldId": "USO_AGRICO",
					"type":"text",
					"xtype":"checkbox",
					"label":"USO_AGRICO"
				},{
					"fieldId": "USO_PARCHE",
					"type":"text",
					"xtype":"checkbox",
					"label":"USO_PARCHE"
				},{
					"fieldId": "USO_COMMER",
					"type":"text",
					"xtype":"checkbox",
					"label":"USO_COMMER"
				},{
					"fieldId": "USO_STRADA",
					"type":"text",
					"xtype":"checkbox",
					"label":"USO_STRADA"
				},{
					"fieldId": "USO_ABBAND",
					"type":"text",
					"xtype":"checkbox",
					"label":"USO_ABBAND"
				},{
					"fieldId": "PRESUNZION",
					"type":"text",
					"xtype":"checkbox",
					"label":"PRESUNZION"
				}]
			},{
				"title": "Area_Priv",
				"fields":[{
					"fieldId": "AREA_PRIVA",
					"type": "text",
					"xtype":"checkbox",
					"label": "AREA_PRIVA"
				},{
					"fieldId": "AREA_PUBBL",
					"type":"text",
					"xtype":"checkbox",
					"label":"AREA_PUBBL"					
				},{
					"fieldId": "ALTRE_CARA",
					"type":"decimal",
					"xtype":"textfield",
					"label":"ALTRE_CARA"
					
				},{
					"fieldId": "DISTANZA_U",
					"type":"decimal",
					"xtype":"textfield",
					"label":"DISTANZA_U"
				}]
			},{
				"title": "Dimensioni",
				"iconCls":"not_supported_yet",
				"fields":[{
					"fieldId": "DIMENSIONI",
					"type":"text",
					"xtype":"spinner",
					"label":"Dimensioni",
					"options":["Piccole","Medie","Grandi"]
				},{
					"fieldId": "RIFIUTI_NO",
					"type":"text",
					"xtype":"text",
					"label":"RIFIUTI_NO",
					"mandatory":"true"
				},{
					"fieldId": "RIFIUTI_PE",
					"type":"text",
					"xtype":"text",
					"label":"RIFIUTI_PE"
				},{
					"fieldId": "QUANTITA_R",
					"type":"text",
					"xtype":"spinner",
					"label":"QUANTITA_R",
					"options":["0","1","2","3"]
				},{
					"fieldId": "STATO_FISI",
					"type":"text",
					"xtype":"text",
					"label":"STATO_FISI"
				}]
			},{
				"title": "Posizione",
				"attributes":{
					"message":"Premi a lungo sul marker per muoverlo."
				},
				"iconCls":"not_supported_yet",
				"fields":[{
					"fieldId": "GEOMETRY",
					"type":"geoPoint",
					"xtype":"mapViewPoint",
					"attributes":{
						"editable":true,
						"description":"Set a position"
					}
				}]
			},{
				"title": "Odore",
				"iconCls":"not_supported_yet",
				"fields":[{
					"fieldId": "ODORE",
					"type":"text",
					"xtype":"spinner",
					"label":"ODORE",
					"options":["assente","presente"]
				},{
					"fieldId": "MODALITA_S",
					"type":"text",
					"xtype":"text",
					"label":"MODALITA_S"
				},{
					"fieldId": "PERCOLATO",
					"type":"text",
					"xtype":"checkbox",
					"label":"PERCOLATO"
				},{
					"fieldId": "VEGETAZION",
					"type":"text",
					"xtype":"spinner",
					"label":"Vegetazione",
					"options":["assente","presente"]
				},{
					"fieldId": "STABILITA",
					"type":"decimal",
					"xtype":"textfield",
					"label":"Stabilità"
				}]
			},{	
				"title": "Gravita",
				"iconCls":"not_supported_yet",
				"fields":[{
					"fieldId": "GRAVITA",
					"type":"text",
					"xtype":"spinner",
					"label":"GRAVITA",
					"options":["area pulita","media","scarsa","elevata"]
				},{
					"fieldId": "RISCHIO",
					"type":"text",
					"xtype":"spinner",
					"label":"RISCHIO",
					"options":["basso","medio","alto"]
				},{
					"fieldId": "VALORE_RIS",
					"type":"decimal",
					"xtype":"textfield",
					"label":"VALORE_RIS"
				},{
					"fieldId": "SOCIO_PAES",
					"type":"text",
					"xtype":"spinner",
					"label":"SOCIO_PAES",
					"options":["basso","medio","alto"]
				},{
					"fieldId": "VALORE_SOC",
					"type":"decimal",
					"xtype":"textfield",
					"label":"VALORE_SOC"
				},{
					"fieldId": "GMROTATION",
					"type":"real",
					"xtype":"textfield",
					"label":"GMROTATION"
				}],				
				"actions":[{
					"id":2,
					"text":"Invia",
					"type":"send",
					"name":"send",
					"iconCls":"ic_send",
					"attributes":{
						"url":"http://geocollect.geo-solutions.it/opensdi2-manager/mvc/geocollect/action/storeSegSequence",
						"mediaurl":"http://geocollect.geo-solutions.it/opensdi2-manager/mvc/geocollect/data",
						"confirmMessage":"Inviare?"
					}
				}]
			}]
	},
	"config":{
		"myAllowedValues1":["value1","value2","value3"],
		"maxImageSize" : "1000"
	},
	"priorityField":"GRAVITA",
	"priorityValuesColors":{
				"elevata":"#FF0000",
				"media":"#FF9933",
				"scarsa":"#FDF600",
				"area pulita":"#00FF00"
			}
	
}