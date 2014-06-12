{
	"id":"punti_accumulo",
	"title": "Punti Abbandono",
	"source":{
		"type":"WFS",
		"URL":"http://demo.geo-solutions.it/share/comunege/geocollect/punti_abbandono.geojson",
		"typeName":"geosolutions:punti_abbandono",
		"storeLocally":"localTable",
		"dataTypes":{
			"CODICE":"String",
			"DATA_RILEV":"String",
			"MACROAREA":"String",
			"MICROAREA":"String",
			"CIRCOSCRIZ":"String",
			"MORFOLOGIA":"String",
			"INCLINAZIO":"String",
			"MORFOLOGI1":"String",
			"COPERTURA_":"String",
			"COPERTURA1":"String",
			"USO_AGRICO":"Integer",
			"USO_PARCHE":"Integer",
			"USO_COMMER":"Integer",
			"USO_STRADA":"Integer",
			"USO_ABBAND":"String",
			"PRESUNZION":"String",
			"AREA_PRIVA":"String",
			"AREA_PUBBL":"String",
			"ALTRE_CARA":"Integer",
			"DISTANZA_U":"Integer",
			"DIMENSIONI":"String",
			"RIFIUTI_NO":"String",
			"RIFIUTI_PE":"String",
			"QUANTITA_R":"Integer",
			"STATO_FISI":"String",
			"ODORE":"String",
			"MODALITA_S":"String",
			"PERCOLATO":"String",
			"VEGETAZION":"String",
			"STABILITA":"Integer",
			"INSEDIAMEN":"String",
			"INSEDIAME1":"String",
			"DISTANZA_C":"String",
			"INSEDIAME2":"String",
			"INSEDIAME3":"String",
			"DISTANZA_P":"String",
			"BOSCATE":"String",
			"BOSCATE_AB":"String",
			"AGRICOLO":"Integer",
			"AGRICOLO_A":"String",
			"TORRENTI_R":"String",
			"NOME_TORRE":"String",
			"RISCHIO_ES":"String",
			"RIFIUTI_IN":"String",
			"PROBABILE_":"String",
			"IMPATTO_ES":"String",
			"POZZI_FALD":"String",
			"CRITICITA":"String",
			"IMPATTO_CO":"Integer",
			"NOTE":"String",
			"PULIZIA":"String",
			"DISSUASION":"String",
			"VALORE_GRA":"Integer",
			"FATTIBILIT":"String",
			"VALORE_FAT":"Integer",
			"LATITUDINE":"Integer",
			"LONGITUDIN":"Integer",
			"ID":"Integer",
			"ID1":"Integer",
			"GRAVITA":"String",
			"RISCHIO":"String",
			"VALORE_RIS":"Integer",
			"SOCIO_PAES":"String",
			"VALORE_SOC":"Integer",
			"GMROTATION":"Real"
		}
	},
	"preview":{
		"title":"Punto Abbandono",
		"fields":[{
					
					"type":"text",
					"xtype":"separator",
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
					"value":"${origin.QUANTITA_R}",
					"label":"Quantità"
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
				}]
			},{
				"title": "Rilevatore",
				"iconCls":"not_supported_yet",
				"fields":[{
					"fieldId": "NOME_RILEVATORE",
					"type":"person",
					"xtype":"textfield",
					"label":"Nome Rilevatore"
				},{
					"fieldId": "COGNOME_RILEVATORE",
					"type":"person",
					"xtype":"textfield",
					"label":"Cognome Rilevatore"
				},{
					"fieldId": "ENTE_RILEVATORE",
					"type":"person",
					"xtype":"textfield",
					"label":"Ente di appartenenza Rilevatore"
				}]
			},{
				"title": "Segnalazione",
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
				fields:[{
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
					"label":"Quantità Presunta"
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
					
					"type":"text",
					"xtype":"separator",
					"label":"TO BE IMPLEMENTED"
				}],
				"actions":[{
					"id":1,
					"text":"Scatta Foto",
					"name":"photos",
					"type":"photo",
					iconCls:"ic_camera"
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
					"label":"Quantità Presunta"
				}],
				"actions":[{
					"id":2,
					"text":"Invia",
					"type":"send",
					"name":"send",
					"iconCls":"ic_send"
				}]
			}],
			
		"submitURL": "http://sample.com"
	},
	"config":{
		"myAllowedValues1":["value1","value2","value3"]
	}
}