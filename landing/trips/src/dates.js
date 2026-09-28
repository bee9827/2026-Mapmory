export function localDay(date) {
  const pad = value => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

export function groupByDate(records) {
  const groups=new Map();
  for(const record of records) {
    const day=record.date?.day ?? '촬영일 정보 없음';
    if(!groups.has(day))groups.set(day,[]);
    groups.get(day).push(record);
  }
  return [...groups].sort(([a],[b])=>a.localeCompare(b)).map(([day,photos])=>({day,photos:[...photos].sort((a,b)=>(a.date?.capturedAt??'').localeCompare(b.date?.capturedAt??'')||a.index-b.index)}));
}
