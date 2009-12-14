var menu={edits:{},views:{},langs:{}};

function init()
{
/*  var script = document.createElement("script");
  script.src = 'http://openlayers.org/api/OpenLayers.js';
  script.type = "text/javascript";
  document.getElementById("readingData").appendChild(script);*/

  script = document.createElement("script");
  script.src = 'http://www.openstreetmap.org/openlayers/OpenStreetMap.js';
  script.type = "text/javascript";
  document.getElementById("readingData").appendChild(script);
  script = document.createElement("script");
  script.src = 'js/mappin.js';
  script.type = "text/javascript";
  document.getElementById("readingData").appendChild(script);

  for(var i=0;i<llang.length;i++){
      menu.langs[llang[i]]=document.getElementById('lang_'+llang[i]);
  }
  menu.edits={
    potlatch:document.getElementById("potlatch"),
    josm:document.getElementById("josm"),
    josm_w:document.getElementById("josm_w")
  };
  menu.views={
    permalink:document.getElementById("permalink"),
    rssTile:document.getElementById("rssTile"),
    osmlink:document.getElementById("osmlink"),
    osblink:document.getElementById("osblink"),
    geofabrik:document.getElementById("geofabrik")
  };
}

