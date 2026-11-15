import { useEffect, useState } from 'react';
import { Form, Pagination } from 'react-bootstrap';

/**
 * Chia danh sách dài thành từng trang. Trả về { dsTrang, dieuKhien }.
 * khoaLoc: đổi bộ lọc / từ khóa thì quay về trang 1.
 */
export function usePhanTrang(ds, khoaLoc = '', soDongMacDinh = 15) {
  const [trang, setTrang] = useState(1);
  const [soDong, setSoDong] = useState(soDongMacDinh);
  const tong = ds.length;
  const soTrang = Math.max(1, Math.ceil(tong / soDong));

  useEffect(() => setTrang(1), [khoaLoc, soDong]);
  useEffect(() => {
    if (trang > soTrang) setTrang(soTrang);
  }, [trang, soTrang]);

  const dsTrang = ds.slice((trang - 1) * soDong, trang * soDong);

  // Hiện tối đa 5 nút số quanh trang hiện tại
  const dau = Math.max(1, Math.min(trang - 2, soTrang - 4));
  const so = [];
  for (let i = dau; i <= Math.min(soTrang, dau + 4); i++) so.push(i);

  const dieuKhien = tong === 0 ? null : (
    <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 px-2 py-2 border-top small">
      <span className="text-muted">
        {(trang - 1) * soDong + 1}–{Math.min(trang * soDong, tong)} / {tong} dòng
      </span>
      <div className="d-flex align-items-center gap-2">
        <Form.Select size="sm" style={{ width: 'auto' }} value={soDong} onChange={(e) => setSoDong(Number(e.target.value))}
          aria-label="Số dòng mỗi trang">
          {[10, 15, 25, 50].map((n) => <option key={n} value={n}>{n} dòng/trang</option>)}
        </Form.Select>
        {soTrang > 1 && (
          <Pagination size="sm" className="mb-0">
            <Pagination.First disabled={trang === 1} onClick={() => setTrang(1)} />
            <Pagination.Prev disabled={trang === 1} onClick={() => setTrang(trang - 1)} />
            {so.map((i) => <Pagination.Item key={i} active={i === trang} onClick={() => setTrang(i)}>{i}</Pagination.Item>)}
            <Pagination.Next disabled={trang === soTrang} onClick={() => setTrang(trang + 1)} />
            <Pagination.Last disabled={trang === soTrang} onClick={() => setTrang(soTrang)} />
          </Pagination>
        )}
      </div>
    </div>
  );

  return { dsTrang, dieuKhien };
}
