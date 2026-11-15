/** Mở vị trí báo cáo trên Google Maps (tab mới). */
export default function LinkBanDo({ viDo, kinhDo }) {
  if (viDo === null || viDo === undefined || kinhDo === null || kinhDo === undefined) return null;
  return (
    <a href={`https://www.google.com/maps?q=${viDo},${kinhDo}`} target="_blank" rel="noopener noreferrer" className="ms-2 small">
      Xem trên bản đồ ↗
    </a>
  );
}
