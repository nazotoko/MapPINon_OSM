/*
 * Copyright 2008, 2009  Xavier Le Bourdon, Christoph Böhme, Mitja Kleider, Shun N. Watanabe
 *
 * This file is derived from a part of 
 * * Openstreetbugs (http://openstreetbugs.schokokeks.org/ ).
 *
 * Openstreetbugs is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with Openstreetbugs.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This file implements the client of MapPIN'on OSM (http://mappin.hp2.jp/ ).
 * 
 */
/* configures (directory must end with '/' ) */
/** base path */
var domain="http://"+document.domain; /* "http://mappin.hp2.jp"; */
var server_path = "/";/* you shoud add absorute path . */

/** relative path */
var img_path = "icons/";

/** relative path */
var data_path ="data/photo";

/** valuables of openlayers' object */
var map = null;
var layer = null;
var vectorLayer=null;
var permalink = null;
var icons={};

/* caches for MapPIN'on OSM */
var photos = {length:0};/* hash object to recode the loaded photo markers */
var popup_id=null;
var new_point_feature=null;

function init2()
{
  init_map();
  refresh();
  var regex = new RegExp("[\\?&]id=([^&#]*)");
  var result = regex.exec(window.location.href);
  if(result != null){
        popup_id=result[1];
  }
}

function init_map(){
    map = new OpenLayers.Map('map', {
        controls: [
            new OpenLayers.Control.Navigation(),
            new OpenLayers.Control.PanZoomBar(),
            new OpenLayers.Control.ScaleLine(),
            new OpenLayers.Control.MousePosition({numDigits:6}),
            new OpenLayers.Control.LayerSwitcher(),
            click=new OpenLayers.Control.Click(),
            new OpenLayers.Control.Attribution()
        ],
        maxResolution: 156543.0339,
        numZoomLevels: 20,
        units: 'm',
        //      projection: new OpenLayers.Projection("EPSG:900913"),
        displayProjection: new OpenLayers.Projection("EPSG:4326")
    });
    layer = new OpenLayers.Layer.Markers("Photos",{wrapDateLine: true});
    layer.setOpacity(0.7);
    vectorLayer=new OpenLayers.Layer.Vector("line",{
        styleMap: new OpenLayers.StyleMap({
            strokeColor: "#666666",
            strokeWidth: 1,
            fillColor: "#a0a000",
            fillOpacity: 0.4
        })
    });
    map.addLayers([new OpenLayers.Layer.OSM.Mapnik("Mapnik",{wrapDateLine: true,
            attribution:'Mapnik under <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA 2.0</a> by (C) <a href="http://www.openstreetmap.org">OpenStreetMap</a> contributors'}),
        new OpenLayers.Layer.OSM.CycleMap("CycleMap",{wrapDateLine: true,
            attribution:'CycleMap under <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA 2.0</a> by (C) <a href="http://www.openstreetmap.org">OpenStreetMap</a> contributors'}),
        new OpenLayers.Layer.OSM.Osmarender("Osmarender",{wrapDateLine: true,
            attribution:'Osmarender under <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA 2.0</a> by (C) <a href="http://www.openstreetmap.org">OpenStreetMap</a> contributors'}),
        new OpenLayers.Layer.OSM( "Relief",
            "http://maps-for-free.com/layer/relief/z${z}/row${y}/${z}_${x}-${y}.jpg",
            {numZoomLevels:12,wrapDateLine: true,
             attribution:'<a href="http://maps-for-free.com/">Relief map</a> under <a href="http://en.wikipedia.org/wiki/GNU_Free_Documentation_License">GFDL ver. 1.2</a> by (C) Hans Braxmeier'}),
        new OpenLayers.Layer.OSM( "Contour",
        "http://www.heywhatsthat.com/bin/contour_tiles.cgi?x=${x}&y=${y}&zoom=${z}&interval=25&color=ff0000",{
            isBaseLayer:false,
            visibility: false,
            wrapDateLine: true,
            attribution:'<a href="http://www.heywhatsthat.com/">The contour layer</a> by (C) 2007 Michael Kosowsky'
        }),
        vectorLayer,
        layer
    ]);

    map.setCenter(new OpenLayers.LonLat(0, 0).transform(
        new OpenLayers.Projection("EPSG:4326"),
        map.getProjectionObject()),
        2);
    document.getElementById("map_OpenLayers_Container").style.cursor = "crosshair";

    icon_size = new OpenLayers.Size(12, 12);
    icon_offset = new OpenLayers.Pixel(-icon_size.w/2, -icon_size.h/2);
    icons={
        0:new OpenLayers.Icon(img_path+'blue.png', icon_size, icon_offset),
        1:new OpenLayers.Icon(img_path+'yellow.png', icon_size, icon_offset),
        2:new OpenLayers.Icon(img_path+'red.png', icon_size, icon_offset),
        4:new OpenLayers.Icon(img_path+'new.png', icon_size, icon_offset)
    };
    map.events.register('moveend', map, refresh);
    click.activate();
    map.addControl(permalink=new OpenLayers.Control.Permalink());
}


/* Strip leading and trailing whitespace from str. 
 */
function strip(str){
	return str.replace(/^\s+|\s+$/g, "");
}

/* Save value in a session cookie named "name".
 */
function set_cookie(name, value){
  var expires = (new Date((new Date()).getTime() + 604800000)).toGMTString(); // one week from now
  document.cookie = name+"="+escape(value)+";expires="+expires+";";
}

/* Retrieve the value of cookie "name".
 */
function get_cookie(name){
  if (document.cookie){
    var cookies = document.cookie.split(";");
    for (var i in cookies){
	c = cookies[i].split("=");
	if (strip(c[0]) == name) return unescape(strip(c[1]));
    }
  }
  return null;
}

/* These functions do some coordinate transformations
 */
function y2lat(a) { return 360/Math.PI  * Math.atan(Math.exp(a / 20037508.34 *Math.PI )) - 90; }
function lat2y(a) { return 20037508.34/Math.PI * Math.log(Math.tan(Math.PI*(0.25+a/360))); }
function x2lon(a) { return a * 180 / 20037508.34; }
function lon2x(a) { return a * 20037508.34 / 180; }
function base36(value,digit){
var moduro,i;
var ret="";
for(i=0;i<digit;i++){
 moduro=value%36;
 ret+="0123456789abcdefghijklmnopqrstuvwxyz".substr(moduro,1);
 value=Math.floor(value/36);
}
return ret;
}

/*
 * Html contents of the popups displayed by openstreetbug
 */

/* Html markup for popups showing photo.
 */
function popup_open_photo(photo){
    var i=0;
    var text='<h1>'+photo.title+'</h1>';
    text+='<div style="position:absolute;right:2px;top:6px" ><a target="_blank" title="'+message.title_reload+'" href="registration.php?reload='+photo.id+'"><img alt="reload" src="icons/reload.png" width="16" height="16" /></a></div>';
    text+='<table><tr><td class="thumb">'
    if(photo.thumb){
    if(photo.link){text+='<a target="_blank" title="'+message.title_link+'" href="'+photo.link+'">';}
    text+='<img src="'+photo.thumb+'" alt="'+message.thumbnail+'" />';
    if(photo.link){text+='</a>';}
    } else {
        text+=message.no_thumb;
    }
    text+='</td><td>';
    text+='<ul class="description">';
    text+='<li>'+message.lat+': '+photo.lat+'</li>';
    text+='<li>'+message.lon+': '+photo.lon+'</li>';
    if(photo.alt){
        text+='<li>'+message.alt+': '+photo.alt+'</li>';
    }
    if(photo.av){
        text+='<li>'+message.av+': '+(photo.av*2)+'</li>';
    }

    if(photo.node){
        for(i=0;i<photo.node.length;i++){
            text+='<li>osm:node=<a href="http://www.openstreetmap.org/browse/node/'+photo.node[i]+'">'+photo.node[i]+'</a></li>';
        }
    }
    if(photo.way){
        for(i=0;i<photo.way.length;i++){
            text+='<li>osm:way=<a href="http://www.openstreetmap.org/browse/way/'+photo.way[i]+'">'+photo.way[i]+'</a></li>';
        }
    }
    text+='</ul></td></tr></table><ul class="commands">';
    if(photo.link) text+='<li><a target="_blank" title="'+message.title_link+'" href="'+photo.link+'">'+message.action_link+'</a></li>';
    if(photo.original){
        text+='<li><a target="_blank" title="'+message.title_original+'" href="'+photo.original+'">'+message.action_original+'</a></li>';
    } else if(photo.l){
        text+='<li><a target="_blank" title="'+message.title_large+'" href="'+photo.l+'">'+message.action_large+'</a></li>';
    }
    if(photo.rss) text+= '<li><a target="_blank" title="'+message.title_rss+'" href="'+photo.rss+'">'+message.action_rss+'</a></li>';
    text+='<li><a href="javascript:embedBox.embed('+photo.id+')" title="'+message.title_embed+'" >'+message.action_embed+'</a></li>';
    return text+'</ul>';
}
var embedBox={
    embed: function(id){
        this.id=id;
        var script = document.createElement("script");
        script.src = 'js/embed.js';
        script.type = "text/javascript";
        document.getElementById("readingData").appendChild(script);
    }
}

function popup_new_point(lon,lat){
  var text='<h1>'+message['here_is']+'</h1>';
  text+='<ul class="description"><li>'+message.lat+': '+lat.toFixed(6)+'</li><li>'+message.lon+":"+lon.toFixed(6)+"</li></ul>"
            +'<p>'+message['try_to_tag']+'<br/><input size="25" value="mappin:at=';
lon=Math.round((lon+180)*1000000);
text+=base36(lon,5);
lon=Math.floor(lon/60466176);
lat=Math.round((lat+90)*1000000)*6+lon;
text+=base36(lat,6);
return text+'"/></p>';
}


/*
 * AJAX functions
 */

/* Request points from the server.
 */
var last_request={x:null,y:null};
function make_url(x,y){
  if(last_request.x!=x || last_request.y!=y) {
    url = server_path+data_path+((x>0)?'+'+x:'-'+(-x))+((y>0)?'+'+y:'-'+(-y))+".js";
    var script = document.createElement("script");
    script.src = url;
    script.type = "text/javascript";
    document.getElementById("readingData").innerHTML = '<a href="'+url+'">'+url+'</a>';
    document.getElementById("readingData").appendChild(script);
    last_request.x=x;last_request.y=y;
  }
}

/* This function is called from the scripts that are returned 
 * on make_url calls.
 */
function AJAXI(photos_i){
  for(id in photos_i){
    if (!photos[id]){
      var photo_i=photos_i[id];
      photos[id]=create_feature({
        id: id,
        lat: photo_i.la,
        lon: photo_i.lo,
        alt: photo_i.al,
        dir: photo_i.di,
        av: photo_i.av,
        title: photo_i.ti,
        thumb: photo_i.th,
        link: photo_i.li,
        original: photo_i.o,
        l: photo_i.l,
        state: photo_i.s,
        node: photo_i.n,
        way: photo_i.w,
        rss: photo_i.r
      });
      photos.length++;
    }
  }
  if(popup_id){
    var a=photos[popup_id].marker.events;
    a.triggerEvent("mouseover");
    a.triggerEvent("click");
    popup_id=null;
  }
  document.getElementById("numberOfPhoto").innerHTML = photos.length;
}
/* This function creates a feature and adds a corresponding
 * marker to the map.
 */
function create_feature(photo){
  var feature = new OpenLayers.Feature(layer, new OpenLayers.LonLat(lon2x(photo.lon), lat2y(photo.lat)), {icon: icons[photo.state].clone()});
  feature.photo=photo;
  var marker = feature.createMarker();
  marker.events.register("click", feature, marker_click);
  marker.events.register("mouseover", feature, marker_mouseover);
  marker.events.register("mouseout", feature, marker_mouseout);

  layer.addMarker(marker);
  return feature;
}
function create_view(photo,lonlat){
  var geometry=new OpenLayers.Geometry.LinearRing();
  geometry.addPoint(new OpenLayers.Geometry.Point(lonlat.lon,lonlat.lat));
  var dx=100*Math.sin((photo.dir+photo.av)*Math.PI/180);
  var dy=100*Math.cos((photo.dir+photo.av)*Math.PI/180);
  geometry.addPoint(new OpenLayers.Geometry.Point(lonlat.lon+dx,lonlat.lat+dy));
  if(photo.av>45){
    dx=100*Math.sin((photo.dir+45)*Math.PI/180);
    dy=100*Math.cos((photo.dir+45)*Math.PI/180);
    geometry.addPoint(new OpenLayers.Geometry.Point(lonlat.lon+dx,lonlat.lat+dy));
  }
  dx=100*Math.sin((photo.dir)*Math.PI/180);
  dy=100*Math.cos((photo.dir)*Math.PI/180);
  geometry.addPoint(new OpenLayers.Geometry.Point(lonlat.lon+dx,lonlat.lat+dy));
  if(photo.av>45){
    dx=100*Math.sin((photo.dir-45)*Math.PI/180);
    dy=100*Math.cos((photo.dir-45)*Math.PI/180);
    geometry.addPoint(new OpenLayers.Geometry.Point(lonlat.lon+dx,lonlat.lat+dy));
  }
  dx=100*Math.sin((photo.dir-photo.av)*Math.PI/180);
  dy=100*Math.cos((photo.dir-photo.av)*Math.PI/180);
  geometry.addPoint(new OpenLayers.Geometry.Point(lonlat.lon+dx,lonlat.lat+dy));
  return new OpenLayers.Feature.Vector(geometry);
}
/*
 * Control to handle clicks on the map
 */
OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
    CLASS_NAME: "OpenLayers.Control.Click",
    initialize: function() {
        OpenLayers.Control.prototype.initialize.apply(this, arguments);
    },
    destroy: function() {
        if (this.handler)
            this.handler.destroy();
        this.handler = null;

        OpenLayers.Control.prototype.destroy.apply(this, arguments);
    },

    draw: function() {
        this.handler = new OpenLayers.Handler.Click(this, {
            'click': this.click
        }, {
            'single': true,
            'double': false,
            'pixelTolerance': 0,
            'stopSingle': false,
            'stopDouble': false
        });
    },
/****** events called from Openlayers ************/
/** Map events */
    click: function(ev){
        var lonlat=map.getLonLatFromPixel(ev.xy);
        if(!new_point_feature){
            new_point_feature = new OpenLayers.Feature(layer, lonlat, {icon: icons[4].clone()});
            new_point_feature.photo={id:0};
        } else {
            map.removePopup(new_point_feature.popup);
            new_point_feature.popup.destroy();
            layer.removeMarker(new_point_feature.marker);
            new_point_feature.marker.destroy();
            new_point_feature.data.icon=icons[4].clone();
            new_point_feature.lonlat=lonlat;
        }
        var marker=new_point_feature.createMarker();
        marker.events.register("click", new_point_feature, marker_click);
        marker.events.register("mouseover", new_point_feature, marker_mouseover);
        marker.events.register("mouseout", new_point_feature, marker_mouseout);
        layer.addMarker(marker);
        new_point_feature.popup =new OpenLayers.Popup.FramedCloud(new_point_feature.id+'_popup',
            lonlat,
            new_point_feature.data.popupSize,
            popup_new_point(x2lon(lonlat.lon),y2lat(lonlat.lat)),
            marker.icon,
            true,
            popup_close);
        new_point_feature.popup.panMapIfOutOfView=false;
        new_point_feature.popup.feature=new_point_feature;
        map.addPopup(new_point_feature.popup);
        new_point_feature.popuped=true;
        document.getElementById("numberOfPopuping").innerHTML = map.popups.length;
        OpenLayers.Event.stop(ev);
    }
});

/* map moveover events */
function refresh(){
  var params = permalink.createParams();
  var lon=params.lon;
  var lat=params.lat;
  var x=Math.round(lon*20);
  var y=Math.round(lat*20);
  var bounds=map.getExtent();
  var left=x2lon(bounds.left);
  var right=x2lon(bounds.right);
  var bottom=y2lat(bounds.bottom);
  var top=y2lat(bounds.top);
  var lonlat = '?lon='+params.lon+'&lat='+params.lat+'&zoom='+params.zoom;
  var layers = '&layers='+params.layers;
  var lefttop='?left='+left+'&right='+right+'&top='+top+'&bottom='+bottom;
  
  if (params.zoom > 12) {
    url=make_url(x,y);
    menu.edits.potlatch.href = 'http://www.openstreetmap.org/edit'+lonlat;
    menu.edits.josm.href = 'http://localhost:8111/load_and_zoom'+lefttop;
    menu.edits.josm_w.href = 'josm/josm.jnlp.php'+lefttop;
    for(key in menu.edits){
      menu.edits[key].title=message['title_'+key];
    }
  } else {
    for(key in menu.edits){
      menu.edits[key].href="#";
      menu.edits[key].title=message.zoom_to;
    }
  }
  menu.views.rssTile.href='rss.php?x='+x+'&y='+y;
  menu.views.permalink.href = lonlat+layers;
  menu.views.osmlink.href = 'http://www.openstreetmap.org/'+lonlat;
  menu.views.osblink.href = 'http://openstreetbugs.schokokeks.org/'+lonlat+layers;
  menu.views.geofabrik.href = "http://tools.geofabrik.de/map/"+lonlat;
  for(id in menu.langs){
    menu.langs[id].href='index.html.'+id+lonlat+layers;
  }
}

/** Marker events */
function marker_click(ev){/* "this" means feature */
    if (this.popuped){
        map.removePopup(this.popup);
        this.popuped=false;
    } else if (map.popups.length<20){
        if(!this.popup.map){map.addPopup(this.popup);}
        this.popuped=true;
    }
    document.getElementById("photoID").innerHTML = this.photo.id;
    document.getElementById("numberOfPopuping").innerHTML = map.popups.length;
    OpenLayers.Event.stop(ev);
}

function popup_close(ev){/* "this" means popup */
    this.feature.popuped=false;
    map.removePopup(this);
    document.getElementById("numberOfPopuping").innerHTML = map.popups.length;
    OpenLayers.Event.stop(ev);
}

function marker_mouseover(ev){/* "this" means feature */
  if (!this.popuped){
    if(!this.popup){
      this.popup = new OpenLayers.Popup.FramedCloud(this.id+'_popup',
                this.lonlat,
                this.data.popupSize,
                popup_open_photo(this.photo),
                this.marker.icon,
                true,
                popup_close);
      this.popup.panMapIfOutOfView=false;
      this.popup.feature = this;
      if(this.photo.dir){
        this.vector=create_view(this.photo,this.lonlat);
      }
    }
    if(this.vector)vectorLayer.addFeatures(this.vector);
    map.addPopup(this.popup);
  }
  document.getElementById("map_OpenLayers_Container").style.cursor = "pointer";
  OpenLayers.Event.stop(ev);
}

function marker_mouseout(ev){/* "this" means feature */
  if (!this.popuped){
    map.removePopup(this.popup);
    if(this.vector)vectorLayer.removeFeatures(this.vector);
  }
  document.getElementById("map_OpenLayers_Container").style.cursor = "crosshair";
  OpenLayers.Event.stop(ev);
}

function toggleBar(index) {
    var NavToggle = document.getElementById("NavToggle" + index);
    var NavContext = document.getElementById("NavContext" + index);
    if (!NavContext || !NavToggle) {return false;}
    // if shown now
    if (NavToggle.firstChild.data == 'hide') {
        NavContext.style.display = 'none';
        NavToggle.firstChild.data = 'show';
    } else if (NavToggle.firstChild.data == 'show') {
        NavContext.style.display = 'block';
        NavToggle.firstChild.data = 'hide';
    }
    return false;
}

init2();
