embedBox.embed= function(id){
    var params = permalink.createParams();
    this.lat = photos[id].photo.lat;
    this.lon = photos[id].photo.lon;
    this.zoom = params.zoom;
    var layers=params.layers;
    this.ref=domain+server_path+'?lat='+this.lat+'&lon='+this.lon+'&zoom='+this.zoom+'&layers='+layers+"&id="+id;
    this.div.style.display='block';
    this.embed2('a');
};
embedBox.embed2=function(s) {
    if(s=="a") this.box.value=this.ref;
    if(s=="h") this.box.value='<a href="'+this.ref+'">See it on OpenStreetMap.</a>';
    if(s=="H") this.box.value='<a href="'+this.ref+'">lat='+this.lat+', lon='+this.lon+'</a>';
    if(s=="O") this.box.value='<a href="'+this.ref+'"><img src="http://tah.openstreetmap.org/MapOf/?lat='+this.lat+'&long='+this.lon+'&z='+this.zoom+'&w=96&h=96&format=png" width="96" height="96"/></a>';
    if(s=="OA") this.box.value='<a href="'+this.ref+'"><img src="http://tah.openstreetmap.org/MapOf/?lat='+this.lat+'&long='+this.lon+'&z='+this.zoom+'&w=96&h=96&format=png" width="96" height="96"/>See it on OpenStreetMap</a>';
    this.test.innerHTML=this.box.value;
};
embedBox.init=function (){
    this.div=document.createElement("div");
    this.div.className='message';

    this.box=document.createElement("input");
    this.box.type='text';
    this.box.name='htmlCode';
    this.div.appendChild(this.box);
    this.div.appendChild(document.createElement('br'));

    var kind=document.createElement("input");
    kind.name='kind';
    kind.type='radio';
    kind.value="ascii";
    kind.onclick=function(){
        embedBox.embed2('a')
    };
    this.div.appendChild(kind);
    this.div.appendChild(document.createTextNode(message.url));
    this.div.appendChild(document.createElement('br'));

    kind=document.createElement("input");
    kind.name='kind';
    kind.type='radio';
    kind.value="html";
    kind.onclick=function(){
        embedBox.embed2('h')
    };
    this.div.appendChild(kind);
    this.div.appendChild(document.createTextNode(message.html1));
    this.div.appendChild(document.createElement('br'));

    kind=document.createElement("input");
    kind.name='kind';
    kind.type='radio';
    kind.value="html2";
    kind.onclick=function(){
        embedBox.embed2('H')
    };
    this.div.appendChild(kind);
    this.div.appendChild(document.createTextNode(message.html2));
    this.div.appendChild(document.createElement('br'));

    kind=document.createElement("input");
    kind.name='kind';
    kind.type='radio';
    kind.value="map";
    kind.onclick=function(){
        embedBox.embed2('O')
    };
    this.div.appendChild(kind);
    this.div.appendChild(document.createTextNode(message.image));
    this.div.appendChild(document.createElement('br'));

    kind=document.createElement("input");
    kind.name='kind';
    kind.type='radio';
    kind.value="MnA";
    kind.onclick=function(){
        embedBox.embed2('OA')
    };
    this.div.appendChild(kind);
    this.div.appendChild(document.createTextNode(message.imageText));
    this.div.appendChild(document.createElement('br'));

    this.test=document.createElement("div");
    this.div.appendChild(this.test);

    kind=document.createElement("div");
    kind.className='olPopupCloseBox';
    kind.style.width="17px";
    kind.style.height="17px"
    kind.style.position="absolute"
    kind.style.right="13px";
    kind.style.top="14px";
    kind.style.zindex="1";
    kind.onclick=function(){
        embedBox.div.style.display='none'
    };
    this.div.appendChild(kind);

    document.body.appendChild(this.div);
    this.embed(this.id);
    this.id=null;
}
embedBox.init();
embedBox.init=null;