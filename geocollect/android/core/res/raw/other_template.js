{
	"title": "template1",
	"source":{
		"type":"WFS",
		"URL":"http://demo1.geo-solutions.it/geoserver-enterprise/geosolutions/ows",
		"typeName":"geosolutions:cities"
	},
	"preview":{
		"title":"Punto Abbandono",
		"fields":[{
					
					"type":"text",
					"xtype":"separator",
					"value":"{origin.NAME}",
					"label":"Name"
				},{
					
					"type":"text",
					"xtype":"label",
					"value":"{origin.CNTRYNAME}",
					"label":"Country"
				},{
					
					"type":"text",
					"xtype":"label",
					"value":"{origin.PROVNAME}",
					"label":"Province"
				},{
					"type":"geoPoint",
					"value":"{origin.the_geom}",
					"xtype":"mapViewPoint",
					"attributes":{
						"description":"{origin.PROVNAME}",
						"height":400
					}
				}]
	},
	"nameField":"NAME",
	"descriptionField":"CNTRYNAME",
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
					"label":"Nome Rileavatore"
				},{
					"fieldId": "COGNOME_RILEVATORE",
					"type":"person",
					"xtype":"textfield",
					"label":"Cognome Rileavatore"
				},{
					"fieldId": "ENTE_RILEVATORE",
					"type":"person",
					"xtype":"textfield",
					"label":"Ente di appartenenza Rilevatore"
				}]
			},{
				"title": "Dati Discarica",
				"fields":[{
					"type":"text",
					"xtype":"separator",
					"label":"Segnalazione"
					},{
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
				},{
					"type":"text",
					"xtype":"separator",
					"label":"Discarica"
				},{
					"fieldId": "CODICE_DISCARICA",
					"type": "text",
					"xtype":"label",
					"label": "Codice Discarica",
					"value":"CODICE_DISCARICA"
				},{
					"fieldId": "TIPOLOGIA_RIFIUTO",
					"type":"text",
					"xtype":"spinner",
					"label":"Provenienza Segnalazione",
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
					"label":"Provenienza Segnalazione",
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
					"label":"Provenienza Segnalazione",
					"options":["Si ","No","Parziale"]
				},{
					"fieldId": "RESPONSABILE_ABBANDONO",
					"type":"text",
					"xtype":"spinner",
					"label":"Provenienza Segnalazione",
					"options":["noto ","ignoto","in fase di accertamento"]
				},{
					"fieldId": "QUANTITA_PRESUNTA",
					"type":"decimal",
					"xtype":"textfield",
					"label":"Quantità Presunta"
				}]
			},{
				"title": "Posizione",
				"iconCls":"not_supported_yet",
				"fields":[{
					"type":"geoPoint",
					"value":"{origin.the_geom}",
					"xtype":"mapViewPoint",
					"attributes":{
						"editable":true,
						"description":"{origin.PROVNAME}"
					}
				}]
			},{
				"title": "Rilievi Fotografici",
				"iconCls":"not_supported_yet",
				"fields":[]
			},{
				"title": "Invio",
				"iconCls":"not_supported_yet",
				"fields":[]
			}],
			
		"submitURL": "http://sample.com"
	},
	"config":{
		"myAllowedValues1":["value1","value2","value3"]
	}
}