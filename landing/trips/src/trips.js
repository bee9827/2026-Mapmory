export function distance(a,b){const r=Math.PI/180;const x=Math.sin((b.latitude-a.latitude)*r/2)**2+Math.cos(a.latitude*r)*Math.cos(b.latitude*r)*Math.sin((b.longitude-a.longitude)*r/2)**2;return 6371*2*Math.asin(Math.sqrt(Math.min(1,Math.max(0,x))));}
export function homeOptions(records){
  const options=[];
  for(const record of records.filter(r=>r.gps).sort((a,b)=>a.index-b.index)){
    let area=options.find(a=>distance(a.gps,record.gps)<=30);
    if(!area){area={gps:record.gps,label:record.city||record.location||'위치 정보 있는 지역',count:0,photos:[]};options.push(area);}
    area.count++;area.photos.push(record);
  }
  return options.sort((a,b)=>b.count-a.count);
}
export function groupTrips(records,home,{radius=40,gapHours=72}={}){
  if(!home)throw new Error('생활 지역을 선택해주세요.');
  const dated=records.filter(r=>r.date&&r.gps).map(record=>({record,time:Date.parse(record.date.capturedAt+'Z')})).filter(r=>Number.isFinite(r.time)).sort((a,b)=>a.time-b.time||a.record.index-b.record.index);
  const trips=[];let current=null;
  for(const {record,time} of dated){
    if(distance(home,record.gps)<=radius){current=null;continue;}
    if(!current||time-current.end>gapHours*3600000){current={start:time,end:time,photos:[]};trips.push(current);}
    current.end=time;current.photos.push(record);
  }
  const assigned=new Set(trips.flatMap(t=>t.photos.map(p=>p.index)));
  return {trips,other:records.filter(r=>!assigned.has(r.index))};
}
