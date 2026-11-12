import { useEffect, useState } from 'react';
import { urlAnh } from '../api/index.js';

/** Ảnh lớn và các ảnh thu nhỏ (anhLon, anhNho1..5), tải qua GET /api/anh/{ma}. */
export default function XemAnh({ dsHinhAnh }) {
  const [chon, setChon] = useState(0);
  useEffect(() => setChon(0), [dsHinhAnh]);
  if (!dsHinhAnh || dsHinhAnh.length === 0) return <p className="text-muted">Báo cáo không có ảnh.</p>;
  const hien = dsHinhAnh[Math.min(chon, dsHinhAnh.length - 1)];
  return (
    <div>
      <img className="anh-lon mb-2" src={urlAnh(hien.maHinhAnh)} alt="Ảnh hiện trường" />
      <div className="d-flex gap-2 flex-wrap">
        {dsHinhAnh.map((h, i) => (
          <img
            key={h.maHinhAnh}
            className={'anh-nho' + (i === chon ? ' chon' : '')}
            src={urlAnh(h.maHinhAnh)}
            alt={'Ảnh ' + (i + 1)}
            onClick={() => setChon(i)}
          />
        ))}
      </div>
    </div>
  );
}
