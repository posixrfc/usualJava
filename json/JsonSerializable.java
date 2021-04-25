package wcy.usual.json;

public interface JsonSerializable
{
public default CharSequence toJsonValue()
{
	return JsonSerializer.serialize(this);
}
public default CharSequence toJsonKey()
{
	return '"'+JsonSerializer.toJsonStandard(toString()).toString()+'"';
}
}
/*util.obj2xml=(key,val)=>{
if("number"===typeof val){
	return '<'+key+'>'+val+'</'+key+'>';
}
if("boolean"===typeof val){
	return '<'+key+'>'+val+'</'+key+'>';
}
if(!val){
	return null;
}
if("string"===typeof val){
	return '<'+key+'>'+val+'</'+key+'>';
}
let rs='';
if(Array.isArray(val)){
	for(let i=0,tmp;i!==val.length;i++){
		tmp=util.obj2xml(key,val[i]);
		if(tmp){
			rs+=tmp;
		}
	}
	return 0===rs.length?null:rs;
}
let mismatch=true;
rs='<'+key+'>';
for(let k in val){
	tmp=util.obj2xml(k,val[k]);
	if(tmp){
		mismatch=false;
		rs+=tmp;
	}
}
if(mismatch){
	return null;
}
return rs+'</'+key+'>';
};
util.xml2obj=(obj,val)=>{
val=val.trim();
if(0===val.length){
	return false;
}
if(val.charAt(0)!=='<'){
	return false;
}
let idx=val.indexOf('>',2);
if(-1===idx){
	return false;
}
let tag=val.substring(1,idx);
if(idx+idx+3>=val.length){
	return false;
}
val=val.substring(idx+1).trim();
idx=val.indexOf('</'+tag+'>');
if(-1===idx){
	return false;
}
let tmp=idx-tag.length-2,noTag=true;
while(tmp>=0 && ((tmp=val.lastIndexOf('<'+tag+'>',tmp))!==-1)){
	noTag=false;
	idx=idx+tag.length+3;
	if(idx+tag.length+3>=val.length){
		return false;
	}
	idx=val.indexOf('</'+tag+'>',idx);
	if(-1===idx){
		return false;
	}
	if(idx+tag.length+3===val.length){
		break;
	}
	tmp=tmp-tag.length-2;
}
let cnt=val.substring(0,idx).trim();
val=val.substring(idx+tag.length+3);
if(noTag && cnt.indexOf('<')!==-1){
	noTag=false;
}
if(noTag){
	if(obj[tag]){
		if(Array.isArray(obj[tag])){
			obj[tag].push(cnt);
		}else{
			obj[tag]=[obj[tag]];
			obj[tag].push(cnt);
		}
	}else{
		obj[tag]=cnt;
	}
}else{
	if(obj[tag]){
		tmp={};
		if(Array.isArray(obj[tag])){
			obj[tag].push(tmp);
		}else{
			obj[tag]=[obj[tag]];
			obj[tag].push(tmp);
		}
	}else{
		tmp=obj[tag]={};
	}
	if(!util.xml2obj(tmp,cnt)){
		delete obj[tag];
		return false;
	}
}
return val ? util.xml2obj(obj,val) : true;
};*/