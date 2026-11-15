/** Bảng nhãn – giá trị gọn (Người gửi, Khu vực, ...). dong: mảng [nhãn, giá trị]; dòng có giá trị rỗng bị bỏ. */
export default function ThongTin({ dong }) {
  return (
    <dl className="thong-tin">
      {dong.filter(([, gt]) => gt !== null && gt !== undefined && gt !== '').map(([nhan, gt]) => (
        <div key={nhan} className="contents">
          <dt>{nhan}</dt>
          <dd>{gt}</dd>
        </div>
      ))}
    </dl>
  );
}
