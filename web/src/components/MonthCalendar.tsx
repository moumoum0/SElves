import { useState } from 'react';

interface MonthCalendarProps {
  selectedDate: Date;
  onDateSelected: (date: Date) => void;
}

const WEEKDAYS = ['日', '一', '二', '三', '四', '五', '六'];

function sameDay(a: Date, b: Date) {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
}

export function MonthCalendar({ selectedDate, onDateSelected }: MonthCalendarProps) {
  const [viewYear, setViewYear] = useState(selectedDate.getFullYear());
  const [viewMonth, setViewMonth] = useState(selectedDate.getMonth());

  const today = new Date();

  const firstDay = new Date(viewYear, viewMonth, 1).getDay();
  const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate();
  const prevMonth = () => {
    if (viewMonth === 0) { setViewYear(y => y - 1); setViewMonth(11); }
    else setViewMonth(m => m - 1);
  };
  const nextMonth = () => {
    const now = new Date();
    if (viewYear > now.getFullYear() || (viewYear === now.getFullYear() && viewMonth >= now.getMonth())) return;
    if (viewMonth === 11) { setViewYear(y => y + 1); setViewMonth(0); }
    else setViewMonth(m => m + 1);
  };

  const isNextDisabled = viewYear > today.getFullYear() || (viewYear === today.getFullYear() && viewMonth >= today.getMonth());

  const totalCells = firstDay + daysInMonth;
  const rows = Math.ceil(totalCells / 7);

  return (
    <div style={{ backgroundColor: 'rgb(var(--mdui-color-surface-container))', borderRadius: 16, padding: 12 }}>
      {/* 月份导航 */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
        <button type="button" onClick={prevMonth} style={{ width: 36, height: 36, borderRadius: '50%', border: 'none', background: 'transparent', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <mdui-icon name="chevron_left" style={{ fontSize: 20, color: 'rgb(var(--mdui-color-on-surface))' }}></mdui-icon>
        </button>
        <span style={{ fontSize: 16, fontWeight: 700, color: 'rgb(var(--mdui-color-on-surface))' }}>{viewYear}年{viewMonth + 1}月</span>
        <button type="button" onClick={nextMonth} disabled={isNextDisabled} style={{ width: 36, height: 36, borderRadius: '50%', border: 'none', background: 'transparent', cursor: isNextDisabled ? 'default' : 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', opacity: isNextDisabled ? 0.3 : 1 }}>
          <mdui-icon name="chevron_right" style={{ fontSize: 20, color: 'rgb(var(--mdui-color-on-surface))' }}></mdui-icon>
        </button>
      </div>

      {/* 星期标题 */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7,1fr)', marginBottom: 8 }}>
        {WEEKDAYS.map(d => (
          <div key={d} style={{ textAlign: 'center', fontSize: 13, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{d}</div>
        ))}
      </div>

      {/* 日期格子 */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7,1fr)', gap: 2 }}>
        {Array.from({ length: rows * 7 }).map((_, idx) => {
          const dayNum = idx - firstDay + 1;
          if (dayNum < 1 || dayNum > daysInMonth) return <div key={idx} />;
          const date = new Date(viewYear, viewMonth, dayNum);
          const isFuture = date > today;
          const isSelected = sameDay(date, selectedDate);
          const isToday = sameDay(date, today);
          return (
            <div key={idx} style={{ aspectRatio: '1', padding: 2 }}>
              <div
                onClick={() => !isFuture && onDateSelected(date)}
                style={{
                  width: '100%', height: '100%', borderRadius: 8,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  cursor: isFuture ? 'default' : 'pointer',
                  backgroundColor: isSelected ? 'rgb(var(--mdui-color-primary))' : isToday ? 'rgb(var(--mdui-color-primary-container))' : 'rgb(var(--mdui-color-surface))',
                  fontSize: 14,
                  fontWeight: isSelected || isToday ? 700 : 400,
                  color: isSelected ? 'rgb(var(--mdui-color-on-primary))' : isToday ? 'rgb(var(--mdui-color-on-primary-container))' : isFuture ? 'rgba(var(--mdui-color-on-surface),0.3)' : 'rgb(var(--mdui-color-on-surface))',
                }}
              >{dayNum}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
