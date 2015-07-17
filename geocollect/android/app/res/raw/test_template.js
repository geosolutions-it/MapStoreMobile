{
	"id":"string_id",
    "title": "Test last",
    "schema_seg": {
        "type": "WFS",
        "URL": "http://geocollect.geo-solutions.it/geoserver/it.geosolutions/wfs?",
        "typeName": "it.geosolutions:testlayer",
        "localSourceStore": "testlayer",
        "fields": {
            "codice": "string",
            "uso_agrico": "decimal",
            "wkb_geometry": "PointPropertyType"
        }
    },
    "schema_sop": {
        "localFormStore": "testlayer_sop",
        "fields": {
            "SEQUESTRO": "string",
            "COGNOME_RILEVATORE": "string",
            "GEOMETRY": "PointPropertyType"
        }
    },
    "nameField": "codice",
    "descriptionField": "uso_agrico",
    "priorityField": "uso_agrico",
    "priorityValuesColors": {
        "1": "#cd3636",
        "2": "#dfb56b",
        "3": "#dbe129"
    },
    "orderingField": "codice",
    "preview": {
        "title": "pre view",
        "fields": [
            {
                "type": null,
                "value": "${origin.codice}",
                "label": "Codice",
                "xtype": "label"
            },
            {
                "type": null,
                "value": "${origin.uso_agrico}",
                "label": "${uso_agrico}",
                "xtype": "separatorWithIcon"
            },
            {
                "type": "geoPoint",
                "value": "${origin.wkb_geometry}",
                "fieldId": "GEOMETRY",
                "xtype": "mapViewPoint",
                "attributes": {
                    "editable": true,
                    "disablePan": true,
                    "disableZoom": false,
                    "displayOriginalValue": true,
                    "height": 640,
                    "zomm": 15
                },
                "localize": true,
                "localizeMsg": "Localizza"
            }
        ],
        "actions": [
            {
                "text": "Localizza",
                "name": "Localize",
                "type": "localize",
                "iconCls": "ic_localize",
                "id": 1
            },
            {
                "text":"Centrare",
				"name":"Center",
				"type":"center",
				"iconCls":"ic_center",
                "id": 2
            }
        ]
    },
    "sop_form": {
        "id": 1,
        "name": "test form",
        "pages": [
            {
                "title": "Valori",
                "fields": [
                    {
                        "type": null,
                        "value": "${local.operatore}",
                        "label": "Operatore",
                        "xtype": "label",
                        "fieldId": "COGNOME_RILEVATORE"
                    },
                    {
                        "fieldId": "SEQUESTRO",
                        "type": "string",
                        "label": "Seauestro",
                        "xtype": "textfield",
                        "mandatory": true,
                        "options": [
                            "Si",
                            "No"
                        ]
                    }
                ],
                "attributes": {
                    "message": "scorri a sinistra per continuare"
                },
                "actions": [
                    {
                        "text": "Invia",
                        "type": "send",
                        "name": "send",
                        "iconCls": "ic_send",
                        "attributes": {
                            "url": "http://84.33.2.28/opensdi2-manager/mvc/geocollect/action/store",
                            "mediaurl": "http://84.33.2.28/opensdi2-manager//mvc/geocollect/data",
                            "confirmMessage": "Invia"
                        },
                        "id": 1
                    }
                ]
            },
            {
                "title": "Rilievi fotografici",
                "fields": [
                    {
                        "type": null,
                        "text": "Rilievo",
                        "xtype": "photo"
                    }
                ],
                "actions": [
                    {
                        "text": "Rilievo",
                        "name": "photos",
                        "type": "photo",
                        "iconCls": "ic_camera",
                        "id": 1
                    }
                ]
            },
            {
                "title": "posizione",
                "fields": [
                    {
                        "type": "geoPoint",
                        "value": "${origin.wkb_geometry}",
                        "fieldId": "GEOMETRY",
                        "xtype": "mapViewPoint",
                        "attributes": {
                            "editable": true,
                            "disablePan": true,
                            "disableZoom": true,
                            "displayOriginalValue": false,
                            "height": 640,
                            "zomm": 18
                        },
                        "localize": true,
                        "localizeMsg": "Localize"
                    }
                ],
                "actions": [
                    {
                        "text": "Localize",
                        "name": "Localize",
                        "type": "localize",
                        "iconCls": "ic_localize",
                        "id": 1
                    },
                    {
                        "text":"Centrare",
						"name":"Center",
						"type":"center",
						"iconCls":"ic_center",
                        "id": 2
                    }
                ]
            }
        ]
    },
    "seg_form": {
        "id": 1,
        "name": "segnalazioni",
        "pages": [
            {
                "title": "posizione",
                "fields": [
                    {
                        "type": "geoPoint",
                        "value": "",
                        "fieldId": "wkb_geometry",
                        "xtype": "mapViewPoint",
                        "attributes": {
                            "editable": true,
                            "disablePan": true,
                            "disableZoom": false,
                            "displayOriginalValue": false,
                            "height": 640,
                            "zomm": 14
                        },
                        "center": true,
                        "centerMsg": "Centra"
                    }
                ],
                "actions": [
                    {
                        "text": "Centra",
                        "name": "Center",
                        "type": "center",
                        "iconCls": "ic_center",
                        "id": 1
                    },
                    {
                        "text": "Centra",
                        "name": "Center",
                        "type": "center",
                        "iconCls": "ic_center",
                        "id": 2
                    },
                    {
                        "text": "Centra",
                        "name": "Center",
                        "type": "center",
                        "iconCls": "ic_center",
                        "id": 3
                    }
                ]
            },
            {
                "title": "Rilievo fotografico",
                "fields": [
                    {
                        "type": null,
                        "text": "Foto segnalazione",
                        "xtype": "photo"
                    }
                ],
                "actions": [
                    {
                        "text": "Foto segnalazione",
                        "name": "photos",
                        "type": "photo",
                        "iconCls": "ic_camera",
                        "id": 1
                    }
                ]
            },
            {
                "title": "Valori",
                "fields": [
                    {
                        "type": null,
                        "value": "",
                        "label": "Codice",
                        "xtype": "label",
                        "fieldId": "codice"
                    },
                    {
                        "fieldId": "uso_agrico",
                        "type": "string",
                        "label": "Uso",
                        "xtype": "textfield",
                        "mandatory": true,
                        "options": [
                            "1",
                            "2",
                            "3",
                            "4",
                            "5",
                            "6",
                            "7"
                        ]
                    }
                ],
                "actions": [
                    {
                        "text": "Send",
                        "type": "send",
                        "name": "send",
                        "iconCls": "ic_send",
                        "attributes": {
                            "url": "http://84.33.2.28/opensdi2-manager/mvc/geocollect/action/store",
                            "mediaurl": "http://84.33.2.28/opensdi2-manager//mvc/geocollect/data",
                            "confirmMessage": "Send"
                        },
                        "id": 1
                    }
                ]
            }
        ]
    }
}