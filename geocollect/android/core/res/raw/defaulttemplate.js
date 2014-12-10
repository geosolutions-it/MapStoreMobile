{
	"id":"punti_abbandono",
	"title": "Punti Abbandono",
	"source":{
		"type":"WFS",
		"URL":"http://demo.geo-solutions.it/share/comunege/geocollect/punti_abbandono.geojson",
		"typeName":"geosolutions:punti_abbandono",
		"localSourceStore":"punti_abbandono",
		"localFormStore":"rilevamenti_effettuati",
		"dataTypes":{
			"CODICE":"string",
			"DATA_RILEV":"string",
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
			"ID":"integer",
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
	"nameField":"CODICE",
	"descriptionField":"RIFIUTI_NO",
	"form": {
		"id": 1,
		"name": "SampleForm",
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
				    "xtype":"text",
				    "value":"${origin.ID}",
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
						"url":"http://84.33.2.28/opensdi2-manager/mvc/geocollect/action/store",
						"mediaurl":"http://84.33.2.28/opensdi2-manager/mvc/geocollect/data",
						"confirmMessage":"Inviare?"
					}
				}]
			}],
			
		"submitURL": "http://sample.com"
	},
	"config":{
		"myAllowedValues1":["value1","value2","value3"]
	},
	"priorityField":"GRAVITA",
	"priorityValuesColors":{
				"elevata":"#FF0000",
				"media":"#FF9933",
				"scarsa":"#FDF600",
				"area pulita":"#00FF00"
			}
	
}